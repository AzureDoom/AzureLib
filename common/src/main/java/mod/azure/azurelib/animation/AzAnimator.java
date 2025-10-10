package mod.azure.azurelib.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.animation.cache.AzBoneCache;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.rewrite.model.AzBakedModel;

/**
 * The {@code AzAnimator} class is an abstract base class for managing animations for various types of objects such as
 * entities, blocks, or items. It provides a reusable structure for animating objects, allowing the integration of a
 * variety of animation controllers and custom animations.
 *
 * @param <T> The type of object this animator will animate (e.g., an entity, block entity, or item stack).
 */
public abstract class AzAnimator<T> {

    private final AzAnimationContext<T> reusableContext;

    // Holds animation controllers.
    private final AzAnimationControllerContainer<T> animationControllerContainer;

    public boolean reloadAnimations;

    protected AzAnimator() {
        this(AzAnimatorConfig.defaultConfig());
    }

    protected AzAnimator(AzAnimatorConfig config) {
        this.animationControllerContainer = new AzAnimationControllerContainer<>();

        var boneCache = new AzBoneCache();
        var timer = new AzAnimationTimer(config);

        this.reusableContext = createReusableContext(config);
    }

    public AzBoneCache createBoneCache() {
        return new AzBoneCache();
    }

    public AzAnimationTimer createAzAnimationTimer(AzAnimatorConfig config) {
        return new AzAnimationTimer(config);
    }

    public AzAnimationContext<T> createReusableContext(AzAnimatorConfig config) {
        return new AzAnimationContext<>(createBoneCache(), config, createAzAnimationTimer(config));
    }

    public abstract void registerControllers(AzAnimationControllerContainer<T> animationControllerContainer);

    public abstract @NotNull ResourceLocation getAnimationLocation(T animatable);

    public void animate(T animatable, float partialTicks, boolean updateTimer) {
        reusableContext.animatable = animatable;

        var boneCache = reusableContext.boneCache();
        var timer = reusableContext.timer();

        if (updateTimer) {
            timer.tick();
        }

        preAnimationSetup(animatable, timer.getAnimTime(), partialTicks);

        if (!boneCache.isEmpty()) {

            for (var controller : animationControllerContainer.getAll()) {
                controller.update();
            }

            this.reloadAnimations = false;

            boneCache.update(reusableContext);
        }

        setCustomAnimations(animatable, partialTicks);
    }

    public void animate(T animatable, float partialTicks) {
        this.animate(animatable, partialTicks, true);
    }

    /**
     * Apply transformations and settings prior to acting on any animation-related functionality.
     *
     * @param animatable   The animatable being animated.
     * @param animTime     Animation time in seconds.
     * @param partialTicks The partial tick for smooth animations.
     */
    protected void preAnimationSetup(T animatable, double animTime, float partialTicks) {
        applyMolangQueries(animatable, animTime, partialTicks);
    }

    /**
     * Handles MoLang queries with support for partial ticks.
     *
     * @param animatable   The animatable being animated.
     * @param animTime     Animation time in seconds.
     * @param partialTicks The partial tick for smooth animations.
     */
    protected void applyMolangQueries(T animatable, double animTime, float partialTicks) {
        // TODO: Refactor this method by moving all logic from the old applyMolangQueries(animatable, animTime)
        // method directly into this one, ensuring that the old method is no longer called.
        // This will consolidate the logic to ensure that partial tick support is fully integrated here.
        applyMolangQueries(animatable, animTime);
    }

    /**
     * Existing method for applying MoLang queries.
     *
     * @param animatable The animatable being animated.
     * @param animTime   Animation time in seconds.
     */
    @Deprecated
    private void applyMolangQueries(T animatable, double animTime) {
        var level = Minecraft.getInstance().level;
        var parser = MolangParser.INSTANCE;

        if (level == null) {
            return;
        }

        parser.setMemoizedValue(MolangQueries.LIFE_TIME, () -> animTime / 20d);
        parser.setMemoizedValue(MolangQueries.ACTOR_COUNT, level::getEntityCount);
        parser.setMemoizedValue(MolangQueries.TIME_OF_DAY, () -> level.getDayTime() / 24000f);
        parser.setMemoizedValue(MolangQueries.MOON_PHASE, level::getMoonPhase);
    }

    /**
     * Sets custom animations for the given animatable object. This method is used to define and configure specific
     * animations unique to the context of the animatable and the current render state.
     *
     * @param animatable   The object for which custom animations are being set.
     * @param partialTicks The partial tick time used for interpolating animations smoothly between frames.
     */
    public void setCustomAnimations(T animatable, float partialTicks) {}

    public void setActiveModel(AzBakedModel model) {
        var modelChanged = reusableContext.boneCache().setActiveModel(model);

        if (modelChanged) {
            // If the model changed, we need to clear the bone animation queue cache for every controller.
            // TODO: We shouldn't have to remember to do this. If the baked model changes, then the bone cache
            // should be re-instantiated. If the bone cache is re-instantiated, then so should the bone animation
            // queue caches.
            animationControllerContainer.getAll()
                .forEach(controller -> controller.boneAnimationQueueCache().clear());
        }
    }

    /**
     * Get the baked animation object used for rendering from the given resource path
     */
    public AzBakedAnimation getAnimation(T animatable, String name) {
        var location = getAnimationLocation(animatable);
        var bakedAnimations = AzBakedAnimationCache.getInstance().getNullable(location);

        if (bakedAnimations == null) {
            throw new AzureLibException(location, "Unable to find animation.");
        }

        return bakedAnimations.getAnimation(name);
    }

    public AzAnimationContext<T> context() {
        return reusableContext;
    }

    public AzAnimationControllerContainer<T> getAnimationControllerContainer() {
        return animationControllerContainer;
    }
}
