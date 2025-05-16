/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import java.util.List;

import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayersContainer;
import mod.azure.azurelib.util.RenderUtils;

/**
 * Base {@link GeoRenderer} class for rendering anything that isn't already handled by the other builtin GeoRenderer
 * subclasses.<br>
 * Before using this class you should ensure your use-case isn't already covered by one of the other existing
 * renderers.<br>
 * <br>
 * It is <b>strongly</b> recommended you override {@link GeoRenderer#getInstanceId} if using this renderer
 */
@Deprecated()
public class GeoObjectRenderer<T extends GeoAnimatable> implements GeoRenderer<T> {

    protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);

    protected final GeoModel<T> model;

    protected T animatable;

    protected float scaleWidth = 1;

    protected float scaleHeight = 1;

    protected Matrix4f objectRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public GeoObjectRenderer(GeoModel<T> model) {
        this.model = model;
    }

    /**
     * Gets the model instance for this renderer
     */
    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    /**
     * Gets the {@link GeoAnimatable} instance currently being rendered
     */
    @Override
    public T getAnimatable() {
        return this.animatable;
    }

    /**
     * Shadowing override of {@link EntityRenderer#getTextureLocation}.<br>
     * This redirects the call to {@link GeoRenderer#getTextureLocation}
     */
    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        return GeoRenderer.super.getTextureLocation(animatable);
    }

    /**
     * Returns the list of registered {@link GeoRenderLayer GeoRenderLayers} for this renderer
     */
    @Override
    public List<GeoRenderLayer<T>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    /**
     * Adds a {@link GeoRenderLayer} to this renderer, to be called after the main model is rendered each frame
     */
    public GeoObjectRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    /**
     * Sets a scale override for this renderer, telling AzureLib to pre-scale the model
     */
    public GeoObjectRenderer<T> withScale(float scale) {
        return withScale(scale, scale);
    }

    /**
     * Sets a scale override for this renderer, telling AzureLib to pre-scale the model
     */
    public GeoObjectRenderer<T> withScale(float scaleWidth, float scaleHeight) {
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;

        return this;
    }

    /**
     * The entry render point for this renderer.<br>
     * Call this whenever you want to render your object
     *
     * @param poseStack    The MatrixStack to render under
     * @param animatable   The {@link T} instance to render
     * @param bufferSource The BufferSource to render with, or null to use the default
     * @param renderType   The specific RenderType to use, or null to fall back to {@link GeoRenderer#getRenderType}
     * @param buffer       The VertexConsumer to use for rendering, or null to use the default for the RenderType
     * @param packedLight  The light level at the given render position for rendering
     */
    public void render(
        MatrixStack poseStack,
        T animatable,
        IRenderTypeBuffer bufferSource,
        RenderType renderType,
        IVertexBuilder buffer,
        int packedLight
    ) {
        this.animatable = animatable;
        Minecraft mc = Minecraft.getInstance();

        if (buffer == null)
            bufferSource = mc.levelRenderer.renderBuffers.bufferSource();

        defaultRender(poseStack, animatable, bufferSource, renderType, buffer, 0, mc.getFrameTime(), packedLight);
    }

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link MatrixStack} translations made here are kept until the end of the render process
     */
    @Override
    public void preRender(
        MatrixStack poseStack,
        T animatable,
        BakedGeoModel model,
        IRenderTypeBuffer bufferSource,
        IVertexBuilder buffer,
        boolean isReRender,
        float partialTick,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        this.objectRenderTranslations = new Matrix4f(poseStack.last().pose());

        scaleModelForRender(
            this.scaleWidth,
            this.scaleHeight,
            poseStack,
            animatable,
            model,
            isReRender,
            partialTick,
            packedLight,
            packedOverlay
        );

        poseStack.translate(0.5f, 0.51f, 0.5f);
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     * {@link GeoRenderer#preRender} has already been called by this stage, and {@link GeoRenderer#postRender} will be
     * called directly after
     */
    @Override
    public void actuallyRender(
        MatrixStack poseStack,
        T animatable,
        BakedGeoModel model,
        RenderType renderType,
        IRenderTypeBuffer bufferSource,
        IVertexBuilder buffer,
        boolean isReRender,
        float partialTick,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        poseStack.pushPose();

        if (!isReRender) {
            AnimationState<T> animationState = new AnimationState<>(animatable, 0, 0, partialTick, false);
            long instanceId = getInstanceId(animatable);

            this.model.addAdditionalStateData(animatable, instanceId, animationState::setData);
            this.model.handleAnimations(animatable, instanceId, animationState);
        }

        this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

        GeoRenderer.super.actuallyRender(
            poseStack,
            animatable,
            model,
            renderType,
            bufferSource,
            buffer,
            isReRender,
            partialTick,
            packedLight,
            packedOverlay,
            red,
            green,
            blue,
            alpha
        );
        poseStack.popPose();
    }

    /**
     * Renders the provided {@link GeoBone} and its associated child bones
     */
    @Override
    public void renderRecursively(
        MatrixStack poseStack,
        T animatable,
        GeoBone bone,
        RenderType renderType,
        IRenderTypeBuffer bufferSource,
        IVertexBuilder buffer,
        boolean isReRender,
        float partialTick,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());

            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            bone.setLocalSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.objectRenderTranslations));
        }

        GeoRenderer.super.renderRecursively(
            poseStack,
            animatable,
            bone,
            renderType,
            bufferSource,
            buffer,
            isReRender,
            partialTick,
            packedLight,
            packedOverlay,
            red,
            green,
            blue,
            alpha
        );
    }
}
