package mod.azure.azurelib.render.vertex;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * A description of how a batch of vertices is drawn, mirroring the factory methods of the 1.18
 * {@code net.minecraft.client.renderer.RenderType} that AzureLib uses.
 * <p>
 * Every factory is memoized per texture, so - exactly as on modern versions - two calls with the same texture return
 * the same instance and render types can be compared with {@code ==}.
 * <p>
 * On 1.12.2 a render type translates into fixed-function GL state, applied by {@link #setupRenderState()} and undone by
 * {@link #clearRenderState()} around each draw issued by {@link AzBufferSource}.
 */
public final class RenderType {

    /**
     * Position, colour, texture, lightmap and normal - the 1.12.2 equivalent of 1.18's {@code NEW_ENTITY} format (the
     * overlay is folded into the colour by {@link AzBufferSource}).
     */
    public static final VertexFormat ENTITY_FORMAT = new VertexFormat().addElement(DefaultVertexFormats.POSITION_3F)
        .addElement(DefaultVertexFormats.COLOR_4UB)
        .addElement(DefaultVertexFormats.TEX_2F)
        .addElement(DefaultVertexFormats.TEX_2S)
        .addElement(DefaultVertexFormats.NORMAL_3B)
        .addElement(DefaultVertexFormats.PADDING_1B);

    private static final ResourceLocation ENCHANTED_ITEM_GLINT = new ResourceLocation(
        "textures/misc/enchanted_item_glint.png"
    );

    private static final Map<ResourceLocation, RenderType> ENTITY_SOLID = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> ENTITY_CUTOUT = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> ENTITY_CUTOUT_NO_CULL = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> ENTITY_TRANSLUCENT = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_CULL = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> ITEM_ENTITY_TRANSLUCENT_CULL = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> ARMOR_CUTOUT_NO_CULL = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> OUTLINE = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> EYES = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> EMISSIVE = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> EMISSIVE_OUTLINE = new ConcurrentHashMap<>();

    private static final RenderType LEASH = new RenderType(
        "leash",
        null,
        GL11.GL_TRIANGLE_STRIP,
        // 1.12.2's VertexFormat can't hold a lightmap UV (index 1) without a texture UV (index 0) before it, so the
        // leash uses the entity format too; with no texture bound the UVs are simply ignored.
        ENTITY_FORMAT,
        false,
        false,
        false,
        false,
        false,
        GlintMode.NONE
    );

    private static final RenderType ENTITY_GLINT = new RenderType(
        "entity_glint",
        ENCHANTED_ITEM_GLINT,
        GL11.GL_QUADS,
        ENTITY_FORMAT,
        false,
        true,
        false,
        false,
        false,
        GlintMode.ITEM
    );

    private static final RenderType ARMOR_ENTITY_GLINT = new RenderType(
        "armor_entity_glint",
        ENCHANTED_ITEM_GLINT,
        GL11.GL_QUADS,
        ENTITY_FORMAT,
        false,
        true,
        false,
        false,
        false,
        GlintMode.ARMOR
    );

    private final String name;

    @Nullable
    private final ResourceLocation texture;

    private final int glMode;

    private final VertexFormat format;

    private final boolean cull;

    private final boolean translucent;

    private final boolean emissive;

    private final boolean diffuseLighting;

    private final boolean writeDepth;

    private final GlintMode glint;

    private boolean restoreLighting;

    private RenderType(
        String name,
        @Nullable ResourceLocation texture,
        int glMode,
        VertexFormat format,
        boolean cull,
        boolean translucent,
        boolean emissive,
        boolean diffuseLighting,
        boolean writeDepth,
        GlintMode glint
    ) {
        this.name = name;
        this.texture = texture;
        this.glMode = glMode;
        this.format = format;
        this.cull = cull;
        this.translucent = translucent;
        this.emissive = emissive;
        this.diffuseLighting = diffuseLighting;
        this.writeDepth = writeDepth;
        this.glint = glint;
    }

    private static RenderType entityType(
        String name,
        ResourceLocation texture,
        boolean cull,
        boolean translucent,
        boolean emissive,
        boolean diffuseLighting
    ) {
        return new RenderType(
            name,
            texture,
            GL11.GL_QUADS,
            ENTITY_FORMAT,
            cull,
            translucent,
            emissive,
            diffuseLighting,
            true,
            GlintMode.NONE
        );
    }

    private static RenderType memoize(
        Map<ResourceLocation, RenderType> cache,
        ResourceLocation texture,
        Function<ResourceLocation, RenderType> factory
    ) {
        Objects.requireNonNull(texture, "texture");
        RenderType type = cache.get(texture);
        if (type == null) {
            type = factory.apply(texture);
            RenderType existing = cache.putIfAbsent(texture, type);
            if (existing != null) {
                type = existing;
            }
        }
        return type;
    }

    public static RenderType entitySolid(ResourceLocation texture) {
        return memoize(ENTITY_SOLID, texture, t -> entityType("entity_solid", t, true, false, false, true));
    }

    public static RenderType entityCutout(ResourceLocation texture) {
        return memoize(ENTITY_CUTOUT, texture, t -> entityType("entity_cutout", t, true, false, false, true));
    }

    public static RenderType entityCutoutNoCull(ResourceLocation texture) {
        return memoize(
            ENTITY_CUTOUT_NO_CULL,
            texture,
            t -> entityType("entity_cutout_no_cull", t, false, false, false, true)
        );
    }

    public static RenderType entityTranslucent(ResourceLocation texture) {
        return memoize(
            ENTITY_TRANSLUCENT,
            texture,
            t -> entityType("entity_translucent", t, false, true, false, true)
        );
    }

    public static RenderType entityTranslucentCull(ResourceLocation texture) {
        return memoize(
            ENTITY_TRANSLUCENT_CULL,
            texture,
            t -> entityType("entity_translucent_cull", t, true, true, false, true)
        );
    }

    public static RenderType itemEntityTranslucentCull(ResourceLocation texture) {
        return memoize(
            ITEM_ENTITY_TRANSLUCENT_CULL,
            texture,
            t -> entityType("item_entity_translucent_cull", t, true, true, false, true)
        );
    }

    public static RenderType armorCutoutNoCull(ResourceLocation texture) {
        return memoize(
            ARMOR_CUTOUT_NO_CULL,
            texture,
            t -> entityType("armor_cutout_no_cull", t, false, false, false, true)
        );
    }

    /**
     * 1.12.2 draws the spectral "glowing" outline through its own entity-outline pass, so this behaves like
     * {@link #entityCutoutNoCull(ResourceLocation)}.
     */
    public static RenderType outline(ResourceLocation texture) {
        return memoize(OUTLINE, texture, t -> entityType("outline", t, false, false, false, true));
    }

    /** Full-bright, unlit and additive, like vanilla's eye layers. */
    public static RenderType eyes(ResourceLocation texture) {
        return memoize(EYES, texture, t -> entityType("eyes", t, false, true, true, false));
    }

    /**
     * Full-bright, unlit and alpha-blended. This is the render type AzureLib uses for glowmask layers.
     */
    public static RenderType emissive(ResourceLocation texture, boolean outline) {
        return memoize(
            outline ? EMISSIVE_OUTLINE : EMISSIVE,
            texture,
            t -> entityType(outline ? "az_glowing_layer_outline" : "az_glowing_layer", t, false, true, true, false)
        );
    }

    public static RenderType leash() {
        return LEASH;
    }

    public static RenderType entityGlint() {
        return ENTITY_GLINT;
    }

    public static RenderType armorEntityGlint() {
        return ARMOR_ENTITY_GLINT;
    }

    public String name() {
        return this.name;
    }

    @Nullable
    public ResourceLocation texture() {
        return this.texture;
    }

    public int glMode() {
        return this.glMode;
    }

    public VertexFormat format() {
        return this.format;
    }

    public boolean isTranslucent() {
        return this.translucent;
    }

    public boolean isEmissive() {
        return this.emissive;
    }

    public boolean isGlint() {
        return this.glint != GlintMode.NONE;
    }

    GlintMode glintMode() {
        return this.glint;
    }

    public void setupRenderState() {
        if (this.texture != null) {
            GlStateManager.enableTexture2D();
            Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        } else {
            GlStateManager.disableTexture2D();
        }

        if (this.cull) {
            GlStateManager.enableCull();
        } else {
            GlStateManager.disableCull();
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();

        if (this.glint != GlintMode.NONE) {
            GlStateManager.enableBlend();
            GlStateManager.depthMask(false);
            GlStateManager.depthFunc(GL11.GL_EQUAL);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        } else if (this.translucent) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        } else {
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        }

        if (!this.writeDepth && this.glint == GlintMode.NONE) {
            GlStateManager.depthMask(false);
        }

        this.restoreLighting = false;
        if (!this.diffuseLighting && GL11.glIsEnabled(GL11.GL_LIGHTING)) {
            GlStateManager.disableLighting();
            this.restoreLighting = true;
        }
    }

    public void clearRenderState() {
        if (this.restoreLighting) {
            GlStateManager.enableLighting();
            this.restoreLighting = false;
        }
        if (this.glint != GlintMode.NONE) {
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
        }
        if (!this.writeDepth || this.glint != GlintMode.NONE) {
            GlStateManager.depthMask(true);
        }
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
    }

    @Override
    public String toString() {
        return "RenderType[" + this.name + (this.texture == null ? "" : ":" + this.texture) + "]";
    }

    enum GlintMode {
        NONE,
        ITEM,
        ARMOR
    }
}
