package mod.azure.azurelib.render.vertex;

import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Beta 1.7.3 implementation of {@link MultiBufferSource}.
 * <p>
 * Vertices handed to the consumers returned by {@link #getBuffer(RenderType)} are recorded per render type, in the
 * order the render types were first used, and are drawn with client-side GL vertex arrays when {@link #endBatch()} is
 * called. (Beta's {@code Tessellator} is avoided entirely, which keeps this class independent of mappings.) This keeps
 * 1.18's batching semantics: AzureLib's renderers may freely switch between render types mid-model (bone texture
 * overrides, glow layers, glint, leashes) without fighting over Beta's single {@code Tessellator}.
 * <p>
 * Each vertex is written once, already in the final interleaved GL layout (position, UV, normalised normal, and a
 * colour with lighting, hurt tint and glint colour folded in). Drawing a batch is then a single bulk copy into the
 * direct buffer followed by {@code glDrawArrays}; nothing is recomputed per vertex at draw time.
 * <p>
 * Positions are relative to the OpenGL model-view matrix that is current when {@link #endBatch()} runs, so a batch must
 * be ended before that matrix changes. AzureLib's renderers end the batch at the end of every top-level render call and
 * before handing control to vanilla rendering code (held items, vanilla armor, the player's arm).
 */
public final class AzBufferSource implements MultiBufferSource {

    private static final AzBufferSource INSTANCE = new AzBufferSource();

    /** x, y, z, u, v, nx, ny, nz, r, g, b, a */
    private static final int GL_FLOATS_PER_VERTEX = 12;

    private static final int GL_STRIDE = GL_FLOATS_PER_VERTEX * 4;

    /** Offset of the colour (r) within a vertex, in floats. */
    private static final int COLOR_OFFSET = 8;

    /** Glint colours. Both passes of a glint mode use the same colour, so it is written at record time. */
    private static final int ITEM_GLINT_COLOR = 0xFF8040CC;

    private static final int ARMOR_GLINT_COLOR = 0xFF61309B;

    private final Map<RenderType, Batch> batches = new IdentityHashMap<>();

    private final List<Batch> pending = new ArrayList<>();

    private boolean flushing;

    private AzBufferSource() {}

    /**
     * The shared buffer source. All AzureLib rendering happens on the client thread, which is also the only thread that
     * may touch the GL context on Beta.
     */
    public static AzBufferSource getInstance() {
        return INSTANCE;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        Batch batch = this.batches.get(renderType);
        if (batch == null) {
            batch = new Batch(this, renderType);
            this.batches.put(renderType, batch);
        }
        return batch;
    }

    /**
     * Returns a consumer for the given render type that also writes the vertices into the enchantment glint pass when
     * {@code withGlint} is set. Mirrors 1.18's {@code ItemRenderer.getFoilBufferDirect}.
     */
    public static VertexConsumer getFoilBuffer(MultiBufferSource source, RenderType renderType, boolean withGlint) {
        VertexConsumer base = source.getBuffer(renderType);
        return withGlint ? VertexMultiConsumer.create(source.getBuffer(RenderType.entityGlint()), base) : base;
    }

    /** Mirrors 1.18's {@code ItemRenderer.getArmorFoilBuffer}. */
    public static VertexConsumer getArmorFoilBuffer(
        MultiBufferSource source,
        RenderType renderType,
        boolean withGlint
    ) {
        VertexConsumer base = source.getBuffer(renderType);
        return withGlint ? VertexMultiConsumer.create(source.getBuffer(RenderType.armorEntityGlint()), base) : base;
    }

    /** True if any vertices are waiting to be drawn. */
    public boolean hasPending() {
        return !this.pending.isEmpty();
    }

    /**
     * Draws everything recorded so far, in the order the render types were first used, then glint passes last.
     */
    public void endBatch() {
        if (this.flushing || this.pending.isEmpty()) {
            return;
        }
        this.flushing = true;
        try {
            boolean anyGlint = false;
            for (int i = 0, size = this.pending.size(); i < size; i++) {
                Batch batch = this.pending.get(i);
                if (batch.renderType.isGlint()) {
                    anyGlint = true;
                    continue;
                }
                draw(batch);
            }
            if (anyGlint) {
                for (int i = 0, size = this.pending.size(); i < size; i++) {
                    Batch batch = this.pending.get(i);
                    if (batch.renderType.isGlint()) {
                        draw(batch);
                    }
                }
            }
        } finally {
            for (int i = 0, size = this.pending.size(); i < size; i++) {
                this.pending.get(i).reset();
            }
            this.pending.clear();
            this.flushing = false;
        }
    }

    private static void draw(Batch batch) {
        if (batch.vertexCount == 0) {
            return;
        }
        RenderType renderType = batch.renderType;
        renderType.setupRenderState();
        try {
            fill(batch);
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
            try {
                bindPointers();
                switch (renderType.glintMode()) {
                    case ITEM:
                        drawItemGlint(batch);
                        break;
                    case ARMOR:
                        drawArmorGlint(batch);
                        break;
                    default:
                        GL11.glDrawArrays(renderType.glMode(), 0, batch.vertexCount);
                        if (batch.hasFlash) {
                            drawWhiteOverlay(batch);
                        }
                        break;
                }
            } finally {
                GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
                GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                // Leave the current colour white so vanilla code drawing after us isn't tinted.
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        } finally {
            renderType.clearRenderState();
        }
    }

    /** Item glint, kept for API parity: Beta has no enchantments, but mods may still request a glint pass. */
    private static void drawItemGlint(Batch batch) {
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        for (int pass = 0; pass < 2; pass++) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(8.0F, 8.0F, 8.0F);
            long period = pass == 0 ? 3000L : 4873L;
            float offset = (float) (System.currentTimeMillis() % period) / (float) period / 8.0F;
            GlStateManager.translate(pass == 0 ? offset : -offset, 0.0F, 0.0F);
            GlStateManager.rotate(pass == 0 ? -50.0F : 10.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GL11.glDrawArrays(batch.renderType.glMode(), 0, batch.vertexCount);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
        }
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    private static void drawArmorGlint(Batch batch) {
        float time = System.currentTimeMillis() % 100000L / 50.0F;
        for (int pass = 0; pass < 2; pass++) {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotate(30.0F - pass * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, time * (0.001F + pass * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GL11.glDrawArrays(batch.renderType.glMode(), 0, batch.vertexCount);
        }
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    private static ByteBuffer glBytes;

    private static FloatBuffer glBuffer;

    static {
        allocate(4096);
    }

    private static void allocate(int vertices) {
        glBytes = ByteBuffer.allocateDirect(vertices * GL_STRIDE).order(ByteOrder.nativeOrder());
        glBuffer = glBytes.asFloatBuffer();
    }

    /** Copies the batch's pre-built vertex data into the direct buffer with one bulk put. */
    private static void fill(Batch batch) {
        int floats = batch.vertexCount * GL_FLOATS_PER_VERTEX;
        if (glBuffer.capacity() < floats) {
            allocate(Math.max(batch.vertexCount, glBuffer.capacity() / GL_FLOATS_PER_VERTEX * 2));
        }
        FloatBuffer out = glBuffer;
        out.clear();
        out.put(batch.data, 0, floats);
        out.flip();
    }

    private static void bindPointers() {
        ByteBuffer bytes = glBytes;
        bytes.limit(glBuffer.limit() * 4);
        bytes.position(0);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, GL_STRIDE, bytes);
        bytes.position(3 * 4);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, GL_STRIDE, bytes);
        bytes.position(5 * 4);
        GL11.glNormalPointer(GL11.GL_FLOAT, GL_STRIDE, bytes);
        bytes.position(COLOR_OFFSET * 4);
        GL11.glColorPointer(4, GL11.GL_FLOAT, GL_STRIDE, bytes);
        bytes.position(0);
    }

    /**
     * Second pass for the 1.18 white damage flash: same geometry, white colour with the flash strength as alpha, drawn
     * over the first pass with an equal depth test. Rewrites only the colours in the direct buffer; the batch's own
     * data is untouched, and the pointers bound for the first pass stay valid.
     */
    private static void drawWhiteOverlay(Batch batch) {
        FloatBuffer out = glBuffer;
        float[] flash = batch.flash;
        int count = batch.vertexCount;
        for (int v = 0; v < count; v++) {
            int base = v * GL_FLOATS_PER_VERTEX + COLOR_OFFSET;
            out.put(base, 1.0F).put(base + 1, 1.0F).put(base + 2, 1.0F).put(base + 3, flash[v]);
        }

        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        int depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDepthMask(false);
        try {
            GL11.glDrawArrays(batch.renderType.glMode(), 0, count);
        } finally {
            GL11.glDepthMask(true);
            GL11.glDepthFunc(depthFunc);
            if (!blend)
                GL11.glDisable(GL11.GL_BLEND);
            if (texture)
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            if (alphaTest)
                GL11.glEnable(GL11.GL_ALPHA_TEST);
        }
    }

    /**
     * Beta has no lightmap texture: lighting is a per-vertex brightness multiplied into the colour, exactly like
     * vanilla's {@code glColor(brightness)} before drawing a model. AzureLib keeps 1.18-style packed light values
     * (block and sky level, 0..15) and folds the brighter of the two into the vertex colour.
     */
    private static float brightness(int packedLight) {
        int level = Math.max(LightTexture.block(packedLight), LightTexture.sky(packedLight));
        return Math.max(0, Math.min(15, level)) / 15.0F;
    }

    /** Strength (0..1) of the 1.18 white flash encoded in the overlay's U coordinate. */
    private static float whiteOverlay(int overlay) {
        if (overlay == OverlayTexture.NO_OVERLAY) {
            return 0;
        }
        int u = overlay & 0xFFFF;
        return Math.min(u, 15) / 15.0F;
    }

    /**
     * The red hurt tint of 1.18's overlay texture, folded into the vertex colour (multiplying works for darkening green
     * and blue; the white flash is a separate pass, see {@link #drawWhiteOverlay}).
     */
    private static int applyHurtTint(int argb, int overlay) {
        if (overlay == OverlayTexture.NO_OVERLAY || (overlay >> 16 & 0xFFFF) >= 8) {
            return argb;
        }
        float keep = 0.69803923F;
        float r = (argb >> 16 & 0xFF) / 255.0F * keep + (1.0F - keep);
        float g = (argb >> 8 & 0xFF) / 255.0F * keep;
        float b = (argb & 0xFF) / 255.0F * keep;
        int a = argb >>> 24;
        return a << 24 | (int) (r * 255.0F) << 16 | (int) (g * 255.0F) << 8 | (int) (b * 255.0F);
    }

    private static int clampColor(float value) {
        return Math.max(0, Math.min(255, (int) (value * 255.0F)));
    }

    private static final class Batch implements VertexConsumer {

        private static final int INITIAL_VERTICES = 256;

        private final AzBufferSource owner;

        private final RenderType renderType;

        /** Final GL vertex data, {@link #GL_FLOATS_PER_VERTEX} floats per vertex. */
        private float[] data = new float[GL_FLOATS_PER_VERTEX * INITIAL_VERTICES];

        /** White-flash alpha per vertex; only meaningful while {@link #hasFlash} is set. */
        private float[] flash = new float[INITIAL_VERTICES];

        private int vertexCount;

        private boolean queued;

        private boolean hasFlash;

        // Colour cache: consecutive vertices almost always share colour, overlay and light, so the folded colour is
        // computed once per change instead of once per vertex. Keys are only ever compared, never reset: the result is
        // a pure function of the key and the (final) render type.
        private boolean colorCached;

        private int cArgb, cOverlay, cLight;

        private float cr, cg, cb, ca, cFlash;

        // Float-colour cache for the bulk vertex(...) path, which receives r/g/b/a floats.
        private float fr = Float.NaN, fg, fb, fa;

        private int fArgb;

        // Normal cache: the four vertices of a quad share one normal, so it is normalised once per quad.
        // NaN keys never compare equal, so the first vertex (and any NaN normal) always recomputes.
        private float rnx = Float.NaN, rny = Float.NaN, rnz = Float.NaN;

        private float nnx, nny = 1.0F, nnz;

        // Element-by-element vertex state
        private float x, y, z, u, v, nx, ny = 1.0F, nz;

        private int color = 0xFFFFFFFF;

        private int overlay = OverlayTexture.NO_OVERLAY;

        private int light = LightTexture.FULL_BRIGHT;

        private Batch(AzBufferSource owner, RenderType renderType) {
            this.owner = owner;
            this.renderType = renderType;
        }

        private void reset() {
            this.vertexCount = 0;
            this.queued = false;
            this.hasFlash = false;
        }

        private void ensureCapacity() {
            if (this.vertexCount >= this.flash.length) {
                int newVertices = Math.max(INITIAL_VERTICES, this.vertexCount * 2);
                this.data = Arrays.copyOf(this.data, newVertices * GL_FLOATS_PER_VERTEX);
                this.flash = Arrays.copyOf(this.flash, newVertices);
            }
            if (!this.queued) {
                this.queued = true;
                this.owner.pending.add(this);
            }
        }

        private void updateColor(int argb, int overlayUV, int lightmapUV) {
            this.cArgb = argb;
            this.cOverlay = overlayUV;
            this.cLight = lightmapUV;
            this.colorCached = true;

            int out;
            float lightLevel;
            float white;
            switch (this.renderType.glintMode()) {
                case ITEM:
                    out = ITEM_GLINT_COLOR;
                    lightLevel = 1.0F;
                    white = 0.0F;
                    break;
                case ARMOR:
                    out = ARMOR_GLINT_COLOR;
                    lightLevel = 1.0F;
                    white = 0.0F;
                    break;
                default:
                    out = applyHurtTint(argb, overlayUV);
                    lightLevel = this.renderType.isEmissive() ? 1.0F : brightness(lightmapUV);
                    white = whiteOverlay(overlayUV) * ((argb >>> 24) / 255.0F);
                    break;
            }
            this.cr = (out >> 16 & 0xFF) / 255.0F * lightLevel;
            this.cg = (out >> 8 & 0xFF) / 255.0F * lightLevel;
            this.cb = (out & 0xFF) / 255.0F * lightLevel;
            this.ca = (out >>> 24) / 255.0F;
            this.cFlash = white;
        }

        private void put(
            float px,
            float py,
            float pz,
            int argb,
            float tu,
            float tv,
            int overlayUV,
            int lightmapUV,
            float normalX,
            float normalY,
            float normalZ
        ) {
            ensureCapacity();

            if (!this.colorCached || argb != this.cArgb || overlayUV != this.cOverlay || lightmapUV != this.cLight) {
                updateColor(argb, overlayUV, lightmapUV);
            }

            if (normalX != this.rnx || normalY != this.rny || normalZ != this.rnz) {
                this.rnx = normalX;
                this.rny = normalY;
                this.rnz = normalZ;
                float lenSq = normalX * normalX + normalY * normalY + normalZ * normalZ;
                if (lenSq > 1.0E-12F && Float.isFinite(lenSq)) {
                    float inv = 1.0F / (float) Math.sqrt(lenSq);
                    this.nnx = normalX * inv;
                    this.nny = normalY * inv;
                    this.nnz = normalZ * inv;
                } else {
                    this.nnx = 0.0F;
                    this.nny = 1.0F;
                    this.nnz = 0.0F;
                }
            }

            int i = this.vertexCount;
            float[] d = this.data;
            int o = i * GL_FLOATS_PER_VERTEX;
            d[o] = px;
            d[o + 1] = py;
            d[o + 2] = pz;
            d[o + 3] = tu;
            d[o + 4] = tv;
            d[o + 5] = this.nnx;
            d[o + 6] = this.nny;
            d[o + 7] = this.nnz;
            d[o + 8] = this.cr;
            d[o + 9] = this.cg;
            d[o + 10] = this.cb;
            d[o + 11] = this.ca;

            float white = this.cFlash;
            if (white > 0.0F && !this.hasFlash) {
                // First flashing vertex in this batch: earlier vertices have no flash.
                this.hasFlash = true;
                Arrays.fill(this.flash, 0, i, 0.0F);
            }
            if (this.hasFlash) {
                this.flash[i] = white;
            }

            this.vertexCount = i + 1;
        }

        @Override
        public void vertex(
            float x,
            float y,
            float z,
            float red,
            float green,
            float blue,
            float alpha,
            float texU,
            float texV,
            int overlayUV,
            int lightmapUV,
            float normalX,
            float normalY,
            float normalZ
        ) {
            if (red != this.fr || green != this.fg || blue != this.fb || alpha != this.fa) {
                this.fr = red;
                this.fg = green;
                this.fb = blue;
                this.fa = alpha;
                this.fArgb = clampColor(alpha) << 24 | clampColor(red) << 16 | clampColor(green) << 8 | clampColor(
                    blue
                );
            }
            put(x, y, z, this.fArgb, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.color = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            this.overlay = u & 0xFFFF | (v & 0xFFFF) << 16;
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            this.light = u & 0xFFFF | (v & 0xFFFF) << 16;
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.nx = x;
            this.ny = y;
            this.nz = z;
            return this;
        }

        @Override
        public void endVertex() {
            put(
                this.x,
                this.y,
                this.z,
                this.color,
                this.u,
                this.v,
                this.overlay,
                this.light,
                this.nx,
                this.ny,
                this.nz
            );
            this.color = 0xFFFFFFFF;
            this.overlay = OverlayTexture.NO_OVERLAY;
            this.light = LightTexture.FULL_BRIGHT;
            this.u = 0;
            this.v = 0;
            this.nx = 0;
            this.ny = 1;
            this.nz = 0;
        }
    }
}
