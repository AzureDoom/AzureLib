package mod.azure.azurelib.render.layer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * A {@link AzRenderLayer} responsible for rendering {@link IBlockState BlockStates} or {@link ItemStack ItemStacks}
 * onto a specified {@link AzRendererPipeline}. This layer handles the rendering of physical elements, such as blocks
 * and items, associated with animation bones.
 */
public class AzBlockAndItemLayer<K, T> implements AzRenderLayer<K, T> {

    protected final Function<AzBone, ItemStack> itemStackProvider;

    protected final Function<AzBone, IBlockState> blockStateProvider;

    public AzBlockAndItemLayer() {
        this(bone -> null, bone -> null);
    }

    public AzBlockAndItemLayer(
        Function<AzBone, ItemStack> itemStackProvider,
        Function<AzBone, IBlockState> blockStateProvider
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
     * Renders an {@link ItemStack} or {@link IBlockState} associated with the specified bone in the rendering context.
     * If both the {@link ItemStack} and {@link IBlockState} are {@code null}, no rendering occurs.
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
        IBlockState blockState = blockStateForBone(bone, animatable);

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
     * Retrieves the {@link IBlockState} associated with the given bone for rendering purposes. Returns {@code null} if
     * there is no {@link IBlockState} to render for this bone.
     *
     * @param bone the bone for which to retrieve the {@link IBlockState}
     * @return the {@link IBlockState} relevant to the specified bone, or {@code null} if none exists
     */
    public IBlockState blockStateForBone(AzBone bone, T animatable) {
        return blockStateProvider.apply(bone);
    }

    /**
     * Determines the specific {@link ItemCameraTransforms.TransformType} to use for rendering the given
     * {@link ItemStack} on the specified bone. By default, this method returns
     * {@link ItemCameraTransforms.TransformType#NONE}.
     *
     * @param bone  the bone where the {@link ItemStack} will be rendered
     * @param stack the {@link ItemStack} to render
     * @return the {@link ItemCameraTransforms.TransformType} to use for rendering
     */
    protected ItemCameraTransforms.TransformType getTransformTypeForStack(AzBone bone, ItemStack stack, T animatable) {
        return ItemCameraTransforms.TransformType.NONE;
    }

    /**
     * Renders the given {@link ItemStack} for the specified bone in the rendering context. The rendering adjusts based
     * on whether the animatable object is a {@link EntityLivingBase}.
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
        beginVanillaRender(context);

        if (context.animatable() instanceof EntityLivingBase) {
            Minecraft.getMinecraft()
                .getRenderItem()
                .renderItem(
                    itemStack,
                    (EntityLivingBase) context.animatable(),
                    getTransformTypeForStack(bone, itemStack, animatable),
                    false
                );
        } else {
            Minecraft.getMinecraft()
                .getRenderItem()
                .renderItem(itemStack, getTransformTypeForStack(bone, itemStack, animatable));
        }

        GlStateManager.popMatrix();
    }

    /**
     * Renders the given {@link IBlockState} for the specified bone in the rendering context. The block is rendered with
     * adjusted position and scale to fit within the bone's space.
     *
     * @param context    the rendering pipeline context
     * @param bone       the bone where the {@link IBlockState} will be rendered
     * @param blockState the {@link IBlockState} to render
     */
    protected void renderBlockForBone(
        AzRendererPipelineContext<K, T> context,
        AzBone bone,
        IBlockState blockState,
        T animatable
    ) {
        context.poseStack().pushPose();
        context.poseStack().translate(-0.25f, -0.25f, -0.25f);
        context.poseStack().scale(0.5f, 0.5f, 0.5f);

        beginVanillaRender(context);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(blockState, 1.0F);
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
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, packedLight & 0xFFFF, packedLight >> 16);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(context.poseStack().last());
    }

}
