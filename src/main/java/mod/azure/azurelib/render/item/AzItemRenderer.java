package mod.azure.azurelib.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.cache.AzItemIdentity;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.render.vertex.VertexConsumer;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Base item renderer for AzureLib-animated items. Register instances with
 * {@link AzItemRendererRegistry#register(net.minecraft.item.Item, java.util.function.Supplier)} on the client; AzureLib
 * then takes over rendering of that item wherever 1.12.2 renders it. The item's model json should use
 * {@code "parent": "builtin/entity"} so the display transforms are applied, just like on modern versions.
 */
public abstract class AzItemRenderer {

    private final AzItemRendererConfig config;

    private final AzProvider<UUID, ItemStack> provider;

    public final AzItemRendererPipeline rendererPipeline;

    @Nullable
    private AzItemAnimator reusedAzItemAnimator;

    protected AzItemRenderer(AzItemRendererConfig config) {
        this.rendererPipeline = createPipeline(config);
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation, AzItemIdentity::resolve);
        this.config = config;
    }

    protected AzItemRendererPipeline createPipeline(AzItemRendererConfig config) {
        return new AzItemRendererPipeline(config, this);
    }

    /**
     * Entry point used by AzureLib's {@code RenderItem} hook. The GL matrix already holds vanilla's item transforms
     * (including the {@code -0.5} block-centering offset), which is the space modern versions hand to
     * {@code renderByItem}.
     */
    public void render(ItemStack stack, ItemCameraTransforms.TransformType transformType) {
        AzBufferSource bufferSource = AzBufferSource.getInstance();
        bufferSource.endBatch();

        PoseStack poseStack = new PoseStack();
        int packedLight = RenderUtils.currentPackedLight();

        if (transformType == ItemCameraTransforms.TransformType.GUI) {
            renderByGui(stack, transformType, poseStack, bufferSource, packedLight);
        } else {
            renderByItem(stack, transformType, poseStack, bufferSource, packedLight);
        }

        bufferSource.endBatch();
    }

    public void renderByGui(
        ItemStack stack,
        ItemCameraTransforms.TransformType transformType,
        @Nonnull PoseStack poseStack,
        @Nonnull MultiBufferSource source,
        int packedLight
    ) {
        // Everything below that looks up the stack's animator (provider, layers, model renderer) resolves to the
        // animator for this display context's view.
        AzItemAnimatorView previousView = AzItemAnimatorView.push(config.animatorView(transformType));
        ItemCameraTransforms.TransformType previousDisplay = AzMolangQueryContext.INSTANCE.setItemDisplayContext(
            transformType
        );

        try {
            AzItemRendererPipelineContext itemContext = (AzItemRendererPipelineContext) rendererPipeline.context();
            AzBakedModel model = provider.provideBakedModel(itemContext.currentEntity(), stack);

            itemContext.setTransformType(transformType);

            prepareAnimator(stack, model);

            AzItemGuiRenderUtil.renderInGui(
                config,
                rendererPipeline,
                stack,
                model,
                stack,
                poseStack,
                source,
                packedLight
            );
        } finally {
            AzMolangQueryContext.INSTANCE.setItemDisplayContext(previousDisplay);
            AzItemAnimatorView.restore(previousView);
        }
    }

    public void renderByItem(
        ItemStack stack,
        ItemCameraTransforms.TransformType transformType,
        @Nonnull PoseStack poseStack,
        @Nonnull MultiBufferSource source,
        int packedLight
    ) {
        // Everything below that looks up the stack's animator (provider, layers, model renderer) resolves to the
        // animator for this display context's view.
        AzItemAnimatorView previousView = AzItemAnimatorView.push(config.animatorView(transformType));
        // Lets Molang answer query.is_first_person for the item's own view.
        ItemCameraTransforms.TransformType previousDisplay = AzMolangQueryContext.INSTANCE.setItemDisplayContext(
            transformType
        );

        try {
            renderByItemInView(stack, transformType, poseStack, source, packedLight);
        } finally {
            AzMolangQueryContext.INSTANCE.setItemDisplayContext(previousDisplay);
            AzItemAnimatorView.restore(previousView);
        }
    }

    private void renderByItemInView(
        ItemStack stack,
        ItemCameraTransforms.TransformType transformType,
        @Nonnull PoseStack poseStack,
        @Nonnull MultiBufferSource source,
        int packedLight
    ) {
        AzItemRendererPipelineContext itemContext = (AzItemRendererPipelineContext) rendererPipeline.context();
        AzBakedModel model = provider.provideBakedModel(itemContext.currentEntity(), stack);
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        RenderType renderType = itemContext.getDefaultRenderType(
            stack,
            config.textureLocation(itemContext.currentEntity(), stack),
            source,
            partialTick,
            config.getRenderType(itemContext.currentEntity(), stack),
            config.alpha(stack)
        );
        boolean withGlint = stack != null && stack.hasEffect();
        VertexConsumer buffer = AzBufferSource.getFoilBuffer(source, renderType, withGlint);

        itemContext.setTransformType(transformType);
        prepareAnimator(stack, model);
        rendererPipeline.render(poseStack, model, stack, source, renderType, buffer, 0, partialTick, packedLight);
    }

    private void prepareAnimator(ItemStack stack, AzBakedModel model) {
        reusedAzItemAnimator = (AzItemAnimator) provider.provideAnimator(
            rendererPipeline.context().currentEntity(),
            stack
        );
    }

    public @Nullable AzItemAnimator getAnimator() {
        return reusedAzItemAnimator;
    }

    public AzItemRendererConfig config() {
        return config;
    }
}
