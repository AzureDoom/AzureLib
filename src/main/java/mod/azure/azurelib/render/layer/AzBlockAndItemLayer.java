package mod.azure.azurelib.render.layer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemDisplayContext;
import mod.azure.azurelib.render.item.AzItemDisplayTransforms;
import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.LightTexture;
import mod.azure.azurelib.util.AzBlockState;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * A {@link AzRenderLayer} responsible for rendering {@link AzBlockState blocks} or {@link ItemStack ItemStacks} onto a
 * specified {@link AzRendererPipeline}. This layer handles the rendering of physical elements, such as blocks and
 * items, associated with animation bones.
 */
public class AzBlockAndItemLayer<K, T> implements AzRenderLayer<K, T> {

    protected final Function<AzBone, ItemStack> itemStackProvider;

    protected final Function<AzBone, AzBlockState> blockStateProvider;

    public AzBlockAndItemLayer() {
        this(bone -> null, bone -> null);
    }

    public AzBlockAndItemLayer(
        Function<AzBone, ItemStack> itemStackProvider,
        Function<AzBone, AzBlockState> blockStateProvider
    ) {
        super();

        this.itemStackProvider = itemStackProvider;
        this.blockStateProvider = blockStateProvider;
    }

    @Override
    public void preRender(AzRendererPipelineContext<K, T> context) {}

    @Override
    public void render(AzRendererPipelineContext<K, T> context) {}

