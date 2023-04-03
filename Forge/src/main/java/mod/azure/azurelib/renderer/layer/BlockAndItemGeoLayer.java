package mod.azure.azurelib.renderer.layer;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * {@link GeoRenderLayer} for rendering {@link net.minecraft.world.level.block.state.BlockState BlockStates} or {@link net.minecraft.world.item.ItemStack ItemStacks} on a given {@link GeoAnimatable}
 */
public class BlockAndItemGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
	protected final BiFunction<GeoBone, T, ItemStack> stackForBone;
	protected final BiFunction<GeoBone, T, BlockState> blockForBone;

	public BlockAndItemGeoLayer(GeoRenderer<T> renderer) {
		this(renderer, (bone, animatable) -> null, (bone, animatable) -> null);
	}

	public BlockAndItemGeoLayer(GeoRenderer<T> renderer, BiFunction<GeoBone, T, ItemStack> stackForBone, BiFunction<GeoBone, T, BlockState> blockForBone) {
		super(renderer);

		this.stackForBone = stackForBone;
		this.blockForBone = blockForBone;
	}

	/**
	 * Return an ItemStack relevant to this bone for rendering, or null if no ItemStack to render
	 */
	@Nullable
	protected ItemStack getStackForBone(GeoBone bone, T animatable) {
		return this.stackForBone.apply(bone, animatable);
	}

	/**
	 * Return a BlockState relevant to this bone for rendering, or null if no BlockState to render
	 */
	@Nullable
	protected BlockState getBlockForBone(GeoBone bone, T animatable) {
		return this.blockForBone.apply(bone, animatable);
	}

	/**
	 * Return a specific TransFormType for this {@link ItemStack} render for this bone.
	 */
	protected ItemCameraTransforms.TransformType getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
		return ItemCameraTransforms.TransformType.NONE;
	}

	/**
	 * This method is called by the {@link GeoRenderer} for each bone being rendered.<br>
	 * This is a more expensive call, particularly if being used to render something on a different buffer.<br>
	 * It does however have the benefit of having the matrix translations and other transformations already applied from render-time.<br>
	 * It's recommended to avoid using this unless necessary.<br>
	 * <br>
	 * The {@link GeoBone} in question has already been rendered by this stage.<br>
	 * <br>
	 * If you <i>do</i> use it, and you render something that changes the {@link IVertexBuilder buffer}, you need to reset it back to the previous buffer using {@link IRenderTypeBuffer#getBuffer} before ending the method
	 */
	@Override
	public void renderForBone(MatrixStack poseStack, T animatable, GeoBone bone, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay) {
		ItemStack stack = getStackForBone(bone, animatable);
		BlockState blockState = getBlockForBone(bone, animatable);

		if (stack == null && blockState == null)
			return;

		poseStack.pushPose();
		RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);

		if (stack != null)
			renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);

		if (blockState != null)
			renderBlockForBone(poseStack, bone, blockState, animatable, bufferSource, partialTick, packedLight, packedOverlay);

		buffer = bufferSource.getBuffer(renderType);

		poseStack.popPose();
	}

	/**
	 * Render the given {@link ItemStack} for the provided {@link GeoBone}.
	 */
	protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, T animatable, IRenderTypeBuffer bufferSource, float partialTick, int packedLight, int packedOverlay) {
		if (animatable instanceof LivingEntity) {
			Minecraft.getInstance().getItemRenderer().renderStatic(((LivingEntity) animatable), stack, getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, ((LivingEntity) animatable).level, packedLight, packedOverlay);
		} else {
			Minecraft.getInstance().getItemRenderer().renderStatic(stack, getTransformTypeForStack(bone, stack, animatable), packedLight, packedOverlay, poseStack, bufferSource);
		}
	}

	/**
	 * Render the given {@link BlockState} for the provided {@link GeoBone}.
	 */
	protected void renderBlockForBone(MatrixStack poseStack, GeoBone bone, BlockState state, T animatable, IRenderTypeBuffer bufferSource, float partialTick, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.translate(-0.25f, -0.25f, -0.25f);
		poseStack.scale(0.5f, 0.5f, 0.5f);
		Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, bufferSource, packedLight, packedOverlay);
		poseStack.popPose();
	}
}
