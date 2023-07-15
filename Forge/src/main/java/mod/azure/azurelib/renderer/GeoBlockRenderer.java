package mod.azure.azurelib.renderer;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.event.GeoRenderEvent;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayersContainer;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.common.MinecraftForge;

/**
 * Base {@link GeoRenderer} class for rendering {@link TileEntity Blocks} specifically.<br>
 * All blocks added to be rendered by AzureLib should use an instance of this class.
 */
public class GeoBlockRenderer<T extends TileEntity & GeoAnimatable> extends TileEntityRenderer<T> implements GeoRenderer<T> {
	protected final GeoModel<T> model;
	protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);

	protected T animatable;
	protected float scaleWidth = 1;
	protected float scaleHeight = 1;

	protected Matrix4f blockRenderTranslations = new Matrix4f();
	protected Matrix4f modelRenderTranslations = new Matrix4f();

	public GeoBlockRenderer(TileEntityRendererDispatcher rendererDispatcherIn, GeoModel<T> model) {
		super(rendererDispatcherIn);
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
	 * {@link MatrixStack} translations made here are kept until the end of the render process
	 */
	@Override
	public void preRender(MatrixStack poseStack, T animatable, BakedGeoModel model, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.blockRenderTranslations = new Matrix4f(poseStack.last().pose());;

		scaleModelForRender(this.scaleWidth, this.scaleHeight, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
	}

	@Override
	public void render(TileEntity animatable, float partialTick, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight, int packedOverlay) {
		this.animatable = (T) animatable;

		defaultRender(poseStack, this.animatable, bufferSource, null, null, 0, partialTick, packedLight);
	}

	/**
	 * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
	 * {@link GeoRenderer#preRender} has already been called by this stage, and {@link GeoRenderer#postRender} will be called directly after
	 */
	@Override
	public void actuallyRender(MatrixStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
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

//		RenderSystem.setShaderTexture(0, getTextureLocation(animatable));
		GeoRenderer.super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}

	/**
	 * Renders the provided {@link GeoBone} and its associated child bones
	 */
	@Override
	public void renderRecursively(MatrixStack poseStack, T animatable, GeoBone bone, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (bone.isTrackingMatrices()) {
			Matrix4f poseState = new Matrix4f(poseStack.last().pose());
			Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(poseState, this.blockRenderTranslations);
			Matrix4f worldState = localMatrix.copy();
			BlockPos pos = this.animatable.getBlockPos();

			bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
			bone.setLocalSpaceMatrix(localMatrix);
			worldState.translate(new Vector3f(pos.getX(), pos.getY(), pos.getZ()));
			bone.setWorldSpaceMatrix(worldState);
		}

		GeoRenderer.super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}

	/**
	 * Rotate the {@link MatrixStack} based on the determined {@link Direction} the block is facing
	 */
	protected void rotateBlock(Direction facing, MatrixStack poseStack) {
		switch (facing) {
		case SOUTH:
			poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
			return;
		case WEST:
			poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
			return;
		case NORTH:
			poseStack.mulPose(Vector3f.YP.rotationDegrees(0));
			return;
		case EAST: 
			poseStack.mulPose(Vector3f.YP.rotationDegrees(270));
			return;
		case UP:
			poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
			return;
		case DOWN:
			poseStack.mulPose(Vector3f.XN.rotationDegrees(90));
			return;
		}
	}

	/**
	 * Attempt to extract a direction from the block so that the model can be oriented correctly
	 */
	protected Direction getFacing(T block) {
		BlockState blockState = block.getBlockState();

		if (blockState.hasProperty(HorizontalBlock.FACING))
			return blockState.getValue(HorizontalBlock.FACING);

		if (blockState.hasProperty(DirectionalBlock.FACING))
			return blockState.getValue(DirectionalBlock.FACING);

		return Direction.NORTH;
	}
	
	/**
	 * Update the current frame of a {@link AnimatableTexture potentially animated} texture used by this GeoRenderer.<br>
	 * This should only be called immediately prior to rendering, and only
	 * 
	 * @see AnimatableTexture#setAndUpdate(ResourceLocation, int)
	 */
	@Override
	public void updateAnimatedTextureFrame(T animatable) {
		AnimatableTexture.setAndUpdate(getTextureLocation(animatable), animatable.getBlockPos().getX() + animatable.getBlockPos().getY() + animatable.getBlockPos().getZ() + (int)animatable.getTick(animatable));
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
	public boolean firePreRenderEvent(MatrixStack poseStack, BakedGeoModel model, IRenderTypeBuffer bufferSource, float partialTick, int packedLight) {
		return !MinecraftForge.EVENT_BUS.post(new GeoRenderEvent.Block.Pre(this, poseStack, model, bufferSource, partialTick, packedLight));
	}

	/**
	 * Create and fire the relevant {@code Post-Render} event hook for this renderer
	 */
	@Override
	public void firePostRenderEvent(MatrixStack poseStack, BakedGeoModel model, IRenderTypeBuffer bufferSource, float partialTick, int packedLight) {
		MinecraftForge.EVENT_BUS.post(new GeoRenderEvent.Block.Post(this, poseStack, model, bufferSource, partialTick, packedLight));
	}
}
