package mod.azure.azurelib.animation.pose;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.controller.AzWeightFade;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * The procedural pose state of one animatable: per-modifier weights and state, and the bones the pose modifiers wrote
 * last frame. Obtain it with {@link AzAnimationContext#poseLayer()}.
 * <p>
 * Every {@code AzAnimator#animate} call does, in order:
 * </p>
 * <ol>
 * <li>{@link #restore()}: bones posed last frame get their animated pose back, unmarked, so controllers, the reset pass
 * and {@code setCustomAnimations} never see procedural offsets.</li>
 * <li>Controllers, the bone reset pass and {@code setCustomAnimations} run as before.</li>
 * <li>{@link #apply}: every modifier runs in order against that animated pose, then the result is written to the bones
 * without marking them changed.</li>
 * </ol>
 *
 * @param <T> the animatable type
 */
public final class AzPoseLayer<T> {

    private static final ModifierState[] NO_STATES = new ModifierState[0];

    private final AzAnimationContext<T> animationContext;

    private final AzPoseContext<T> poseContext;

    private final Map<String, AzPoseBone> bonesByName = new Object2ObjectOpenHashMap<>();

    private final ObjectArrayList<AzPoseBone> touched = new ObjectArrayList<>();

    private final Map<String, ModifierState> statesById = new Object2ObjectOpenHashMap<>();

    private @Nullable AzBakedModel boundModel;

    private @Nullable AzPoseModifierRegistry<T> boundRegistry;

    private int boundRegistryVersion = -1;

    private ModifierState[] alignedStates = NO_STATES;

    private long frame;

    private double lastAnimTime = Double.NaN;

    private boolean committed;

    private @Nullable AzPoseModifierEntry<T> currentEntry;

    private @Nullable ModifierState currentState;

    private float currentWeight;

    public AzPoseLayer(AzAnimationContext<T> animationContext) {
        this.animationContext = animationContext;
        this.poseContext = new AzPoseContext<>(this);
    }

    /** Sets a modifier's weight for this animatable immediately, cancelling any fade. Clamped to [0, 1]. */
    public void setWeight(String modifierId, double weight) {
        ModifierState state = stateFor(modifierId);
        state.fade.cancel();
        state.weight = Mth.clamp(weight, 0, 1);
        state.weightInitialized = true;
    }

    /**
     * Fades a modifier's weight for this animatable to {@code target} over {@code lengthTicks} animation ticks,
     * starting from its current weight. A length of 0 or less sets it immediately.
     */
    public void fadeWeight(String modifierId, double target, double lengthTicks) {
        if (lengthTicks <= 0) {
            setWeight(modifierId, target);
            return;
        }

        ModifierState state = stateFor(modifierId);
        double from = state.weightInitialized ? state.weight : defaultWeightOf(modifierId);
        state.weight = from;
        state.weightInitialized = true;
        state.fade.start(from, Mth.clamp(target, 0, 1), lengthTicks);
    }

    public double getWeight(String modifierId) {
        ModifierState state = statesById.get(modifierId);
        return state != null && state.weightInitialized ? state.weight : defaultWeightOf(modifierId);
    }

    public void resetState(String modifierId) {
        ModifierState state = statesById.get(modifierId);

        if (state != null) {
            state.userState = null;
            state.lastRunFrame = Long.MIN_VALUE;
        }
    }

    public void resetAllState() {
        for (ModifierState state : statesById.values()) {
            state.userState = null;
            state.lastRunFrame = Long.MIN_VALUE;
        }
    }

    public AzAnimationContext<T> animationContext() {
        return animationContext;
    }

    public void restore() {
        if (!committed) {
            return;
        }

        committed = false;

        if (boundModel == animationContext.boneCache().getBakedModel()) {
            for (int i = 0, n = touched.size(); i < n; i++) {
                touched.get(i).restore();
            }
        }

        touched.clear();
    }

    /**
     * Runs every modifier in {@code registry} against the current (animated) pose and writes the result to the bones.
     * Called by {@code AzAnimator} after {@code setCustomAnimations}.
     */
    public void apply(AzPoseModifierRegistry<T> registry, T animatable, float partialTick) {
        restore();
        // Anything left over from an evaluation that never committed (a modifier threw) must not be committed now.
        touched.clear();
        bindModel();

        AzPoseModifierEntry<T>[] entries = registry.sorted();

        if (entries.length == 0) {
            return;
        }

        alignStates(registry, entries);

        double animTime = animationContext.timer().getAnimTime();
        double gap = animTime - lastAnimTime;
        boolean layerDiscontinuous = Double.isNaN(gap) || gap < 0 || gap > 10;
        lastAnimTime = animTime;

        frame++;
        poseContext.begin(animatable, partialTick, animTime, layerDiscontinuous ? 0 : gap);

        try {
            for (int i = 0; i < entries.length; i++) {
                AzPoseModifierEntry<T> entry = entries[i];
                ModifierState state = alignedStates[i];
                float weight = (float) state.weight(animTime, entry);

                if (weight <= 0) {
                    continue;
                }

                currentEntry = entry;
                currentState = state;
                currentWeight = Math.min(weight, 1);
                poseContext.enter(layerDiscontinuous || state.lastRunFrame != frame - 1);
                state.lastRunFrame = frame;

                entry.modifier().apply(poseContext);
            }
        } finally {
            currentEntry = null;
            currentState = null;
            poseContext.end();
        }

        for (int i = 0, n = touched.size(); i < n; i++) {
            touched.get(i).commit();
        }

        committed = !touched.isEmpty();
    }

    long frame() {
        return frame;
    }

    float weight() {
        return currentWeight;
    }

    AzPoseModifierEntry<T> currentEntry() {
        if (currentEntry == null) {
            throw new IllegalStateException("No pose modifier is running");
        }

        return currentEntry;
    }

    boolean canWrite(AzBone bone) {
        return currentEntry != null && currentEntry.mask().includes(bone);
    }

    void markTouched(AzPoseBone bone) {
        touched.add(bone);
    }

    AzPoseBone bone(String name) {
        AzPoseBone poseBone = bonesByName.get(name);

        if (poseBone == null) {
            AzBone bone = boundModel == null ? null : boundModel.getBoneOrNull(name);
            poseBone = bone == null ? AzPoseBone.MISSING : new AzPoseBone(this, bone);
            bonesByName.put(name, poseBone);
        }

        return poseBone;
    }

    @SuppressWarnings("unchecked")
    <S> S state(Supplier<S> factory) {
        ModifierState state = currentState;

        if (state == null) {
            throw new IllegalStateException("No pose modifier is running");
        }

        if (state.userState == null) {
            state.userState = factory.get();
        }

        return (S) state.userState;
    }

    private void bindModel() {
        AzBakedModel model = animationContext.boneCache().getBakedModel();

        if (model != boundModel) {
            boundModel = model;
            bonesByName.clear();
            touched.clear();
            resetAllState();
        }
    }

    private void alignStates(AzPoseModifierRegistry<T> registry, AzPoseModifierEntry<T>[] entries) {
        int version = registry.version();

        if (registry == boundRegistry && version == boundRegistryVersion && alignedStates.length == entries.length) {
            return;
        }

        ModifierState[] states = new ModifierState[entries.length];

        for (int i = 0; i < entries.length; i++) {
            states[i] = stateFor(entries[i].id());
        }

        alignedStates = states;
        boundRegistry = registry;
        boundRegistryVersion = version;
    }

    private ModifierState stateFor(String modifierId) {
        return statesById.computeIfAbsent(modifierId, ignored -> new ModifierState());
    }

    private double defaultWeightOf(String modifierId) {
        AzPoseModifierEntry<T> entry = boundRegistry == null ? null : boundRegistry.get(modifierId);
        return entry == null ? 1 : entry.defaultWeight();
    }

    private static final class ModifierState {

        private final AzWeightFade fade = new AzWeightFade();

        private double weight;

        private boolean weightInitialized;

        private long lastRunFrame = Long.MIN_VALUE;

        private @Nullable Object userState;

        double weight(double animTime, AzPoseModifierEntry<?> entry) {
            if (!weightInitialized) {
                weight = entry.defaultWeight();
                weightInitialized = true;
            }

            if (fade.isActive()) {
                weight = fade.update(animTime);
            }

            return weight;
        }
    }
}
