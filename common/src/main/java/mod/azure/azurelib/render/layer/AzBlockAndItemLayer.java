package mod.azure.azurelib.render.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * A {@link AzRenderLayer} responsible for rendering {@link net.minecraft.world.level.block.state.BlockState
 * BlockStates} or {@link net.minecraft.world.item.ItemStack ItemStacks} onto a specified {@link AzRendererPipeline}.
 * This layer handles the rendering of physical elements, such as blocks and items, associated with animation bones.
 * <p>
 * 26.2 removed the old immediate-mode {@code ItemRenderer#renderStatic}/{@code BlockRenderer#renderSingleBlock} entry
 * points in favour of resolver + render-state + submit: an {@code ItemModelResolver} populates an
 * {@code ItemStackRenderState}, which is then handed a {@code SubmitNodeCollector} later. Since this layer runs eagerly
 * (mid bone-tree walk) rather than at an actual submit callback, the submission itself is deferred via
 * {@link mod.azure.azurelib.render.AzBufferSource#defer} until the surrounding pipeline's own {@code AzBufferSource} is
 * replayed.
 * <p>
 * {@link BlockState} bones are rendered by wrapping the block in an {@link net.minecraft.world.item.ItemStack} and
 * reusing the same item pipeline, rather than resolving a {@code BlockModelRenderState} directly. Unlike
 * {@code ItemModelResolver}, {@code BlockModelResolver} is only ever handed to renderers at construction time (via
 * {@code EntityRendererProvider.Context}/{@code BlockEntityRendererProvider.Context}) — {@link Minecraft} keeps it as a
 * constructor-local value with no field or getter, so it can't be reached generically from here the way
 * {@link Minecraft#getItemModelResolver()} can. This means directional/multipart/other blockstate-dependent visuals
 * won't be reflected (block items render whatever variant their item model resolves to, not the exact BlockState passed
 * in) — acceptable for simple decorative blocks, but if you need exact per-BlockState fidelity you'll need to thread a
 * real BlockModelResolver through your renderer's construction instead.
 */
public class AzBlockAndItemLayer<K, T> implements AzRenderLayer<K, T> {

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
    public void preRender(AzRendererPipelineContext<K, T> context) {}

    @Override
    public void render(AzRendererPipelineContext<K, T> context) {}

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
    public void renderForBone(AzRendererPipelineContext<K, T> context, AzBone bone) {
        var animatable = context.animatable();
        var stack = itemStackForBone(bone, animatable);
        var blockState = blockStateForBone(bone, animatable);

        if (stack == null && blockState == null)
            return;

        context.poseStack().pushPose();
        RenderUtils.translateAndRotateMatrixForBone(context.poseStack(), bone);

        if (stack != null)
            renderItemForBone(context, bone, stack, animatable);

        if (blockState != null)
            renderItemForBone(context, bone, new ItemStack(blockState.getBlock()), animatable);

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
     * Determines the specific {@link ItemDisplayContext} to use for rendering the given {@link ItemStack} on the
     * specified bone. By default, this method returns {@link ItemDisplayContext#NONE}.
     *
     * @param bone  the bone where the {@link ItemStack} will be rendered
     * @param stack the {@link ItemStack} to render
     * @return the {@link ItemDisplayContext} to use for rendering
     */
    protected ItemDisplayContext getTransformTypeForStack(AzBone bone, ItemStack stack, T animatable) {
        return ItemDisplayContext.NONE;
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
        var minecraft = Minecraft.getInstance();
        var resolver = minecraft.getItemModelResolver();
        var displayContext = getTransformTypeForStack(bone, itemStack, animatable);
        var renderState = new ItemStackRenderState();

        if (context.animatable() instanceof LivingEntity livingEntity) {
            resolver.updateForLiving(renderState, itemStack, displayContext, livingEntity);
        } else if (context.currentEntity() != null) {
            resolver.updateForNonLiving(renderState, itemStack, displayContext, context.currentEntity());
        } else if (minecraft.level != null) {
            var owner = animatable instanceof ItemOwner itemOwner ? itemOwner : null;
            var seed = animatable != null ? animatable.hashCode() : 0;

            resolver.updateForTopItem(renderState, itemStack, displayContext, minecraft.level, owner, seed);
        } else {
            return;
        }

        if (renderState.isEmpty()) {
            return;
        }

        var packedLight = context.packedLight();
        var packedOverlay = context.packedOverlay();
        var outlineColor = 0;

        context.multiBufferSource()
            .defer(
                context.poseStack(),
                (poseStack, collector) -> renderState
                    .submit(poseStack, collector, packedLight, packedOverlay, outlineColor)
            );
    }

}
