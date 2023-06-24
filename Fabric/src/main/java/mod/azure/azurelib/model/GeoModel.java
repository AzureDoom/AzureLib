package mod.azure.azurelib.model;

import java.util.Optional;
import java.util.function.BiConsumer;

import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.model.CoreGeoModel;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.AnimationProcessor;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.object.DataTicket;
import mod.azure.azurelib.loading.object.BakedAnimations;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Base class for all code-based model objects.<br>
 * All models to registered to a {@link GeoRenderer} should be an instance of this or one of its subclasses.
 */
public abstract class GeoModel<T extends GeoAnimatable> implements CoreGeoModel<T> {
	private final AnimationProcessor<T> processor = new AnimationProcessor<>(this);

	private BakedGeoModel currentModel = null;
	private double animTime;
	private double lastGameTickTime;

	/**
	 * Returns the resource path for the {@link BakedGeoModel} (model json file) to render based on the provided animatable
	 */
	public abstract ResourceLocation getModelResource(T animatable);

	/**
	 * Returns the resource path for the texture file to render based on the provided animatable
	 */
	public abstract ResourceLocation getTextureResource(T animatable);

	/**
	 * Returns the resourcepath for the {@link BakedAnimations} (animation json file) to use for animations based on the provided animatable
	 */
	public abstract ResourceLocation getAnimationResource(T animatable);

	/**
	 * Override this and return true if AzureLib should crash when attempting to animate the model, but fails to find a bone.<br>
	 * By default, AzureLib will just gracefully ignore a missing bone, which might cause oddities with incorrect models or mismatching variables.<br>
	 */
	public boolean crashIfBoneMissing() {
		return false;
	}

	/**
	 * Gets the default render type for this animatable, to be selected by default by the renderer using it
	 */
	public RenderType getRenderType(T animatable, ResourceLocation texture) {
		return RenderType.entityCutoutNoCull(texture);
	}

	@Override
	public final BakedGeoModel getBakedGeoModel(String location) {
		return getBakedModel(new ResourceLocation(location));
	}

	/**
	 * Get the baked geo model object used for rendering from the given resource path
	 */
	public BakedGeoModel getBakedModel(ResourceLocation location) {
		BakedGeoModel model = AzureLibCache.getBakedModels().get(location);

		if (model == null)
			throw new AzureLibException(location, "Unable to find model");

		if (model != this.currentModel) {
			this.processor.setActiveModel(model);
			this.currentModel = model;
		}

		return this.currentModel;
	}

	/**
	 * Gets a bone from this model by name
	 * 
	 * @param name The name of the bone
	 * @return An {@link Optional} containing the {@link mod.azure.azurelib.cache.object.GeoBone} if one matches, otherwise an empty Optional
	 */
	public Optional<mod.azure.azurelib.cache.object.GeoBone> getBone(String name) {
		return Optional.ofNullable((GeoBone) getAnimationProcessor().getBone(name));
	}

	/**
	 * Get the baked animation object used for rendering from the given resource path
	 */
	@Override
	public Animation getAnimation(T animatable, String name) {
		ResourceLocation location = getAnimationResource(animatable);
		BakedAnimations bakedAnimations = AzureLibCache.getBakedAnimations().get(location);

		if (bakedAnimations == null)
			throw new AzureLibException(location, "Unable to find animation.");

		return bakedAnimations.getAnimation(name);
	}

	@Override
	public AnimationProcessor<T> getAnimationProcessor() {
		return this.processor;
	}

	/**
	 * Add additional {@link DataTicket DataTickets} to the {@link AnimationState} to be handled by your animation handler at render time
	 * 
	 * @param animatable   The animatable instance currently being animated
	 * @param instanceId   The unique instance id of the animatable being animated
	 * @param dataConsumer The DataTicket + data consumer to be added to the AnimationEvent
	 */
	public void addAdditionalStateData(T animatable, long instanceId, BiConsumer<DataTicket<T>, T> dataConsumer) {
	}

