package mod.azure.azurelib.render.vertex;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The 1.12.2 implementation of {@link MultiBufferSource}.
 * <p>
 * Vertices handed to the consumers returned by {@link #getBuffer(RenderType)} are recorded per render type, in the
 * order the render types were first used, and are uploaded through the {@link Tessellator} when {@link #endBatch()} is
 * called. This keeps 1.18's batching semantics: AzureLib's renderers may freely switch between render types mid-model
 * (bone texture overrides, glow layers, glint, leashes) without fighting over the single 1.12.2 {@link BufferBuilder}.
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
     * may touch the GL context on 1.12.2.
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

    /** Same two-pass texture-matrix animation as 1.12.2's {@code RenderItem#renderEffect}. */
    private static void drawItemGlint(Batch batch) {
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        for (int pass = 0; pass < 2; pass++) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(8.0F, 8.0F, 8.0F);
            long period = pass == 0 ? 3000L : 4873L;
            float offset = (float) (Minecraft.getSystemTime() % period) / (float) period / 8.0F;
            GlStateManager.translate(pass == 0 ? offset : -offset, 0.0F, 0.0F);
            GlStateManager.rotate(pass == 0 ? -50.0F : 10.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            upload(batch, 0xFF8040CC);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
        }
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    /** Same two-pass texture-matrix animation as 1.12.2's {@code LayerArmorBase#renderEnchantedGlint}. */
    private static void drawArmorGlint(Batch batch) {
        Minecraft mc = Minecraft.getMinecraft();
        float time = (mc.player == null ? 0 : mc.player.ticksExisted) + mc.getRenderPartialTicks();
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

    private static void upload(Batch batch, int colorOverride) {
        RenderType renderType = batch.renderType;
        boolean fullBright = renderType.isEmissive();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(renderType.glMode(), renderType.format());
        float[] f = batch.floats;
        int[] n = batch.ints;
        for (int v = 0; v < batch.vertexCount; v++) {
            int fi = v * FLOATS_PER_VERTEX;
            int ni = v * INTS_PER_VERTEX;
            int argb = colorOverride != -1 ? colorOverride : applyOverlay(n[ni], n[ni + 1]);
            int light = fullBright ? LightTexture.FULL_BRIGHT : n[ni + 2];
            builder.pos(f[fi], f[fi + 1], f[fi + 2]);
            builder.color(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24);
            builder.tex(f[fi + 3], f[fi + 4]);
            builder.lightmap(light >> 16 & 0xFFFF, light & 0xFFFF);
            builder.normal(f[fi + 5], f[fi + 6], f[fi + 7]);
            builder.endVertex();
        }
        tessellator.draw();
    }

    /**
     * Folds 1.18's overlay texture into the vertex colour: a red tint while hurt and a white flash driven by the
     * overlay's U coordinate, matching the colours of the 1.18 overlay texture.
     */
    private static int applyOverlay(int argb, int overlay) {
        if (overlay == OverlayTexture.NO_OVERLAY) {
            return argb;
        }
        int u = overlay & 0xFFFF;
        int v = overlay >> 16 & 0xFFFF;
        float r = (argb >> 16 & 0xFF) / 255.0F;
        float g = (argb >> 8 & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        int a = argb >>> 24;
        if (v < 8) {
            float keep = 0.69803923F;
            r = r * keep + (1.0F - keep);
            g = g * keep;
            b = b * keep;
        }
        if (u > 0) {
            float white = Math.min(u, 15) / 15.0F;
            r = r + (1.0F - r) * white;
            g = g + (1.0F - g) * white;
            b = b + (1.0F - b) * white;
        }
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