    /**
     * Renders an {@link ItemStack} or {@link AzBlockState} associated with the specified bone in the rendering context.
     * If both the {@link ItemStack} and {@link AzBlockState} are {@code null}, no rendering occurs.
     * <p>
     * This method applies the bone's transformations to the current rendering matrix stack before rendering, ensuring
     * the item or block appears correctly positioned and oriented relative to the bone.
     * </p>
     *
     * @param context the rendering pipeline context, containing rendering state and utilities
     * @param bone    the bone for which to render associated elements
     */
    @Override
    public void renderForBone(AzRendererPipelineContext<K, T> context, AzBone bone) {
        T animatable = context.animatable();
        ItemStack stack = itemStackForBone(bone, animatable);
        AzBlockState blockState = blockStateForBone(bone, animatable);

        if (stack == null && blockState == null)
            return;

        context.poseStack().pushPose();
        RenderUtils.translateAndRotateMatrixForBone(context.poseStack(), bone);

        if (stack != null)
            renderItemForBone(context, bone, stack, animatable);

        if (blockState != null)
            renderBlockForBone(context, bone, blockState, animatable);

        context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));

        context.poseStack().popPose();
    }

    /**
     * Retrieves the {@link ItemStack} associated with the given bone for rendering purposes. Returns {@code null} if
     * there is no {@link ItemStack} to render for this bone.
     *
     * @param bone the bone for which to retrieve the {@link ItemStack}
     * @return the {@link ItemStack} relevant to the specified bone, or {@code null} if none exists
     */
    public ItemStack itemStackForBone(AzBone bone, T animatable) {
        return itemStackProvider.apply(bone);
    }

    /**
     * Retrieves the {@link AzBlockState} associated with the given bone for rendering purposes. Returns {@code null} if
     * there is no {@link AzBlockState} to render for this bone.
     *
     * @param bone the bone for which to retrieve the {@link AzBlockState}
     * @return the {@link AzBlockState} relevant to the specified bone, or {@code null} if none exists
     */
    public AzBlockState blockStateForBone(AzBone bone, T animatable) {
        return blockStateProvider.apply(bone);
    }

    /**
     * Determines the specific {@link AzItemDisplayContext} to use for rendering the given {@link ItemStack} on the
     * specified bone. By default, this method returns {@link AzItemDisplayContext#NONE}.
     *
     * @param bone  the bone where the {@link ItemStack} will be rendered
     * @param stack the {@link ItemStack} to render
     * @return the {@link AzItemDisplayContext} to use for rendering
     */
    protected AzItemDisplayContext getTransformTypeForStack(AzBone bone, ItemStack stack, T animatable) {
        return AzItemDisplayContext.NONE;
    }

    /**
     * Renders the given {@link ItemStack} for the specified bone in the rendering context. The rendering adjusts based
     * on whether the animatable object is a {@link LivingEntity}.
     *
     * @param context   the rendering pipeline context
     * @param bone      the bone where the {@link ItemStack} will be rendered
     * @param itemStack the {@link ItemStack} to render
     */
    protected void renderItemForBone(
        AzRendererPipelineContext<K, T> context,
        AzBone bone,
        ItemStack itemStack,
        T animatable
    ) {
        if (!(animatable instanceof LivingEntity)) {
            return;
        }

        beginVanillaRender(context);

        try {
            AzItemRenderer renderer =
                AzItemRendererRegistry.getOrNull(itemStack.getItem());

            if (renderer != null) {
                AzItemDisplayContext displayContext =
                    getTransformTypeForStack(bone, itemStack, animatable);

                AzItemDisplayTransforms.forItem(itemStack.getItem())
                    .apply(displayContext);

                GlStateManager.translate(-0.5F, -0.5F, -0.5F);

                renderer.render(itemStack, displayContext);
            } else {
                applyVanillaHandTransforms(bone, itemStack, animatable);
                McClient.renderVanillaItem(
                    animatable instanceof LivingEntity ? (LivingEntity) animatable : null,
                    itemStack
                );
            }
        } finally {
            GlStateManager.popMatrix();
        }
    }

    /**
     * Positions a vanilla (non-AzureLib) item at the bone the way Beta's biped renderer positions a held item at the
     * hand: blocks as small tilted cubes, tools and swords diagonally at 0.625 scale, everything else as a 0.375 scale
     * sprite. Override to adjust for models whose hand bones are oriented differently.
     */
    protected void applyVanillaHandTransforms(AzBone bone, ItemStack stack, T animatable) {
        if (McClient.isBlockItem3D(stack)) {
            applyVanillaBlockInHandTransforms();
        } else if (Mc.isHandheld(Mc.item(stack))) {
            float scale = 0.625F;
            GlStateManager.translate(0.0F, 0.1875F, 0.0F);
            GlStateManager.scale(scale, -scale, scale);
            GlStateManager.rotate(-100.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        } else {
            float scale = 0.375F;
            GlStateManager.translate(0.25F, 0.1875F, -0.1875F);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(20.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    /**
     * Held blocks are drawn by StationAPI's Arsenic renderer, not Beta's: StationAPI gives block items JSON models, and
     * for those Arsenic applies its own third-person hand placement and the model's {@code thirdperson_righthand}
     * display transform (that's where the player's held-block size comes from). Beta's biped block placement must
     * therefore not be applied on top, or the block ends up scaled twice (0.375 x 0.375) and too small.
     * <p>
     * What Arsenic does expect is Beta's model space at the hand, where +X and +Y point the opposite way to AzureLib's
     * Bedrock-style bone space. A 180 degree turn about Z converts one into the other (a rotation rather than a mirror,
     * so faces keep their winding and the block isn't drawn upside down or inside out).
     */
    protected void applyVanillaBlockInHandTransforms() {
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
    }

    protected void renderBlockForBone(
        AzRendererPipelineContext<K, T> context,
        AzBone bone,
        AzBlockState blockState,
        T animatable
    ) {
        context.poseStack().pushPose();
        context.poseStack().translate(-0.25f, -0.25f, -0.25f);
        context.poseStack().scale(0.5f, 0.5f, 0.5f);

        beginVanillaRender(context);
        // Beta's held-item renderer draws block items as 3D blocks (binding terrain.png itself).
        McClient.renderVanillaItem(
            animatable instanceof LivingEntity ? (LivingEntity) animatable : null,
            new ItemStack(Mc.blockId(blockState.getBlock()), 1, blockState.getMeta())
        );
        GlStateManager.popMatrix();

        context.poseStack().popPose();
    }

    /**
     * Flushes AzureLib's pending vertices, applies the context's light and pushes the current bone pose onto the GL
     * matrix stack so vanilla's item and block renderers draw in the right place. Callers must pop the GL matrix.
     */
    protected void beginVanillaRender(AzRendererPipelineContext<K, T> context) {
        if (context.multiBufferSource() instanceof AzBufferSource) {
            ((AzBufferSource) context.multiBufferSource()).endBatch();
        }

        int packedLight = context.packedLight();
        float light = Math.max(LightTexture.block(packedLight), LightTexture.sky(packedLight)) / 15.0F;
        GlStateManager.color(light, light, light, 1.0F);
        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(context.poseStack().last());
    }

}
