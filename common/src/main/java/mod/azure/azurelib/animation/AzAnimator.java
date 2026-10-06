package mod.azure.azurelib.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.MoonPhase;
import org.jetbrains.annotations.NotNull;

import java.util.WeakHashMap;
import java.util.function.DoubleSupplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.animation.cache.AzBoneCache;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;

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

    private static final MolangVariableRef LIFE_TIME_REF = new MolangVariableRef(MolangQueries.LIFE_TIME);

    private static final MolangVariableRef ACTOR_COUNT_REF = new MolangVariableRef(MolangQueries.ACTOR_COUNT);

    private static final MolangVariableRef TIME_OF_DAY_REF = new MolangVariableRef(MolangQueries.TIME_OF_DAY);

    private static final MolangVariableRef MOON_PHASE_REF = new MolangVariableRef(MolangQueries.MOON_PHASE);

    private static final MolangVariableRef MOON_BRIGHTNESS_REF = new MolangVariableRef(MolangQueries.MOON_BRIGHTNESS);

    private static final MolangVariableRef DAY_REF = new MolangVariableRef(MolangQueries.DAY);

    private static final MolangVariableRef TIME_STAMP_REF = new MolangVariableRef(MolangQueries.TIME_STAMP);

    private static final MolangVariableRef FRAME_ALPHA_REF = new MolangVariableRef(MolangQueries.FRAME_ALPHA);

    private static final MolangVariableRef CLIENT_MAX_RENDER_DISTANCE_REF = new MolangVariableRef(
        MolangQueries.CLIENT_MAX_RENDER_DISTANCE
    );

    private static final double[] MOON_BRIGHTNESS = { 1, 0.75, 0.5, 0.25, 0, 0.25, 0.5, 0.75 };

    private AzAnimationContext<T> currentContext;

    private final WeakHashMap<K, AzAnimationContext<T>> contextCache = new WeakHashMap<>();

    private final AzAnimationControllerContainer<T> animationControllerContainer;

    protected final AzAnimatorConfig config;

    public boolean reloadAnimations;

    private double molangAnimTime;

    private float molangPartialTicks;

    private final DoubleSupplier lifetimeSupplier = () -> molangAnimTime / 20d;

    private final DoubleSupplier actorCountSupplier = () -> {
        var lvl = Minecraft.getInstance().level;
        return lvl != null ? lvl.getEntityCount() : 0;
    };

    private final DoubleSupplier timeOfDaySupplier = () -> {
        var lvl = Minecraft.getInstance().level;
        return lvl != null ? lvl.getDefaultClockTime() / 24000f : 0;
    };

    private final DoubleSupplier moonPhaseSupplier = AzAnimator::moonPhase;

    private final DoubleSupplier moonBrightnessSupplier = () -> MOON_BRIGHTNESS[moonPhase()];

    private final DoubleSupplier daySupplier = () -> {
        var lvl = Minecraft.getInstance().level;
        return lvl != null ? Math.floorDiv(lvl.getDefaultClockTime(), 24000L) : 0;
    };

    private final DoubleSupplier timeStampSupplier = () -> {
        var lvl = Minecraft.getInstance().level;
        return lvl != null ? lvl.getGameTime() : 0;
    };

    private final DoubleSupplier frameAlphaSupplier = () -> molangPartialTicks;

    private final DoubleSupplier clientMaxRenderDistanceSupplier = () -> Minecraft.getInstance().options
        .getEffectiveRenderDistance();

    private static int moonPhase() {
        var lvl = Minecraft.getInstance().level;

        if (lvl == null)
            return 0;

        return (int) Math.floorMod(lvl.getDefaultClockTime() / MoonPhase.PHASE_LENGTH, (long) MoonPhase.COUNT);
    }

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
            ignored -> new AzAnimationContext<>(createBoneCache(), config, createAzAnimationTimer(config))
        );
        this.currentContext = ctx;
        return ctx;
    }

    public abstract void registerControllers(AzAnimationControllerContainer<T> animationControllerContainer);

    public abstract @NotNull Identifier getAnimationLocation(T animatable);

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

        if (level == null) {
            return;
        }

        this.molangAnimTime = animTime;
        this.molangPartialTicks = partialTicks;
        LIFE_TIME_REF.setMemoized(lifetimeSupplier);
        ACTOR_COUNT_REF.setMemoized(actorCountSupplier);
        TIME_OF_DAY_REF.setMemoized(timeOfDaySupplier);
        MOON_PHASE_REF.setMemoized(moonPhaseSupplier);
        MOON_BRIGHTNESS_REF.setMemoized(moonBrightnessSupplier);
        DAY_REF.setMemoized(daySupplier);
        TIME_STAMP_REF.setMemoized(timeStampSupplier);
        FRAME_ALPHA_REF.setMemoized(frameAlphaSupplier);
        CLIENT_MAX_RENDER_DISTANCE_REF.setMemoized(clientMaxRenderDistanceSupplier);

        AzMolangQueryContext.INSTANCE.bind(null, partialTicks);
    }

    /**
     * Sets custom animations for the given animatable object. This method is used to define and configure specific
     * animations unique to the context of the animatable and the current render state.
     *
     * @param animatable   The object for which custom animations are being set.
     * @param partialTicks The partial tick time used for interpolating animations smoothly between frames.
     */
    @SuppressWarnings("unused")
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
