package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * An abstract base class representing the rendering context for a custom rendering pipeline. This class provides
 * generic rendering properties and behavior that can be extended to customize rendering for different types of
 * animatable objects.
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> the type of the animatable object being rendered
 */
public abstract class AzRendererPipelineContext<K, T> {

    public ResourceLocation textureOverride;

    private final AzRendererPipeline<K, T> rendererPipeline;

    protected T animatable;

    protected @Nullable Entity currentEntity;

    private AzBakedModel bakedModel;

    private MultiBufferSource multiBufferSource;

    private int packedLight;

    private int packedOverlay;

    private float partialTick;

    private PoseStack poseStack;

    private float red;

    private float green;

    private float blue;

    private float alpha;

    private RenderType renderType;

    private VertexConsumer vertexConsumer;

    private boolean applyAnimationOnReRender;

    protected static final Map<ResourceLocation, IntIntPair> TEXTURE_DIMENSIONS_CACHE =
        new Object2ObjectOpenHashMap<>();

    protected AzRendererPipelineContext(AzRendererPipeline<K, T> rendererPipeline) {
        this.rendererPipeline = rendererPipeline;
    }

    /**
     * Populates the rendering context with all necessary parameters required to render a specific animatable object.
     * This method initializes the rendering pipeline with data such as the model, buffer source, lighting, and other
     * associated properties for rendering the specified animatable object.
     *
     * @param animatable        The animatable object that is being rendered.
     * @param bakedModel        The pre-baked 3D model associated with the animatable object.
     * @param multiBufferSource The multibuffer source used for rendering vertex data.
     * @param packedLight       The packed light value for controlling light effects during rendering.
     * @param partialTick       The partial tick value for interpolating animations or movements.
     * @param poseStack         The pose stack used to manage rendering transformations.
     * @param renderType        The render type that determines how the object will be rendered, e.g., opaque,
     *                          translucent, etc.
     * @param vertexConsumer    The vertex consumer used for buffering vertex attributes during rendering.
     */
    public void populate(
        T animatable,
        AzBakedModel bakedModel,
        MultiBufferSource multiBufferSource,
        int packedLight,
        float partialTick,
        PoseStack poseStack,
        RenderType renderType,
        VertexConsumer vertexConsumer
    ) {
        this.animatable = animatable;
        this.bakedModel = bakedModel;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.packedOverlay = getPackedOverlay(animatable, 0, partialTick);
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.renderType = renderType;
        this.vertexConsumer = vertexConsumer;
        Color renderColor = getRenderColor(animatable, partialTick, packedLight);
        this.red = renderColor.getRedFloat();
        this.green = renderColor.getGreenFloat();
        this.blue = renderColor.getBlueFloat();
        this.alpha = renderColor.getAlphaFloat();

        if (renderType == null) {
            var textureLocation = rendererPipeline.config().textureLocation(currentEntity, animatable);
            this.renderType = getDefaultRenderType(
                animatable,
                textureLocation,
                multiBufferSource,
                partialTick,
                rendererPipeline.config().getRenderType(currentEntity, animatable),
                rendererPipeline.config().alpha(animatable)
            );
        } else {
            this.renderType = renderType;
        }

        if (this.vertexConsumer == null && this.renderType != null) {
            this.vertexConsumer = multiBufferSource.getBuffer(this.renderType);
        }
    }

    /**
     * Gets the {@link RenderType} to render the given animatable with.<br>
     * Uses the {@link RenderType#entityCutoutNoCull} {@code RenderType} by default.<br>
     * Override this to change the way a model will render (such as translucent models, etc)
     */
    public abstract RenderType getDefaultRenderType(
        T animatable,
        ResourceLocation texture,
        MultiBufferSource bufferSource,
        float partialTick,
        RenderType defaultRenderType,
        float alpha
    );

    /**
     * Gets a tint-applying color to render the given animatable with.<br>
     * Returns {@link Color#WHITE} by default
     */
    public Color getRenderColor(T animatable, float partialTick, int packedLight) {
        return Color.WHITE;
    }

    /**
     * Gets a packed overlay coordinate pair for rendering.<br>
     * Mostly just used for the red tint when an entity is hurt, but can be used for other things like the
     * {@link net.minecraft.world.entity.monster.Creeper} white tint when exploding.
     */
    protected int getPackedOverlay(T animatable, float u, float partialTick) {
        return OverlayTexture.NO_OVERLAY;
    }

    public AzRendererPipeline<K, T> rendererPipeline() {
        return rendererPipeline;
    }

    public T animatable() {
        return animatable;
    }

    public void setCurrentEntity(Entity currentEntity) {
        this.currentEntity = currentEntity;
    }

    public @Nullable Entity currentEntity() {
        return currentEntity;
    }

    public AzBakedModel bakedModel() {
        return bakedModel;
    }

    public void setBakedModel(@Nullable AzBakedModel bakedModel) {
        this.bakedModel = bakedModel != null ? bakedModel : AzBakedModel.getDefault();
    }

    public boolean applyAnimationOnReRender() {
        return this.applyAnimationOnReRender;
    }

    public void setApplyAnimationOnReRender(boolean applyAnimationOnReRender) {
        this.applyAnimationOnReRender = applyAnimationOnReRender;
    }

    public MultiBufferSource multiBufferSource() {
        return multiBufferSource;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public float alpha() {
        return alpha;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setColor(Color color) {
        this.red = color.getRedFloat();
        this.green = color.getGreenFloat();
        this.blue = color.getBlueFloat();
    }

    public int packedLight() {
        return packedLight;
    }

    public void setPackedLight(int packedLight) {
        this.packedLight = packedLight;
    }

    public int packedOverlay() {
        return packedOverlay;
    }

    public void setPackedOverlay(int packedOverlay) {
        this.packedOverlay = packedOverlay;
    }

    public float partialTick() {
        return partialTick;
    }

    public PoseStack poseStack() {
        return poseStack;
    }

    public RenderType renderType() {
        return renderType;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }

    public VertexConsumer vertexConsumer() {
        return vertexConsumer;
    }

    public void setVertexConsumer(VertexConsumer vertexConsumer) {
        this.vertexConsumer = vertexConsumer;
    }

    /**
     * Sets the texture override for the current rendering context. This can be used to replace the default texture
     * associated with the animatable object being rendered.
     *
     * @param textureOverride the {@link ResourceLocation} of the texture to override; passing null will revert back to
     *                        the default texture
     */
    public void setTextureOverride(ResourceLocation textureOverride) {
        this.textureOverride = textureOverride;
    }

    /**
     * Retrieves the texture override set for this rendering context, if any.
     *
     * @return the {@link ResourceLocation} representing the texture override, or null if no override is set.
     */
    public ResourceLocation getTextureOverride() {
        return textureOverride;
    }

    /**
     * Computes the dimensions of the specified texture and caches the result for future use. This method retrieves the
     * dimensions of the texture represented by the given {@code ResourceLocation} and returns them as an
     * {@code IntIntPair}, where the first value represents the width and the second value represents the height of the
     * texture.
     *
     * @param texture the {@link ResourceLocation} of the texture whose dimensions need to be computed
     * @return an {@link IntIntPair} containing the width and height of the texture
     */
    public IntIntPair computeTextureSize(ResourceLocation texture) {
        return TEXTURE_DIMENSIONS_CACHE.computeIfAbsent(texture, RenderUtils::getTextureDimensions);
    }
}
