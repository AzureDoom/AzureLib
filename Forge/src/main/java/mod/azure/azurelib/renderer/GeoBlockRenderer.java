package mod.azure.azurelib.renderer;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.event.GeoRenderEvent;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayersContainer;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

/**
 * Base {@link GeoRenderer} class for rendering {@link BlockEntity Blocks} specifically.<br>
 * All blocks added to be rendered by AzureLib should use an instance of this class.
 */
public class GeoBlockRenderer<T extends BlockEntity & GeoAnimatable> implements GeoRenderer<T>, BlockEntityRenderer<T> {
	protected final GeoModel<T> model;
	protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);

	protected T animatable;
	protected float scaleWidth = 1;
	protected float scaleHeight = 1;

	protected Matrix4f blockRenderTranslations = new Matrix4f();
	protected Matrix4f modelRenderTranslations = new Matrix4f();

	public GeoBlockRenderer(GeoModel<T> model) {
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
	 * Gets the id that represents the current animatable's instance for animation purposes. This is mostly useful for things like items, which have a single registered instance for all objects
	 */
	@Override
	public long getInstanceId(T animatable) {
		return animatable.getBlockPos().hashCode();
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
	public GeoBlockRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
		this.renderLayers.addLayer(renderLayer);

		return this;
	}

	/**
	 * Sets a scale override for this renderer, telling AzureLib to pre-scale the model
	 */
	public GeoBlockRenderer<T> withScale(float scale) {
		return withScale(scale, scale);
	}

	/**
	 * Sets a scale override for this renderer, telling AzureLib to pre-scale the model
	 */
	public GeoBlockRenderer<T> withScale(float scaleWidth, float scaleHeight) {
		this.scaleWidth = scaleWidth;
		this.scaleHeight = scaleHeight;

		return this;
	}

	/**
	 * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling and translating.<br>
	 * {@link PoseStack} translations made here are kept until the end of the render process
	 */
	@Override
	public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.blockRenderTranslations = new Matrix4f(poseStack.last().pose());;

		scaleModelForRender(this.scaleWidth, this.scaleHeight, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
	}

	@Override
	public void render(BlockEntity animatable, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		this.animatable = (T) animatable;

		defaultRender(poseStack, this.animatable, bufferSource, null, null, 0, partialTick, packedLight);
	}

	/**
	 * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
	 * {@link GeoRenderer#preRender} has already been called by this stage, and {@link GeoRenderer#postRender} will be called directly after
	 */
	@Override
	public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.pushPose();

		if (!isReRender) {
			AnimationState<T> animationState = new AnimationState<T>(animatable, 0, 0, partialTick, false);
			long instanceId = getInstanceId(animatable);

			animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
			animationState.setData(DataTickets.BLOCK_ENTITY, animatable);
			this.model.addAdditionalStateData(animatable, instanceId, animationState::setData);
			poseStack.translate(0, 0.01f, 0);
			poseStack.translate(0.5, 0, 0.5);
			rotateBlock(getFacing(animatable), poseStack);
			this.model.handleAnimations(animatable, instanceId, animationState);
		}

		this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

		RenderSystem.setShaderTexture(0, getTextureLocation(animatable));
		GeoRenderer.super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}

	/**
	 * Renders the provided {@link GeoBone} and its associated child bones
	 */
	@Override
	public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (bone.isTrackingMatrices()) {
			Matrix4f poseState = new Matrix4f(poseStack.last().pose());
			Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(poseState, this.blockRenderTranslations);

			bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
			bone.setLocalSpaceMatrix(RenderUtils.translateMatrix(localMatrix, getRenderOffset(this.animatable, 1).toVector3f()));
			bone.setWorldSpaceMatrix(RenderUtils.translateMatrix(new Matrix4f(localMatrix), new Vector3f(this.animatable.getBlockPos().getX(), this.animatable.getBlockPos().getY(), this.animatable.getBlockPos().getZ())));
		}

		GeoRenderer.super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public Vec3 getRenderOffset(BlockEntity entity, float f) {
		return Vec3.ZERO;
	}

	/**
	 * Rotate the {@link PoseStack} based on the determined {@link Direction} the block is facing
	 */
	protected void rotateBlock(Direction facing, PoseStack poseStack) {
		switch (facing) {
		case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
		case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
		case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
		case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
		case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
		case DOWN -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
		}
	}

	/**
	 * Attempt to extract a direction from the block so that the model can be oriented correctly
	 */
	protected Direction getFacing(T block) {
		BlockState blockState = block.getBlockState();

		if (blockState.hasProperty(HorizontalDirectionalBlock.FACING))
			return blockState.getValue(HorizontalDirectionalBlock.FACING);

		if (blockState.hasProperty(DirectionalBlock.FACING))
			return blockState.getValue(DirectionalBlock.FACING);

		return Direction.NORTH;
	}

	/**
	 * Scales the {@link PoseStack} in preparation for rendering the model, excluding when re-rendering the model as part of a {@link GeoRenderLayer} or external render call.<br>
	 * Override and call super with modified scale values as needed to further modify the scale of the model (E.G. child entities)
	 */
	@Override
	public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, T animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
		if (!isReRender && (widthScale != 1 || heightScale != 1))
			poseStack.scale(this.scaleWidth, this.scaleHeight, this.scaleWidth);
	}

	/**
	 * Create and fire the relevant {@code CompileLayers} event hook for this renderer
	 */
	@Override
	public void fireCompileRenderLayersEvent() {
		MinecraftForge.EVENT_BUS.post(new GeoRenderEvent.Block.CompileRenderLayers(this));
	}

	/**
	 * Create and fire the relevant {@code Pre-Render} event hook for this renderer.<br>
	 * 
	 * @return Whether the renderer should proceed based on the cancellation state of the event
	 */
	@Override
	public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
		return !MinecraftForge.EVENT_BUS.post(new GeoRenderEvent.Block.Pre(this, poseStack, model, bufferSource, partialTick, packedLight));
	}

	/**
	 * Create and fire the relevant {@code Post-Render} event hook for this renderer
	 */
	@Override
	public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
		MinecraftForge.EVENT_BUS.post(new GeoRenderEvent.Block.Post(this, poseStack, model, bufferSource, partialTick, packedLight));
	}
}
