package mod.azure.azurelib.renderer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

/**
 * Base interface for all AzureLib renderers.<br>
 */
public interface GeoRenderer<T extends GeoAnimatable> {
	/**
	 * Gets the model instance for this renderer
	 */
	GeoModel<T> getGeoModel();

	/**
	 * Gets the {@link GeoAnimatable} instance currently being rendered
	 */
	T getAnimatable();

	/**
	 * Gets the texture resource location to render for the given animatable
	 */
	default ResourceLocation getTextureLocation(T animatable) {
		return getGeoModel().getTextureResource(animatable);
	}

	/**
	 * Returns the list of registered {@link GeoRenderLayer GeoRenderLayers} for this renderer
	 */
	default List<GeoRenderLayer<T>> getRenderLayers() {
		return new ArrayList<>();
	}

	/**
	 * Gets the {@link RenderType} to render the given animatable with.<br>
	 * Uses the {@link RenderType#entityCutoutNoCull} {@code RenderType} by default.<br>
	 * Override this to change the way a model will render (such as translucent models, etc)
	 */
	default RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable IRenderTypeBuffer bufferSource, float partialTick) {
		return getGeoModel().getRenderType(animatable, texture);
	}

	/**
	 * Gets a tint-applying color to render the given animatable with.<br>
	 * Returns {@link Color#WHITE} by default
	 */
	default Color getRenderColor(T animatable, float partialTick, int packedLight) {
		return Color.WHITE;
	}

	/**
	 * Gets a packed overlay coordinate pair for rendering.<br>
	 * Mostly just used for the red tint when an entity is hurt, but can be used for other things like the {@link net.minecraft.world.entity.monster.Creeper} white tint when exploding.
	 */
	default int getPackedOverlay(T animatable, float u) {
		return OverlayTexture.NO_OVERLAY;
	}

	/**
	 * Gets the id that represents the current animatable's instance for animation purposes. This is mostly useful for things like items, which have a single registered instance for all objects
	 */
	default long getInstanceId(T animatable) {
		return animatable.hashCode();
	}

	/**
	 * Determines the threshold value before the animatable should be considered moving for animation purposes.<br>
	 * The default value and usage for this varies depending on the renderer.<br>
	 * <ul>
	 * <li>For entities, it represents the averaged lateral velocity of the object.</li>
	 * <li>For {@link mod.azure.azurelib.animatable.GeoBlockEntity Tile Entities} and {@link mod.azure.azurelib.animatable.GeoItem Items}, it's currently unused</li>
	 * </ul>
	 * The lower the value, the more sensitive the {@link AnimationState#isMoving()} check will be.<br>
	 * Particularly low values may have adverse effects however
	 */
	default float getMotionAnimThreshold(T animatable) {
		return 0.015f;
	}

	/**
	 * Initial access point for rendering. It all begins here.<br>
	 * All AzureLib renderers should immediately defer their respective default {@code render} calls to this, for consistent handling
	 */
	default void defaultRender(MatrixStack poseStack, T animatable, IRenderTypeBuffer bufferSource, @Nullable RenderType renderType, @Nullable IVertexBuilder buffer, float yaw, float partialTick, int packedLight) {
		poseStack.pushPose();

		Color renderColor = getRenderColor(animatable, partialTick, packedLight);
		float red = renderColor.getRed();
		float green = renderColor.getGreen();
		float blue = renderColor.getBlue();
		float alpha = renderColor.getAlpha();
		int packedOverlay = getPackedOverlay(animatable, 0);
		BakedGeoModel model = getGeoModel().getBakedModel(getGeoModel().getModelResource(animatable));

		if (renderType == null)
			renderType = getRenderType(animatable, getTextureLocation(animatable), bufferSource, partialTick);

		if (buffer == null)
			buffer = bufferSource.getBuffer(renderType);

		preRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

		if (firePreRenderEvent(poseStack, model, bufferSource, partialTick, packedLight)) {
			preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, packedLight, packedLight, packedOverlay);
			actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
			applyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
			postRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
			firePostRenderEvent(poseStack, model, bufferSource, partialTick, packedLight);
		}

		poseStack.popPose();

		renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}

	/**
	 * Re-renders the provided {@link BakedGeoModel} using the existing {@link GeoRenderer}.<br>
	 * Usually you'd use this for rendering alternate {@link RenderType} layers or for sub-model rendering whilst inside a {@link GeoRenderLayer} or similar
	 */
	default void reRender(BakedGeoModel model, MatrixStack poseStack, IRenderTypeBuffer bufferSource, T animatable, RenderType renderType, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		preRender(poseStack, animatable, model, bufferSource, buffer, true, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, true, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		postRender(poseStack, animatable, model, bufferSource, buffer, true, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}

	/**
	 * The actual render method that sub-type renderers should override to handle their specific rendering tasks.<br>
	 * {@link GeoRenderer#preRender} has already been called by this stage, and {@link GeoRenderer#postRender} will be called directly after
	 */
	default void actuallyRender(MatrixStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		updateAnimatedTextureFrame(animatable);
		
		for (GeoBone group : model.topLevelBones()) {
			renderRecursively(poseStack, animatable, group, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		}
	}

	/**
	 * Calls back to the various {@link GeoRenderLayer RenderLayers} that have been registered to this renderer for their {@link GeoRenderLayer#preRender pre-render} actions.
	 */
	default void preApplyRenderLayers(MatrixStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay) {
		for (GeoRenderLayer<T> renderLayer : getRenderLayers()) {
			renderLayer.preRender(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
		}
	}

	/**
	 * Calls back to the various {@link GeoRenderLayer RenderLayers} that have been registered to this renderer for their {@link GeoRenderLayer#renderForBone per-bone} render actions.
	 */
	default void applyRenderLayersForBone(MatrixStack poseStack, T animatable, GeoBone bone, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay) {
		for (GeoRenderLayer<T> renderLayer : getRenderLayers()) {
			renderLayer.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
		}
	}

	/**
	 * Render the various {@link GeoRenderLayer RenderLayers} that have been registered to this renderer
	 */
	default void applyRenderLayers(MatrixStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay) {
		for (GeoRenderLayer<T> renderLayer : getRenderLayers()) {
			renderLayer.render(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
		}
	}

	/**
	 * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling and translating.<br>
	 * {@link MatrixStack} translations made here are kept until the end of the render process
	 */
	default void preRender(MatrixStack poseStack, T animatable, BakedGeoModel model, @Nullable IRenderTypeBuffer bufferSource, @Nullable IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
	}

	/**
	 * Called after rendering the model to buffer. Post-render modifications should be performed here.<br>
	 * {@link MatrixStack} transformations will be unused and lost once this method ends
	 */
	default void postRender(MatrixStack poseStack, T animatable, BakedGeoModel model, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
	}

	/**
	 * Call after all other rendering work has taken place, including reverting the {@link MatrixStack}'s state. This method is <u>not</u> called in {@link GeoRenderer#reRender re-render}
	 */
	default void renderFinal(MatrixStack poseStack, T animatable, BakedGeoModel model, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
	}

	/**
	 * Renders the provided {@link GeoBone} and its associated child bones
	 */
	default void renderRecursively(MatrixStack poseStack, T animatable, GeoBone bone, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		RenderUtils.prepMatrixForBone(poseStack, bone);
		renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);

		if (!isReRender)
			applyRenderLayersForBone(poseStack, getAnimatable(), bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

		renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}

	/**
	 * Renders the {@link GeoCube GeoCubes} associated with a given {@link GeoBone}
	 */
	default void renderCubesOfBone(MatrixStack poseStack, GeoBone bone, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (bone.isHidden())
			return;

		for (GeoCube cube : bone.getCubes()) {
			poseStack.pushPose();
			renderCube(poseStack, cube, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			poseStack.popPose();
		}
	}

	/**
	 * Render the child bones of a given {@link GeoBone}.<br>
	 * Note that this does not render the bone itself. That should be done through {@link GeoRenderer#renderCubesOfBone} separately
	 */
	default void renderChildBones(MatrixStack poseStack, T animatable, GeoBone bone, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (bone.isHidingChildren())
			return;

		for (GeoBone childBone : bone.getChildBones()) {
			renderRecursively(poseStack, animatable, childBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		}
	}

	/**
	 * Renders an individual {@link GeoCube}.<br>
	 * This tends to be called recursively from something like {@link GeoRenderer#renderCubesOfBone}
	 */
	default void renderCube(MatrixStack poseStack, GeoCube cube, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		RenderUtils.translateToPivotPoint(poseStack, cube);
		RenderUtils.rotateMatrixAroundCube(poseStack, cube);
		RenderUtils.translateAwayFromPivotPoint(poseStack, cube);

		Matrix3f normalisedPoseState = poseStack.last().normal();
		Matrix4f poseState = new Matrix4f(poseStack.last().pose());

		for (GeoQuad quad : cube.quads()) {
			if (quad == null)
				continue;

			Vector3f normal = quad.normal().copy();

			normal.transform(normalisedPoseState);

			RenderUtils.fixInvertedFlatCube(cube, normal);
			createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		}
	}

	/**
	 * Applies the {@link GeoQuad Quad's} {@link GeoVertex vertices} to the given {@link IVertexBuilder buffer} for rendering
	 */
	default void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		for (GeoVertex vertex : quad.vertices()) {
			Vector4f vector4f = new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1);

			vector4f.transform(poseState);

			buffer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, vertex.texU(), vertex.texV(), packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
		}
	}

	/**
	 * Create and fire the relevant {@code CompileLayers} event hook for this renderer
	 */
	void fireCompileRenderLayersEvent();

	/**
	 * Create and fire the relevant {@code Pre-Render} event hook for this renderer.<br>
	 * 
	 * @return Whether the renderer should proceed based on the cancellation state of the event
	 */
	boolean firePreRenderEvent(MatrixStack poseStack, BakedGeoModel model, IRenderTypeBuffer bufferSource, float partialTick, int packedLight);

	/**
	 * Create and fire the relevant {@code Post-Render} event hook for this renderer
	 */
	void firePostRenderEvent(MatrixStack poseStack, BakedGeoModel model, IRenderTypeBuffer bufferSource, float partialTick, int packedLight);

	/**
	 * Scales the {@link MatrixStack} in preparation for rendering the model, excluding when re-rendering the model as part of a {@link GeoRenderLayer} or external render call.<br>
	 * Override and call super with modified scale values as needed to further modify the scale of the model (E.G. child entities)
	 */
	default void scaleModelForRender(float widthScale, float heightScale, MatrixStack poseStack, T animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
		if (!isReRender && (widthScale != 1 || heightScale != 1))
			poseStack.scale(widthScale, heightScale, widthScale);
	}
	
	/**
	 * Update the current frame of a {@link AnimatableTexture potentially animated} texture used by this GeoRenderer.<br>
	 * This should only be called immediately prior to rendering, and only
	 * @see AnimatableTexture#setAndUpdate(ResourceLocation, int)
	 */
	void updateAnimatedTextureFrame(T animatable);
}
