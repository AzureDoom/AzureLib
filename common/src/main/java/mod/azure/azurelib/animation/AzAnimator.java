package mod.azure.azurelib.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.WeakHashMap;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.animation.cache.AzBoneCache;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;

/**
 * The {@code AzAnimator} class is an abstract base class for managing animations for various types of objects such as
 * entities, blocks, or items. It provides a reusable structure for animating objects, allowing the integration of a
 * variety of animation controllers and custom animations.
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> The type of object this animator will animate (e.g., an entity, block entity, or item stack).
 */
public abstract class AzAnimator<K, T> {

    private AzAnimationContext<T> currentContext;

    private final WeakHashMap<K, AzAnimationContext<T>> contextCache = new WeakHashMap<>();

    private final AzAnimationControllerContainer<T> animationControllerContainer;

    protected final AzAnimatorConfig config;

    public boolean reloadAnimations;

    protected AzAnimator() {
        this(AzAnimatorConfig.defaultConfig());
    }

    protected AzAnimator(AzAnimatorConfig config) {
        this.animationControllerContainer = new AzAnimationControllerContainer<>();

        this.config = config;
    }

    public AzBoneCache createBoneCache() {
        return new AzBoneCache();
    }

    public AzAnimationTimer createAzAnimationTimer(AzAnimatorConfig config) {
        return new AzAnimationTimer(config);
    }

    public AzAnimationContext<T> getOrCreateContext(K uuid) {
        var ctx = contextCache.computeIfAbsent(
            uuid,
            a -> new AzAnimationContext<>(createBoneCache(), config, createAzAnimationTimer(config))
        );
        this.currentContext = ctx;
        return ctx;
    }

    public abstract void registerControllers(AzAnimationControllerContainer<T> animationControllerContainer);

    public abstract @NotNull ResourceLocation getAnimationLocation(T animatable);

    public void animate(T animatable, float partialTicks, boolean updateTimer) {
        this.currentContext.animatable = animatable;

        var boneCache = this.currentContext.boneCache();
        var timer = this.currentContext.timer();

        if (updateTimer) {
            timer.tick();
        }

        preAnimationSetup(animatable, timer.getAnimTime(), partialTicks);

        if (!boneCache.isEmpty()) {
            for (var controller : animationControllerContainer.getAll()) {
                controller.update();
            }

            this.reloadAnimations = false;

            boneCache.update(this.currentContext);
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

    /**
     * Get the baked animation object used for rendering from the given resource path
     */
    public AzBakedAnimation getAnimation(T animatable, String name) {
        var location = getAnimationLocation(animatable);
        var bakedAnimations = AzBakedAnimationCache.getInstance().getNullable(location);
        if (bakedAnimations == null) {
            AzureLib.LOGGER.error("Unable to find animations file: {}", location);
            return null;
        }

        return bakedAnimations.getAnimation(name);
    }

    public AzAnimationContext<T> context() {
        return currentContext;
    }

    public AzAnimationControllerContainer<T> getAnimationControllerContainer() {
        return animationControllerContainer;
    }
}
