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
 * Positions are relative to the OpenGL model-view matrix that is current when {@link #endBatch()} runs, so a batch must
 * be ended before that matrix changes. AzureLib's renderers end the batch at the end of every top-level render call and
 * before handing control to vanilla rendering code (held items, vanilla armor, the player's arm).
 */
public final class AzBufferSource implements MultiBufferSource {

    private static final AzBufferSource INSTANCE = new AzBufferSource();

    /** x, y, z, u, v, nx, ny, nz */
    private static final int FLOATS_PER_VERTEX = 8;

    /** colour (ARGB), packed overlay, packed light */
    private static final int INTS_PER_VERTEX = 3;

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
            List<Batch> glintBatches = null;
            for (int i = 0; i < this.pending.size(); i++) {
                Batch batch = this.pending.get(i);
                if (batch.renderType.isGlint()) {
                    if (glintBatches == null) {
                        glintBatches = new ArrayList<>(2);
                    }
                    glintBatches.add(batch);
                    continue;
                }
                draw(batch);
            }
            if (glintBatches != null) {
                for (Batch batch : glintBatches) {
                    draw(batch);
                }
            }
        } finally {
            for (Batch batch : this.pending) {
                batch.reset();
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
            switch (renderType.glintMode()) {
                case ITEM:
                    drawItemGlint(batch);
                    break;
                case ARMOR:
                    drawArmorGlint(batch);
                    break;
                default:
                    upload(batch, -1);
                    break;
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
            upload(batch, 0xFF8040CC);
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
            upload(batch, 0xFF61309B);
        }
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    /** x, y, z, u, v, nx, ny, nz, r, g, b, a */
    private static final int GL_FLOATS_PER_VERTEX = 12;

    private static final int GL_STRIDE = GL_FLOATS_PER_VERTEX * 4;

    private static ByteBuffer glBytes;

    private static FloatBuffer glBuffer;

    static {
        allocate(4096);
    }

    private static void allocate(int vertices) {
        glBytes = ByteBuffer.allocateDirect(vertices * GL_STRIDE).order(ByteOrder.nativeOrder());
        glBuffer = glBytes.asFloatBuffer();
    }

    /**
     * Beta has no lightmap texture: lighting is a per-vertex brightness multiplied into the colour, exactly like
     * vanilla's {@code glColor(brightness)} before drawing a model. AzureLib keeps 1.18-style packed light values
     * (block and sky level, 0..15) and folds the brighter of the two into the vertex colour here.
     */
    private static float brightness(int packedLight) {
        int level = Math.max(LightTexture.block(packedLight), LightTexture.sky(packedLight));
        return Math.max(0, Math.min(15, level)) / 15.0F;
    }

    private static void upload(Batch batch, int colorOverride) {
        RenderType renderType = batch.renderType;
        boolean fullBright = renderType.isEmissive() || colorOverride != -1;
        int count = batch.vertexCount;
        if (glBuffer.capacity() < count * GL_FLOATS_PER_VERTEX) {
            allocate(Math.max(count, glBuffer.capacity() / GL_FLOATS_PER_VERTEX * 2));
        }
        FloatBuffer out = glBuffer;
        out.clear();
        float[] f = batch.floats;
        int[] n = batch.ints;
        boolean anyWhite = false;
        for (int v = 0; v < count; v++) {
            int fi = v * FLOATS_PER_VERTEX;
            int ni = v * INTS_PER_VERTEX;
            int argb = colorOverride != -1 ? colorOverride : applyHurtTint(n[ni], n[ni + 1]);
            float light = fullBright ? 1.0F : brightness(n[ni + 2]);
            out.put(f[fi]).put(f[fi + 1]).put(f[fi + 2]);
            out.put(f[fi + 3]).put(f[fi + 4]);
            out.put(f[fi + 5]).put(f[fi + 6]).put(f[fi + 7]);
            out.put((argb >> 16 & 0xFF) / 255.0F * light)
                .put((argb >> 8 & 0xFF) / 255.0F * light)
                .put((argb & 0xFF) / 255.0F * light)
                .put((argb >>> 24) / 255.0F);
            anyWhite |= colorOverride == -1 && whiteOverlay(n[ni + 1]) > 0;
        }

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        try {
            bindPointers(out);
            GL11.glDrawArrays(renderType.glMode(), 0, count);

            if (anyWhite) {
                drawWhiteOverlay(batch, out, renderType);
            }
        } finally {
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
            // Leave the current colour white so vanilla code drawing after us isn't tinted.
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /**
     * Points GL at the interleaved data in {@link #glBytes}. Offsets are in bytes: position, then uv at float 3, normal
     * at float 5 and colour at float 8.
     */
    private static void bindPointers(FloatBuffer out) {
        ByteBuffer bytes = glBytes;
        bytes.limit(out.limit() * 4);
        bytes.position(0);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, GL_STRIDE, bytes);
        bytes.position(3 * 4);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, GL_STRIDE, bytes);
        bytes.position(5 * 4);
        GL11.glNormalPointer(GL11.GL_FLOAT, GL_STRIDE, bytes);
        bytes.position(8 * 4);
        GL11.glColorPointer(4, GL11.GL_FLOAT, GL_STRIDE, bytes);
        bytes.position(0);
    }

    private static void drawWhiteOverlay(Batch batch, FloatBuffer out, RenderType renderType) {
        int[] n = batch.ints;
        int count = batch.vertexCount;
        for (int v = 0; v < count; v++) {
            int ni = v * INTS_PER_VERTEX;
            float white = whiteOverlay(n[ni + 1]) * ((n[ni] >>> 24) / 255.0F);
            int base = v * GL_FLOATS_PER_VERTEX + 8;
            out.put(base, 1.0F).put(base + 1, 1.0F).put(base + 2, 1.0F).put(base + 3, white);
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
            bindPointers(out);
            GL11.glDrawArrays(renderType.glMode(), 0, count);
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

    private static final class Batch implements VertexConsumer {

        private final AzBufferSource owner;

        private final RenderType renderType;

        private float[] floats = new float[FLOATS_PER_VERTEX * 256];

        private int[] ints = new int[INTS_PER_VERTEX * 256];

        private int vertexCount;

        private boolean queued;

        // Element-by-element vertex state
        private float x, y, z, u, v, nx, ny, nz;

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
        }

        private void ensureCapacity() {
            if ((this.vertexCount + 1) * FLOATS_PER_VERTEX > this.floats.length) {
                int newVertices = Math.max(256, this.vertexCount * 2);
                this.floats = Arrays.copyOf(this.floats, newVertices * FLOATS_PER_VERTEX);
                this.ints = Arrays.copyOf(this.ints, newVertices * INTS_PER_VERTEX);
            }
            if (!this.queued) {
                this.queued = true;
                this.owner.pending.add(this);
            }
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
            int fi = this.vertexCount * FLOATS_PER_VERTEX;
            int ni = this.vertexCount * INTS_PER_VERTEX;
            this.floats[fi] = px;
            this.floats[fi + 1] = py;
            this.floats[fi + 2] = pz;
            this.floats[fi + 3] = tu;
            this.floats[fi + 4] = tv;
            float lenSq = normalX * normalX + normalY * normalY + normalZ * normalZ;
            if (lenSq > 1.0E-12F && Float.isFinite(lenSq)) {
                float inv = 1.0F / (float) Math.sqrt(lenSq);
                normalX *= inv;
                normalY *= inv;
                normalZ *= inv;
            } else {
                normalX = 0.0F;
                normalY = 1.0F;
                normalZ = 0.0F;
            }
            this.floats[fi + 5] = normalX;
            this.floats[fi + 6] = normalY;
            this.floats[fi + 7] = normalZ;
            this.ints[ni] = argb;
            this.ints[ni + 1] = overlayUV;
            this.ints[ni + 2] = lightmapUV;
            this.vertexCount++;
        }

        private static int clampColor(float value) {
            return Math.max(0, Math.min(255, (int) (value * 255.0F)));
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
            int argb = clampColor(alpha) << 24 | clampColor(red) << 16 | clampColor(green) << 8 | clampColor(blue);
            put(x, y, z, argb, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
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