	@Override
	public final void handleAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
		Minecraft mc = Minecraft.getInstance();
		AnimatableManager<T> animatableManager = animatable.getAnimatableInstanceCache().getManagerForId(instanceId);
		Double currentTick = animationState.getData(DataTickets.TICK);

		if (currentTick == null)
			currentTick = animatable instanceof LivingEntity livingEntity ? (double) livingEntity.tickCount : RenderUtils.getCurrentTick();

		if (animatableManager.getFirstTickTime() == -1)
			animatableManager.startedAt(currentTick + mc.getFrameTime());

		double currentFrameTime = currentTick - animatableManager.getFirstTickTime();

		if (!animatableManager.isFirstTick() && currentFrameTime == animatableManager.getLastUpdateTime())
			return;

		if ((!mc.isPaused() || animatable.shouldPlayAnimsWhileGamePaused())) {
			if (animatable instanceof LivingEntity) {
				animatableManager.updatedAt(currentFrameTime);
			} else {
				animatableManager.updatedAt(currentFrameTime);
			}

			double lastUpdateTime = animatableManager.getLastUpdateTime();
			this.animTime += lastUpdateTime - this.lastGameTickTime;
			this.lastGameTickTime = lastUpdateTime;
		}

		animationState.animationTick = this.animTime;
		AnimationProcessor<T> processor = getAnimationProcessor();

		processor.preAnimationSetup(animationState.getAnimatable(), this.animTime);

		if (!processor.getRegisteredBones().isEmpty())
			processor.tickAnimation(animatable, this, animatableManager, this.animTime, animationState, crashIfBoneMissing());

		setCustomAnimations(animatable, instanceId, animationState);
	}

	@Override
	public void applyMolangQueries(T animatable, double animTime) {
		MolangParser parser = MolangParser.INSTANCE;
		Minecraft mc = Minecraft.getInstance();

		parser.setMemoizedValue(MolangQueries.LIFE_TIME, () -> animTime / 20d);
		parser.setMemoizedValue(MolangQueries.ACTOR_COUNT, mc.level::getEntityCount);
		parser.setMemoizedValue(MolangQueries.TIME_OF_DAY, () -> mc.level.getDayTime() / 24000f);
		parser.setMemoizedValue(MolangQueries.MOON_PHASE, mc.level::getMoonPhase);

		if (animatable instanceof Entity entity) {
			parser.setMemoizedValue(MolangQueries.DISTANCE_FROM_CAMERA, () -> mc.gameRenderer.getMainCamera().getPosition().distanceTo(entity.position()));
			parser.setMemoizedValue(MolangQueries.IS_ON_GROUND, () -> RenderUtils.booleanToFloat(entity.isOnGround()));
			parser.setMemoizedValue(MolangQueries.IS_IN_WATER, () -> RenderUtils.booleanToFloat(entity.isInWater()));
			parser.setMemoizedValue(MolangQueries.IS_IN_WATER_OR_RAIN, () -> RenderUtils.booleanToFloat(entity.isInWaterRainOrBubble()));

			if (entity instanceof LivingEntity livingEntity) {
				parser.setMemoizedValue(MolangQueries.HEALTH, livingEntity::getHealth);
				parser.setMemoizedValue(MolangQueries.MAX_HEALTH, livingEntity::getMaxHealth);
				parser.setMemoizedValue(MolangQueries.IS_ON_FIRE, () -> RenderUtils.booleanToFloat(livingEntity.isOnFire()));
				parser.setMemoizedValue(MolangQueries.GROUND_SPEED, () -> {
					Vec3 velocity = livingEntity.getDeltaMovement();

					return Mth.sqrt((float) ((velocity.x * velocity.x) + (velocity.z * velocity.z)));
				});
				parser.setMemoizedValue(MolangQueries.YAW_SPEED, () -> livingEntity.getViewYRot((float) animTime - livingEntity.getViewYRot((float) animTime - 0.1f)));
			}
		}
	}
}
