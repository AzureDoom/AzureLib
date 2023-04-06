package mod.azure.azurelib.renderer;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import mod.azure.azurelib.animatable.GeoItem;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.common.MinecraftForge;

/**
 * Base {@link GeoRenderer} for rendering in-world armor specifically.<br>
 * All custom armor added to be rendered in-world by AzureLib should use an instance of this class.
 * 
 * @see GeoItem
 * @param <T>
 */
public class GeoArmorRenderer<T extends Item & GeoItem> extends BipedModel implements GeoRenderer<T> {
	protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);
	protected final GeoModel<T> model;

	protected T animatable;
	protected BipedModel<?> baseModel;
	protected float scaleWidth = 1;
	protected float scaleHeight = 1;

	protected Matrix4f entityRenderTranslations = new Matrix4f();
	protected Matrix4f modelRenderTranslations = new Matrix4f();

	protected BakedGeoModel lastModel = null;
	protected GeoBone head = null;
	protected GeoBone body = null;
	protected GeoBone rightArm = null;
	protected GeoBone leftArm = null;
	protected GeoBone rightLeg = null;
	protected GeoBone leftLeg = null;
	protected GeoBone rightBoot = null;
	protected GeoBone leftBoot = null;

	protected Entity currentEntity = null;
	protected ItemStack currentStack = null;
	protected EquipmentSlotType currentSlot = null;

	public GeoArmorRenderer(GeoModel<T> model) {
		super(1.0f);

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
	 * Gets the {@link GeoItem} instance currently being rendered
	 */
	public T getAnimatable() {
		return this.animatable;
	}

	/**
	 * Returns the entity currently being rendered with armour equipped
	 */
	public Entity getCurrentEntity() {
		return this.currentEntity;
	}

	/**
	 * Returns the ItemStack pertaining to the current piece of armor being rendered
	 */
	public ItemStack getCurrentStack() {
		return this.currentStack;
	}

	/**
	 * Returns the equipped slot of the armor piece being rendered
	 */
	public EquipmentSlotType getCurrentSlot() {
		return this.currentSlot;
	}

	/**
	 * Gets the id that represents the current animatable's instance for animation purposes. This is mostly useful for things like items, which have a single registered instance for all objects
	 */
	@Override
	public long getInstanceId(T animatable) {
		return GeoItem.getId(this.currentStack) + this.currentEntity.getId();
	}

	/**
	 * Gets the {@link RenderType} to render the given animatable with.<br>
	 * Uses the {@link RenderType#armorCutoutNoCull} {@code RenderType} by default.<br>
	 * Override this to change the way a model will render (such as translucent models, etc)
	 */
	@Override
	public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable IRenderTypeBuffer bufferSource, float partialTick) {
		return RenderType.armorCutoutNoCull(texture);
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
	public GeoArmorRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
		this.renderLayers.addLayer(renderLayer);

		return this;
	}

	/**
	 * Sets a scale override for this renderer, telling AzureLib to pre-scale the model
	 */
	public GeoArmorRenderer<T> withScale(float scale) {
		return withScale(scale, scale);
	}

	/**
	 * Sets a scale override for this renderer, telling AzureLib to pre-scale the model
	 */
	public GeoArmorRenderer<T> withScale(float scaleWidth, float scaleHeight) {
		this.scaleWidth = scaleWidth;
		this.scaleHeight = scaleHeight;

		return this;
	}

	/**
	 * Returns the 'head' GeoBone from this model.<br>
	 * Override if your geo model has different bone names for these bones
	 * 
	 * @return The bone for the head model piece, or null if not using it
	 */
	@Nullable
	public GeoBone getHeadBone() {
		return this.model.getBone("armorHead").orElse(null);
	}

	/**
	 * Returns the 'body' GeoBone from this model.<br>
	 * Override if your geo model has different bone names for these bones
	 * 
	 * @return The bone for the body model piece, or null if not using it
	 */
	@Nullable
	public GeoBone getBodyBone() {
		return this.model.getBone("armorBody").orElse(null);
	}

	/**
	 * Returns the 'right arm' GeoBone from this model.<br>
	 * Override if your geo model has different bone names for these bones
	 * 
	 * @return The bone for the right arm model piece, or null if not using it
	 */
	@Nullable
	public GeoBone getRightArmBone() {
		return this.model.getBone("armorRightArm").orElse(null);
	}

	/**
	 * Returns the 'left arm' GeoBone from this model.<br>
	 * Override if your geo model has different bone names for these bones
	 * 
	 * @return The bone for the left arm model piece, or null if not using it
	 */
	@Nullable
	public GeoBone getLeftArmBone() {
		return this.model.getBone("armorLeftArm").orElse(null);
	}

	/**
	 * Returns the 'right leg' GeoBone from this model.<br>
	 * Override if your geo model has different bone names for these bones
	 * 
	 * @return The bone for the right leg model piece, or null if not using it
	 */
	@Nullable
	public GeoBone getRightLegBone() {
		return this.model.getBone("armorRightLeg").orElse(null);
	}

	/**
	 * Returns the 'left leg' GeoBone from this model.<br>
	 * Override if your geo model has different bone names for these bones
	 * 
	 * @return The bone for the left leg model piece, or null if not using it
	 */
	@Nullable
	public GeoBone getLeftLegBone() {
		return this.model.getBone("armorLeftLeg").orElse(null);
	}

	/**
	 * Returns the 'right boot' GeoBone from this model.<br>
	 * Override if your geo model has different bone names for these bones
	 * 
	 * @return The bone for the right boot model piece, or null if not using it
	 */
	@Nullable
	public GeoBone getRightBootBone() {
		return this.model.getBone("armorRightBoot").orElse(null);
	}

	/**
	 * Returns the 'left boot' GeoBone from this model.<br>
	 * Override if your geo model has different bone names for these bones
	 * 
	 * @return The bone for the left boot model piece, or null if not using it
	 */
	@Nullable
	public GeoBone getLeftBootBone() {
		return this.model.getBone("armorLeftBoot").orElse(null);
	}

	/**
	 * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling and translating.<br>
	 * {@link MatrixStack} translations made here are kept until the end of the render process
	 */
	@Override
	public void preRender(MatrixStack poseStack, T animatable, BakedGeoModel model, @Nullable IRenderTypeBuffer bufferSource, @Nullable IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.entityRenderTranslations = new Matrix4f(poseStack.last().pose());

		applyBaseModel(this.baseModel);
		grabRelevantBones(getGeoModel().getBakedModel(getGeoModel().getModelResource(this.animatable)));
		applyBaseTransformations(this.baseModel);
		scaleModelForBaby(poseStack, animatable, partialTick, isReRender);
		scaleModelForRender(this.scaleWidth, this.scaleHeight, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);

		if (!(this.currentEntity instanceof GeoAnimatable))
			applyBoneVisibilityBySlot(this.currentSlot);
	}

	@Override
	public void renderToBuffer(MatrixStack poseStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Minecraft mc = Minecraft.getInstance();
		IRenderTypeBuffer bufferSource = mc.renderBuffers().bufferSource();

		if (mc.levelRenderer.shouldShowEntityOutlines() && mc.shouldEntityAppearGlowing(this.currentEntity))
			bufferSource = mc.renderBuffers().outlineBufferSource();

		float partialTick = mc.getFrameTime();
		RenderType renderType = getRenderType(this.animatable, getTextureLocation(this.animatable), bufferSource, partialTick);
		buffer = ItemRenderer.getArmorFoilBuffer(bufferSource, renderType, false, this.currentStack.hasFoil());

		defaultRender(poseStack, this.animatable, bufferSource, null, buffer, 0, partialTick, packedLight);
	}

	/**
	 * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
	 * {@link GeoRenderer#preRender} has already been called by this stage, and {@link GeoRenderer#postRender} will be called directly after
	 */
	@Override
	public void actuallyRender(MatrixStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		poseStack.translate(0, 24 / 16f, 0);
		poseStack.scale(-1, -1, 1);

		if (!isReRender) {
			AnimationState<T> animationState = new AnimationState<>(animatable, 0, 0, partialTick, false);
			long instanceId = getInstanceId(animatable);

			animationState.setData(DataTickets.TICK, animatable.getTick(this.currentEntity));
			animationState.setData(DataTickets.ITEMSTACK, this.currentStack);
			animationState.setData(DataTickets.ENTITY, this.currentEntity);
			animationState.setData(DataTickets.EQUIPMENT_SLOT, this.currentSlot);
			this.model.addAdditionalStateData(animatable, instanceId, animationState::setData);
			this.model.handleAnimations(animatable, instanceId, animationState);
		}

		this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

		GeoRenderer.super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}

	/**
	 * Renders the provided {@link GeoBone} and its associated child bones
	 */
	@Override
	public void renderRecursively(MatrixStack poseStack, T animatable, GeoBone bone, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (bone.isTrackingMatrices()) {
			Matrix4f poseState = new Matrix4f(poseStack.last().pose());;

			bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
			bone.setLocalSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.entityRenderTranslations));
		}

		GeoRenderer.super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}

	/**
	 * Gets and caches the relevant armor model bones for this baked model if it hasn't been done already
	 */
	protected void grabRelevantBones(BakedGeoModel bakedModel) {
		if (this.lastModel == bakedModel)
			return;

		this.lastModel = bakedModel;
		this.head = getHeadBone();
		this.body = getBodyBone();
		this.rightArm = getRightArmBone();
		this.leftArm = getLeftArmBone();
		this.rightLeg = getRightLegBone();
		this.leftLeg = getLeftLegBone();
		this.rightBoot = getRightBootBone();
		this.leftBoot = getLeftBootBone();
	}

	/**
	 * Prepare the renderer for the current render cycle.<br>
	 * Must be called prior to render as the default BipedModel doesn't give render context.<br>
	 * Params have been left nullable so that the renderer can be called for model/texture purposes safely. If you do grab the renderer using null parameters, you should not use it for actual rendering.
	 * 
	 * @param entity    The entity being rendered with the armor on
	 * @param stack     The ItemStack being rendered
	 * @param slot      The slot being rendered
	 * @param baseModel The default (vanilla) model that would have been rendered if this model hadn't replaced it
	 */
	public void prepForRender(@Nullable Entity entity, ItemStack stack, @Nullable EquipmentSlotType slot, @Nullable BipedModel<?> baseModel) {
		if (entity == null || slot == null || baseModel == null)
			return;

		this.baseModel = baseModel;
		this.currentEntity = entity;
		this.currentStack = stack;
		this.animatable = (T) stack.getItem();
		this.currentSlot = slot;
	}

	/**
	 * Applies settings and transformations pre-render based on the default model
	 */
	protected void applyBaseModel(BipedModel<?> baseModel) {
		this.young = baseModel.young;
		this.crouching = baseModel.crouching;
		this.riding = baseModel.riding;
		this.rightArmPose = baseModel.rightArmPose;
		this.leftArmPose = baseModel.leftArmPose;
	}

	/**
	 * Resets the bone visibility for the model based on the currently rendering slot, and then sets bones relevant to the current slot as visible for rendering.<br>
	 * <br>
	 * This is only called by default for non-geo entities (I.E. players or vanilla mobs)
	 */
	protected void applyBoneVisibilityBySlot(EquipmentSlotType currentSlot) {
		setAllVisible(false);

		switch (currentSlot) {
		case HEAD:
			setBoneVisible(this.head, true);
			return;
		case CHEST:
			setBoneVisible(this.body, true);
			setBoneVisible(this.rightArm, true);
			setBoneVisible(this.leftArm, true);

			return;
		case LEGS:
			setBoneVisible(this.rightLeg, true);
			setBoneVisible(this.leftLeg, true);

			return;
		case FEET:
			setBoneVisible(this.rightBoot, true);
			setBoneVisible(this.leftBoot, true);

			return;
		default:
			return;
		}
	}

	/**
	 * Resets the bone visibility for the model based on the current {@link ModelRenderer} and {@link EquipmentSlotType}, and then sets the bones relevant to the current part as visible for rendering.<br>
	 * <br>
	 * If you are rendering a geo entity with armor, you should probably be calling this prior to rendering
	 */
	public void applyBoneVisibilityByPart(EquipmentSlotType currentSlot, ModelRenderer currentPart, BipedModel<?> model) {
		setAllVisible(false);

		currentPart.visible = true;
		GeoBone bone = null;

		if (currentPart == model.hat || currentPart == model.head) {
			bone = this.head;
		} else if (currentPart == model.body) {
			bone = this.body;
		} else if (currentPart == model.leftArm) {
			bone = this.leftArm;
		} else if (currentPart == model.rightArm) {
			bone = this.rightArm;
		} else if (currentPart == model.leftLeg) {
			bone = currentSlot == EquipmentSlotType.FEET ? this.leftBoot : this.leftLeg;
		} else if (currentPart == model.rightLeg) {
			bone = currentSlot == EquipmentSlotType.FEET ? this.rightBoot : this.rightLeg;
		}

		if (bone != null)
			bone.setHidden(false);
	}

	/**
	 * Transform the currently rendering {@link GeoModel} to match the positions and rotations of the base model
	 */
	protected void applyBaseTransformations(BipedModel<?> baseModel) {
		if (this.head != null) {
			ModelRenderer headPart = baseModel.head;

			RenderUtils.matchModelPartRot(headPart, this.head);
			this.head.updatePosition(headPart.x, -headPart.y, headPart.z);
		}

		if (this.body != null) {
			ModelRenderer bodyPart = baseModel.body;

			RenderUtils.matchModelPartRot(bodyPart, this.body);
			this.body.updatePosition(bodyPart.x, -bodyPart.y, bodyPart.z);
		}

		if (this.rightArm != null) {
			ModelRenderer rightArmPart = baseModel.rightArm;

			RenderUtils.matchModelPartRot(rightArmPart, this.rightArm);
			this.rightArm.updatePosition(rightArmPart.x + 5, 2 - rightArmPart.y, rightArmPart.z);
		}

		if (this.leftArm != null) {
			ModelRenderer leftArmPart = baseModel.leftArm;

			RenderUtils.matchModelPartRot(leftArmPart, this.leftArm);
			this.leftArm.updatePosition(leftArmPart.x - 5f, 2f - leftArmPart.y, leftArmPart.z);
		}

		if (this.rightLeg != null) {
			ModelRenderer rightLegPart = baseModel.rightLeg;

			RenderUtils.matchModelPartRot(rightLegPart, this.rightLeg);
			this.rightLeg.updatePosition(rightLegPart.x + 2, 12 - rightLegPart.y, rightLegPart.z);

			if (this.rightBoot != null) {
				RenderUtils.matchModelPartRot(rightLegPart, this.rightBoot);
				this.rightBoot.updatePosition(rightLegPart.x + 2, 12 - rightLegPart.y, rightLegPart.z);
			}
		}

		if (this.leftLeg != null) {
			ModelRenderer leftLegPart = baseModel.leftLeg;

			RenderUtils.matchModelPartRot(leftLegPart, this.leftLeg);
			this.leftLeg.updatePosition(leftLegPart.x - 2, 12 - leftLegPart.y, leftLegPart.z);

			if (this.leftBoot != null) {
				RenderUtils.matchModelPartRot(leftLegPart, this.leftBoot);
				this.leftBoot.updatePosition(leftLegPart.x - 2, 12 - leftLegPart.y, leftLegPart.z);
			}
		}
	}

	@Override
	public void setAllVisible(boolean pVisible) {
		super.setAllVisible(pVisible);

		setBoneVisible(this.head, pVisible);
		setBoneVisible(this.body, pVisible);
		setBoneVisible(this.rightArm, pVisible);
		setBoneVisible(this.leftArm, pVisible);
		setBoneVisible(this.rightLeg, pVisible);
		setBoneVisible(this.leftLeg, pVisible);
		setBoneVisible(this.rightBoot, pVisible);
		setBoneVisible(this.leftBoot, pVisible);
	}

	/**
	 * Apply custom scaling to account for {@link net.minecraft.client.model.AgeableListModel AgeableListModel} baby models
	 */
	public void scaleModelForBaby(MatrixStack poseStack, T animatable, float partialTick, boolean isReRender) {
		if (!this.young || isReRender)
			return;

		if (this.currentSlot == EquipmentSlotType.HEAD) {
			if (this.baseModel.scaleHead) {
				float headScale = 1.5f / this.baseModel.babyHeadScale;

				poseStack.scale(headScale, headScale, headScale);
			}

			poseStack.translate(0, this.baseModel.yHeadOffset / 16f, this.baseModel.zHeadOffset / 16f);
		} else {
			float bodyScale = 1 / this.baseModel.babyBodyScale;

			poseStack.scale(bodyScale, bodyScale, bodyScale);
			poseStack.translate(0, this.baseModel.bodyYOffset / 16f, 0);
		}
	}

	/**
	 * Sets a bone as visible or hidden, with nullability
	 */
	protected void setBoneVisible(@Nullable GeoBone bone, boolean visible) {
		if (bone == null)
			return;

		bone.setHidden(!visible);
	}

	/**
	 * Create and fire the relevant {@code CompileLayers} event hook for this renderer
	 */
	@Override
	public void fireCompileRenderLayersEvent() {
		MinecraftForge.EVENT_BUS.post(new GeoRenderEvent.Armor.CompileRenderLayers(this));
	}

	/**
	 * Create and fire the relevant {@code Pre-Render} event hook for this renderer.<br>
	 * 
	 * @return Whether the renderer should proceed based on the cancellation state of the event
	 */
	@Override
	public boolean firePreRenderEvent(MatrixStack poseStack, BakedGeoModel model, IRenderTypeBuffer bufferSource, float partialTick, int packedLight) {
		return !MinecraftForge.EVENT_BUS.post(new GeoRenderEvent.Armor.Pre(this, poseStack, model, bufferSource, partialTick, packedLight));
	}

	/**
	 * Create and fire the relevant {@code Post-Render} event hook for this renderer
	 */
	@Override
	public void firePostRenderEvent(MatrixStack poseStack, BakedGeoModel model, IRenderTypeBuffer bufferSource, float partialTick, int packedLight) {
		MinecraftForge.EVENT_BUS.post(new GeoRenderEvent.Armor.Post(this, poseStack, model, bufferSource, partialTick, packedLight));
	}
}