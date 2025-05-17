/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayersContainer;
import mod.azure.azurelib.util.RenderUtils;

/**
 * Base {@link GeoRenderer} class for rendering {@link Item Items} specifically.<br>
 * All items added to be rendered by AzureLib should use an instance of this class.
 */
@Deprecated()
public class GeoItemRenderer<T extends Item & GeoAnimatable> extends ItemStackTileEntityRenderer implements GeoRenderer<T> {

    protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);

    protected final GeoModel<T> model;

    protected ItemStack currentItemStack;

    protected T animatable;

    protected float scaleWidth = 1;

    protected float scaleHeight = 1;

    protected Matrix4f itemRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public GeoItemRenderer(GeoModel<T> model) {
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
     * Returns the current ItemStack being rendered
     */
    public ItemStack getCurrentItemStack() {
        return this.currentItemStack;
    }

    /**
     * Gets the id that represents the current animatable's instance for animation purposes. This is mostly useful for
     * things like items, which have a single registered instance for all objects
     */
    @Override
    public long getInstanceId(T animatable) {
        return GeoItem.getId(this.currentItemStack);
    }

    /**
     * Shadowing override of {@link EntityRenderer#getEntityTexture(Entity)}.<br>
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
    public GeoItemRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    /**
     * Sets a scale override for this renderer, telling AzureLib to pre-scale the model
     */
    public GeoItemRenderer<T> withScale(float scale) {
        return withScale(scale, scale);
    }

    /**
     * Sets a scale override for this renderer, telling AzureLib to pre-scale the model
     */
    public GeoItemRenderer<T> withScale(float scaleWidth, float scaleHeight) {
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;

        return this;
    }

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link MatrixStack } translations made here are kept until the end of the render process
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
        this.itemRenderTranslations = new Matrix4f(poseStack.getLast().getMatrix());

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

        if (!isReRender)
            poseStack.translate(0.5f, this.useNewOffset() ? 0.0f : 0.51f, 0.5f);
    }

    @Override
    public void render(
        ItemStack stack,
        MatrixStack poseStack,
        IRenderTypeBuffer bufferSource,
        int packedLight,
        int packedOverlay
    ) {
        this.animatable = (T) stack.getItem();
        this.currentItemStack = stack;
        RenderType renderType = getRenderType(
            this.animatable,
            getTextureLocation(this.animatable),
            bufferSource,
            Minecraft.getInstance().getRenderPartialTicks()
        );
        IVertexBuilder buffer = ItemRenderer.getBuffer(
            bufferSource,
            renderType,
            false,
            this.currentItemStack != null && this.currentItemStack.hasEffect()
        );
        defaultRender(
            poseStack,
            this.animatable,
            bufferSource,
            renderType,
            buffer,
            0,
            Minecraft.getInstance().getRenderPartialTicks(),
            packedLight
        );
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
        if (!isReRender) {
            AnimationState<T> animationState = new AnimationState<>(animatable, 0, 0, partialTick, false);
            long instanceId = getInstanceId(animatable);

            animationState.setData(DataTickets.TICK, animatable.getTick(this.currentItemStack));
            animationState.setData(DataTickets.ITEMSTACK, this.currentItemStack);
            this.model.addAdditionalStateData(animatable, instanceId, animationState::setData);
            this.model.handleAnimations(animatable, instanceId, animationState);
        }

        this.modelRenderTranslations = new Matrix4f(poseStack.getLast().getMatrix());

        // RenderSystem.setShaderTexture(0, getTextureLocation(animatable));
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
            Matrix4f poseState = new Matrix4f(poseStack.getLast().getMatrix());

            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            bone.setLocalSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.itemRenderTranslations));
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

    /**
     * Determines whether to apply the y offset for a model due to the change in BlockBench 4.11.
     *
     * @return {@code false} by default, meaning the Y-offset will be {@code 0.51f}. Override this method or change the
     *         return value to {@code true} to use the new Y-offset of {@code 0.0f} for anything created in 4.11+ of
     *         Blockbench.
     */
    public boolean useNewOffset() {
        return false;
    }
}
