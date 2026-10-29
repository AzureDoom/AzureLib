package mod.azure.azurelib.animation;

import net.minecraft.world.World;

import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import javax.annotation.Nonnull;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.animation.cache.AzBoneCache;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.animation.pose.AzPoseLayer;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.pose.AzPoseModifiers;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.animation.primitive.AzBakedAnimations;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangScope;
import mod.azure.azurelib.core.molang.MolangVariableRef;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.profiling.AzProfileStage;
import mod.azure.azurelib.profiling.AzProfiler;
import mod.azure.azurelib.util.ResourceLocation;

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

    private final AzPoseModifierRegistry<T> poseModifiers = new AzPoseModifierRegistry<>();

    private boolean poseModifiersRegistered;

    public boolean reloadAnimations;

    private double molangAnimTime;

    private float molangPartialTicks;

    private boolean hasAnimated;

    private final DoubleSupplier lifetimeSupplier = () -> molangAnimTime / 20d;

    private final DoubleSupplier actorCountSupplier = () -> {
        World lvl = McClient.world();
        return lvl != null ? Mc.entities(lvl).size() : 0;
    };

    private final DoubleSupplier timeOfDaySupplier = () -> {
        World lvl = McClient.world();
        return lvl != null ? Mc.time(lvl) / 24000f : 0;
    };

    private final DoubleSupplier moonPhaseSupplier = AzAnimator::moonPhase;

    private final DoubleSupplier moonBrightnessSupplier = () -> MOON_BRIGHTNESS[moonPhase()];

    private final DoubleSupplier daySupplier = () -> {
        World lvl = McClient.world();
        return lvl != null ? Math.floorDiv(Mc.time(lvl), 24000L) : 0;
    };

    private final DoubleSupplier timeStampSupplier = () -> {
        World lvl = McClient.world();
        return lvl != null ? Mc.time(lvl) : 0;
    };

    private final DoubleSupplier frameAlphaSupplier = () -> molangPartialTicks;

    private final DoubleSupplier clientMaxRenderDistanceSupplier = McClient::renderDistanceChunks;

    private static int moonPhase() {
        World lvl = McClient.world();

        if (lvl == null)
            return 0;

        return McClient.moonPhase();
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
        AzAnimationContext<T> ctx = contextCache.computeIfAbsent(
            uuid,
            ignored -> new AzAnimationContext<>(createBoneCache(), config, createAzAnimationTimer(config))
        );
        this.currentContext = ctx;
        return ctx;
    }

    public AzAnimationContext<T> getOrCreateContext(K uuid, T animatable) {
        AzAnimationContext<T> ctx = getOrCreateContext(uuid);
        ctx.animatable = animatable;
        return ctx;
    }

    public abstract void registerControllers(AzAnimationControllerContainer<T> animationControllerContainer);

    public abstract @Nonnull ResourceLocation getAnimationLocation(T animatable);

    public void animate(T animatable, float partialTicks, boolean updateTimer) {
        AzProfiler.begin(AzProfileStage.ANIMATE, animatable);
        this.currentContext.animatable = animatable;

        // Molang variable.* reads and writes go to this animatable's own scope while it animates.
        MolangScope previousScope = MolangScope.setCurrent(this.currentContext.molangScope());

        try {
            animateInScope(animatable, partialTicks, updateTimer);
        } finally {
            MolangScope.setCurrent(previousScope);
        }

        AzProfiler.end(AzProfileStage.ANIMATE);
    }

    private void animateInScope(T animatable, float partialTicks, boolean updateTimer) {
        AzBoneCache boneCache = this.currentContext.boneCache();
        AzAnimationTimer timer = this.currentContext.timer();

        if (updateTimer) {
            timer.tick();
        }

        // Take last frame's procedural offsets back out before anything reads or animates the bones.
        AzPoseLayer<T> poseLayer = this.currentContext.poseLayer();
        poseLayer.restore();

        if (!hasAnimated) {
            hasAnimated = true;
            onFirstAnimate(animatable);
        }

        AzProfiler.begin(AzProfileStage.MOLANG_SETUP, animatable);
        preAnimationSetup(animatable, timer.getAnimTime(), partialTicks);
        AzProfiler.end(AzProfileStage.MOLANG_SETUP);

        if (!boneCache.isEmpty()) {
            for (AzAnimationController<T> controller : animationControllerContainer.getAll()) {
                AzProfiler.begin(AzProfileStage.CONTROLLER_UPDATE, controller);
                controller.update();
                AzProfiler.end(AzProfileStage.CONTROLLER_UPDATE);
            }

            this.reloadAnimations = false;

            AzProfiler.begin(AzProfileStage.BONE_UPDATE, animatable);
            boneCache.update(this.currentContext);
            AzProfiler.end(AzProfileStage.BONE_UPDATE);

            AzPoseModifierRegistry<T> modifiers = getPoseModifiers();

            if (!boneCache.isEmpty() && !modifiers.isEmpty()) {
                AzProfiler.begin(AzProfileStage.POSE_MODIFIERS, animatable);
                poseLayer.apply(modifiers, animatable, partialTicks);
                AzProfiler.end(AzProfileStage.POSE_MODIFIERS);
            }
        }

        AzProfiler.begin(AzProfileStage.CUSTOM_ANIMATIONS, animatable);
        setCustomAnimations(animatable, partialTicks);
        AzProfiler.end(AzProfileStage.CUSTOM_ANIMATIONS);

        AzPoseModifierRegistry<T> modifiers = getPoseModifiers();

        if (!boneCache.isEmpty() && !modifiers.isEmpty()) {
            AzProfiler.begin(AzProfileStage.POSE_MODIFIERS, animatable);
            poseLayer.apply(modifiers, animatable, partialTicks);
            AzProfiler.end(AzProfileStage.POSE_MODIFIERS);
        }
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
        World level = McClient.world();

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
     * <p>
     * Writes made here go straight onto the bones and become part of the animated pose, so additive changes accumulate
     * on bones no controller animated this frame (see {@link AzBoneCache#wasRotationAnimatedThisFrame}). For procedural
     * posing prefer {@link #registerPoseModifiers}: modifiers are weighted, masked, ordered, and never leak into the
     * next frame.
     * </p>
     *
     * @param animatable   The object for which custom animations are being set.
     * @param partialTicks The partial tick time used for interpolating animations smoothly between frames.
     */
    @SuppressWarnings("unused")
    public void setCustomAnimations(T animatable, float partialTicks) {}

    /**
     * Registers this animator's procedural pose modifiers. Called once, before the first {@link #animate}. Modifiers
     * run every frame after the controllers, the bone reset pass and {@link #setCustomAnimations}, in priority order,
     * and their writes are undone before the next frame's controllers run.
     *
     * <pre>{@code
     *
     * protected void registerPoseModifiers(AzPoseModifierRegistry<MyEntity> modifiers) {
     *     modifiers.add("look", AzPoseModifiers.headLook("neck", "head"));
     *     modifiers.add("tail", AzPoseModifiers.spring("tail")).priority(100);
     * }
     * }</pre>
     *
     * @param modifiers the registry to add to
     * @see AzPoseModifiers
     * @see AzAnimationContext#poseLayer()
     */
    protected void registerPoseModifiers(AzPoseModifierRegistry<T> modifiers) {}

    /**
     * This animator's pose modifiers. Addons and compat code may add or remove modifiers here at any time; changes
     * apply from the next frame.
     */
    public AzPoseModifierRegistry<T> getPoseModifiers() {
        if (!poseModifiersRegistered) {
            poseModifiersRegistered = true;
            registerPoseModifiers(poseModifiers);
        }

        return poseModifiers;
    }

    /**
     * Get the baked animation object used for rendering from the given resource path
     */
    public AzBakedAnimation getAnimation(T animatable, String name) {
        ResourceLocation location = getAnimationLocation(animatable);
        AzBakedAnimations bakedAnimations = AzBakedAnimationCache.getInstance().getNullable(location);
        if (bakedAnimations == null) {
            AzureLib.LOGGER.error("Unable to find animations file: {}", location);
            return null;
        }

        return bakedAnimations.getAnimation(name);
    }

    public AzAnimationContext<T> context() {
        return currentContext;
    }

    /**
     * Looks up the context this animator keeps for {@code key} without creating one or changing the current context.
     * Molang's arrow operator uses it to read another animatable's {@code variable.*} values.
     *
     * @return the context, or {@code null} if this animator has never animated {@code key}
     */
    public AzAnimationContext<T> findContext(K key) {
        return contextCache.get(key);
    }

    public AzAnimationControllerContainer<T> getAnimationControllerContainer() {
        return animationControllerContainer;
    }

    /**
     * Runs {@code dispatch} on every animator linked to this animator's current context. Called by the command dispatch
     * paths after a command has been applied to this animator.
     */
    public void forEachLinkedAnimator(Consumer<AzAnimator<?, ?>> dispatch) {
        AzAnimationContext<T> context = this.currentContext;

        if (context == null) {
            return;
        }

        for (AzAnimator<?, ?> linked : context.linkedAnimators()) {
            dispatch.accept(linked);
        }
    }

    /**
     * Called once, on this animator's first {@link #animate} call, right before its controllers update for the first
     * time. Commands dispatched here (e.g. {@code MY_COMMAND.sendForEntity(animatable)}) apply on that same frame, so
     * the animatable never renders a frame without an animation.
     * <p>
     * This is the place to start an entity's spawn or initial animation. Prefer it over keying off
     * {@code tickCount == 0}: a freshly spawned entity is not guaranteed to render before its first client tick. It
     * runs again if the animator is replaced, e.g. when the model changes.
     * </p>
     *
     * @param animatable the animatable being animated for the first time
     */
    protected void onFirstAnimate(T animatable) {}
}
