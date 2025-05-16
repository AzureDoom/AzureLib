package mod.azure.azurelib.rewrite.render.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzRendererPipeline;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;

/**
 * A {@link AzRenderLayer} responsible for rendering {@link BlockState BlockStates} or {@link ItemStack ItemStacks} onto
 * a specified {@link AzRendererPipeline}. This layer handles the rendering of physical elements, such as blocks and
 * items, associated with animation bones.
 */
public class AzBlockAndItemLayer<T> implements AzRenderLayer<T> {

    protected final Function<AzBone, ItemStack> itemStackProvider;

    protected final Function<AzBone, BlockState> blockStateProvider;

    public AzBlockAndItemLayer() {
        this(bone -> null, bone -> null);
    }

    public AzBlockAndItemLayer(
        Function<AzBone, ItemStack> itemStackProvider,
        Function<AzBone, BlockState> blockStateProvider
    ) {
        super();

        this.itemStackProvider = itemStackProvider;
        this.blockStateProvider = blockStateProvider;
    }

    @Override
    public void preRender(AzRendererPipelineContext<T> context) {}

    @Override
    public void render(AzRendererPipelineContext<T> context) {}

    /**
     * Renders an {@link ItemStack} or {@link BlockState} associated with the specified bone in the rendering context.
     * If both the {@link ItemStack} and {@link BlockState} are {@code null}, no rendering occurs.
     * <p>
     * This method applies the bone's transformations to the current rendering matrix stack before rendering, ensuring
     * the item or block appears correctly positioned and oriented relative to the bone.
     * </p>
     *
     * @param context the rendering pipeline context, containing rendering state and utilities
     * @param bone    the bone for which to render associated elements
     */
    @Override
    public void renderForBone(AzRendererPipelineContext<T> context, AzBone bone) {
        T animatable = context.animatable();
        ItemStack stack = itemStackForBone(bone, animatable);
        BlockState blockState = blockStateForBone(bone, animatable);

        if (stack == null && blockState == null)
            return;

        context.poseStack().pushPose();
        RenderUtils.translateAndRotateMatrixForBone(context.poseStack(), bone);

        if (stack != null)
            renderItemForBone(context, bone, stack, animatable);

        if (blockState != null)
            renderBlockForBone(context, bone, blockState, animatable);

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
     * Retrieves the {@link BlockState} associated with the given bone for rendering purposes. Returns {@code null} if
     * there is no {@link BlockState} to render for this bone.
     *
     * @param bone the bone for which to retrieve the {@link BlockState}
     * @return the {@link BlockState} relevant to the specified bone, or {@code null} if none exists
     */
    public BlockState blockStateForBone(AzBone bone, T animatable) {
        return blockStateProvider.apply(bone);
    }

    /**
     * Determines the specific {@link ItemTransforms.TransformType} to use for rendering the given {@link ItemStack} on
     * the specified bone. By default, this method returns {@link ItemTransforms.TransformType#NONE}.
     *
     * @param bone  the bone where the {@link ItemStack} will be rendered
     * @param stack the {@link ItemStack} to render
     * @return the {@link ItemTransforms.TransformType} to use for rendering
     */
    protected ItemTransforms.TransformType getTransformTypeForStack(AzBone bone, ItemStack stack, T animatable) {
        return ItemTransforms.TransformType.NONE;
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
        AzRendererPipelineContext<T> context,
        AzBone bone,
        ItemStack itemStack,
        T animatable
    ) {
        if (context.animatable() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) context.animatable();
            Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                    livingEntity,
                    itemStack,
                    getTransformTypeForStack(bone, itemStack, animatable),
                    false,
                    context.poseStack(),
                    context.multiBufferSource(),
                    livingEntity.level,
                    context.packedLight(),
                    context.packedOverlay()
                );
        } else {
            Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                    itemStack,
                    getTransformTypeForStack(bone, itemStack, animatable),
                    context.packedLight(),
                    context.packedOverlay(),
                    context.poseStack(),
                    context.multiBufferSource()
                );
        }
    }

    /**
     * Renders the given {@link BlockState} for the specified bone in the rendering context. The block is rendered with
     * adjusted position and scale to fit within the bone's space.
     *
     * @param context    the rendering pipeline context
     * @param bone       the bone where the {@link BlockState} will be rendered
     * @param blockState the {@link BlockState} to render
     */
    protected void renderBlockForBone(
        AzRendererPipelineContext<T> context,
        AzBone bone,
        BlockState blockState,
        T animatable
    ) {
        context.poseStack().pushPose();

        context.poseStack().translate(-0.25f, -0.25f, -0.25f);
        context.poseStack().scale(0.5f, 0.5f, 0.5f);

        Minecraft.getInstance()
            .getBlockRenderer()
            .renderSingleBlock(
                blockState,
                context.poseStack(),
                context.multiBufferSource(),
                context.packedLight(),
                OverlayTexture.NO_OVERLAY
            );

        context.poseStack().popPose();
    }

}
