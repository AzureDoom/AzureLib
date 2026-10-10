package mod.azure.azurelib.render.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.render.vertex.VertexConsumer;
import mod.azure.azurelib.util.AzItemIds;
import mod.azure.azurelib.util.client.AzRenderTick;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Base item renderer for AzureLib-animated items. Register instances with
 * {@link AzItemRendererRegistry#register(net.minecraft.item.Item, java.util.function.Supplier)} on the client; AzureLib
 * registers a Forge {@code IItemRenderer} for the item and renders it in every context. Display transforms are read
 * from the item's Blockbench-exported model json ({@code assets/<modid>/models/item/<name>.json}), as on modern
 * versions.
 */
public abstract class AzItemRenderer {

    private final AzItemRendererConfig config;

    private final AzProvider<UUID, ItemStack> provider;

    public final AzItemRendererPipeline rendererPipeline;

    @Nullable
    private AzItemAnimator reusedAzItemAnimator;

    protected AzItemRenderer(AzItemRendererConfig config) {
        this.rendererPipeline = createPipeline(config);
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation, AzItemRenderer::getStackId);
        this.config = config;
    }

    /**
     * The stack's AzureLib identity, or a random id for stacks that don't carry one.
     */
    static UUID getStackId(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag != null && AzItemIds.has(tag)) {
            return AzItemIds.get(tag);
        }

        return UUID.randomUUID();
    }

    protected AzItemRendererPipeline createPipeline(AzItemRendererConfig config) {
        return new AzItemRendererPipeline(config, this);
    }

    /**
     * Entry point used by {@link AzItemRendererAdapter}. The GL matrix already holds the base and display transforms
     * and the {@code -0.5} block-centering offset, which is the space modern versions hand to {@code renderByItem}.
     */
    public void render(ItemStack stack, AzItemDisplayContext transformType) {
        AzBufferSource bufferSource = AzBufferSource.getInstance();
        bufferSource.endBatch();

        PoseStack poseStack = new PoseStack();
        int packedLight = RenderUtils.currentPackedLight();

        if (transformType == AzItemDisplayContext.GUI) {
            renderByGui(stack, transformType, poseStack, bufferSource, packedLight);
        } else {
            renderByItem(stack, transformType, poseStack, bufferSource, packedLight);
        }

        bufferSource.endBatch();
    }

    /**
     * Maps the GL space Forge 1.7.10 hands an {@link net.minecraftforge.client.IItemRenderer} for the context to the
     * space 1.12.2's {@code RenderItem} is in right before it applies a model's {@code display} transform, so the
     * Blockbench display settings place the item the same way on both versions. Override to adjust a context.
     */
    public void applyBaseTransform(AzItemDisplayContext context, AzItemDisplayTransforms transforms, ItemStack stack) {
        switch (context) {
            case GUI:
                // Forge: origin at the slot's top-left corner, in pixels, Y down.
                // 1.12.2: translate(8, 8), scale(1, -1, 1), scale(16).
                GlStateManager.translate(8.0F, 8.0F, 0.0F);
                GlStateManager.scale(16.0F, -16.0F, 16.0F);
                break;
            case GROUND:
                // Forge has bobbed and spun the item and scaled flat custom items by 0.5.
                // 1.12.2 raises dropped items by a quarter of the ground scale.
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                GlStateManager.translate(0.0F, 0.25F * transforms.scaleY(context), 0.0F);
                break;
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                // 1.7.10's ItemRenderer applies the same hand translation as 1.12.2, plus rotate(45, Y) and scale(0.4);
                // Forge's EQUIPPED_BLOCK helper then offsets by -0.5. Undo those (the swing rotations stay).
                GlStateManager.translate(0.5F, 0.5F, 0.5F);
                GlStateManager.scale(2.5F, 2.5F, 2.5F);
                GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
                break;
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
                if (stack.getItem().isFull3D()) {
                    undoFull3DThirdPerson(stack);
                    break;
                }

                // 1.7.10 (RenderPlayer/RenderBiped, flat item) after arm.postRender:
                // T(-1/16, 7/16, 1/16) T(0.25, 0.1875, -0.1875) S(0.375) Rz(60) Rx(-90) Rz(20), then Forge's T(-0.5)
                // 1.12.2 (LayerHeldItem) after arm.postRender:
                // Rx(-90) Ry(180) T(1/16, 0.125, -0.625)
                // Undo the first chain in reverse, then apply the second.
                GlStateManager.translate(0.5F, 0.5F, 0.5F);
                GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-60.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.scale(1.0F / 0.375F, 1.0F / 0.375F, 1.0F / 0.375F);
                GlStateManager.translate(-0.25F, -0.1875F, 0.1875F);
                GlStateManager.translate(0.0625F, -0.4375F, -0.0625F);
                GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0625F, 0.125F, -0.625F);
                break;
            default:
                break;
        }
    }

    /**
     * Third person for items whose {@code Item#isFull3D()} is true (swords, tools, anything that overrides it). 1.7.10
     * holds those differently from flat items, after arm.postRender: T(-1/16, 7/16, 1/16) [Rz(180) T(0, -0.125, 0) if
     * shouldRotateAroundWhenRendering] T(0, 0.1875, 0) S(0.625, -0.625, 0.625) Rx(-100) Ry(45), then Forge's T(-0.5).
     * Undo it and apply 1.12.2's LayerHeldItem transform. (The extra pose while a player is blocking with a sword isn't
     * undone.)
     */
    protected void undoFull3DThirdPerson(ItemStack stack) {
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(100.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(1.0F / 0.625F, -1.0F / 0.625F, 1.0F / 0.625F);
        GlStateManager.translate(0.0F, -0.1875F, 0.0F);

        if (stack.getItem().shouldRotateAroundWhenRendering()) {
            GlStateManager.translate(0.0F, 0.125F, 0.0F);
            GlStateManager.rotate(-180.0F, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.translate(0.0625F, -0.4375F, -0.0625F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0625F, 0.125F, -0.625F);
    }

    public void renderByGui(
        ItemStack stack,
        AzItemDisplayContext transformType,
        @Nonnull PoseStack poseStack,
        @Nonnull MultiBufferSource source,
        int packedLight
    ) {
        AzItemRendererPipelineContext itemContext = (AzItemRendererPipelineContext) rendererPipeline.context();
        AzBakedModel model = provider.provideBakedModel(itemContext.currentEntity(), stack);

        itemContext.setTransformType(transformType);
        prepareAnimator(stack, model);
        AzItemGuiRenderUtil.renderInGui(config, rendererPipeline, stack, model, stack, poseStack, source, packedLight);
    }

    public void renderByItem(
        ItemStack stack,
        AzItemDisplayContext transformType,
        @Nonnull PoseStack poseStack,
        @Nonnull MultiBufferSource source,
        int packedLight
    ) {
        AzItemRendererPipelineContext itemContext = (AzItemRendererPipelineContext) rendererPipeline.context();
        AzBakedModel model = provider.provideBakedModel(itemContext.currentEntity(), stack);
        float partialTick = AzRenderTick.partialTicks();
        RenderType renderType = itemContext.getDefaultRenderType(
            stack,
            config.textureLocation(itemContext.currentEntity(), stack),
            source,
            partialTick,
            config.getRenderType(itemContext.currentEntity(), stack),
            config.alpha(stack)
        );
        boolean withGlint = stack != null && stack.hasEffect(0);
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
