package mod.azure.azurelib.animation.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.AzCachedBoneUpdateUtil;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * The AzBoneCache class is responsible for managing the state and cache of bones in a baked model. It provides
 * functionality for updating animation contexts, managing snapshots of bone states, and resetting transformation
 * markers in preparation for rendering.
 */
public class AzBoneCache {

    private AzBakedModel templateModel;

    private AzBakedModel bakedModel;

    private final Map<String, AzBoneSnapshot> boneSnapshotsByName;

    private static final AzBone[] NO_BONES = new AzBone[0];

    private static final AzBoneSnapshot[] NO_SNAPSHOTS = new AzBoneSnapshot[0];

    private static final boolean[] NO_FLAGS = new boolean[0];

    /*
     * Every bone of bakedModel, with its saved snapshot (or null) at the same index, so update() can walk them without
     * hashing bone names. Built for arrayModel; rebuilt whenever the baked model or the snapshot map is replaced.
     */
    private AzBone[] boneArray = NO_BONES;

    private AzBoneSnapshot[] snapshotArray = NO_SNAPSHOTS;

    /*
     * atRest[i]: bone i has finished easing back to its bind pose on every channel and nothing has changed it since, so
     * update() can skip it until something marks it changed again.
     */
    private boolean[] atRest = NO_FLAGS;

    @Nullable
    private AzBakedModel arrayModel;

    /** Counts animation frames for this animatable; advanced at the end of {@link #update}. */
    private long currentFrame;

    /**
     * The frame controllers wrote most recently, i.e. the frame {@link #update} last finished. {@code -1} until the
     * first update, which never matches a snapshot's write frame.
     */
    private long lastAnimatedFrame = -1;

    public AzBoneCache() {
        this.boneSnapshotsByName = new Object2ObjectOpenHashMap<>();
        setBakedModel(AzBakedModel.getDefault());
    }

    public void setActiveModel(AzBakedModel model) {
        if (model == null) {
            this.templateModel = null;
            this.bakedModel = AzBakedModel.getDefault();
            boneSnapshotsByName.clear();
            invalidateBoneArrays();
            return;
        }

        if (this.templateModel == model) {
            return;
        }

        this.templateModel = model;
        this.bakedModel = model.deepCopy();
        boneSnapshotsByName.clear();
        snapshot();
    }

    public void update(AzAnimationContext<?> context) {
        var config = context.config();
        var timer = context.timer();
        var animTime = timer.getAnimTime();
        var resetTickLength = config.boneResetTime();

        ensureBoneArrays();

        var bones = boneArray;
        var snapshots = snapshotArray;
        var rest = atRest;

        // Ease bones nothing animated this frame back towards their bind pose, then clear their change markers.
        for (int i = 0; i < bones.length; i++) {
            var bone = bones[i];
            var changed = bone.hasRotationChanged() || bone.hasPositionChanged() || bone.hasScaleChanged();

            // A settled bone that nothing touched since would only be set to the pose it already has.
            if (rest[i] && !changed) {
                continue;
            }

            var snapshot = snapshots[i];
            var rotationSettled = AzCachedBoneUpdateUtil.updateCachedBoneRotation(
                    bone,
                    snapshot,
                    animTime,
                    resetTickLength
            );
            var positionSettled = AzCachedBoneUpdateUtil.updateCachedBonePosition(
                    bone,
                    snapshot,
                    animTime,
                    resetTickLength
            );
            var scaleSettled = AzCachedBoneUpdateUtil.updateCachedBoneScale(
                    bone,
                    snapshot,
                    animTime,
                    resetTickLength
            );

            rest[i] = rotationSettled && positionSettled && scaleSettled;
            bone.resetStateChanges();
        }

        lastAnimatedFrame = currentFrame;
        currentFrame++;
    }

    /**
     * Whether an animation controller moved this bone's rotation in the most recent animation update.
     * <p>
     * Intended for {@code AzAnimator#setCustomAnimations}, which runs right after that update. When this returns
     * {@code true}, the bone's current rotation is this frame's animated value, so it is safe to add to it. When it
     * returns {@code false}, the bone still holds whatever was set last frame (possibly by your own code), so set the
     * rotation from {@link AzBone#getInitialAzSnapshot()} instead.
     * </p>
     *
     * @param bone a bone from this cache's {@link #getBakedModel() baked model}
     */
    public boolean wasRotationAnimatedThisFrame(AzBone bone) {
        var snapshot = boneSnapshotsByName.get(bone.getName());
        return snapshot != null && snapshot.isRotationWrittenInFrame(lastAnimatedFrame);
    }

    /**
     * Whether an animation controller moved this bone's position in the most recent animation update. See
     * {@link #wasRotationAnimatedThisFrame(AzBone)}.
     *
     * @param bone a bone from this cache's {@link #getBakedModel() baked model}
     */
    public boolean wasPositionAnimatedThisFrame(AzBone bone) {
        var snapshot = boneSnapshotsByName.get(bone.getName());
        return snapshot != null && snapshot.isPositionWrittenInFrame(lastAnimatedFrame);
    }

    /**
     * Whether an animation controller moved this bone's scale in the most recent animation update. See
     * {@link #wasRotationAnimatedThisFrame(AzBone)}.
     *
     * @param bone a bone from this cache's {@link #getBakedModel() baked model}
     */
    public boolean wasScaleAnimatedThisFrame(AzBone bone) {
        var snapshot = boneSnapshotsByName.get(bone.getName());
        return snapshot != null && snapshot.isScaleWrittenInFrame(lastAnimatedFrame);
    }

    /**
     * The frame controllers are currently writing. Used to tell whether an earlier controller already wrote a bone
     * channel this frame, so later controllers blend on top of it rather than on top of the bind pose.
     */
    public long currentFrame() {
        return currentFrame;
    }

    /**
     * Forces the bone arrays to be rebuilt on the next {@link #update}. Call this after replacing or removing entries
     * in {@link #getBoneSnapshotsByName()} directly; changing a snapshot's values does not need it.
     */
    public void invalidateBoneArrays() {
        arrayModel = null;
    }

    /**
     * (Re)builds {@link #boneArray}, {@link #snapshotArray} and {@link #atRest} for the current baked model if they
     * were built for a different one. Snapshots are taken from the name map as it stands, so a model set through
     * {@link #setBakedModel} pairs its bones with the existing snapshots by name, exactly as the name lookups did.
     */
    private void ensureBoneArrays() {
        if (arrayModel == bakedModel) {
            return;
        }

        var bones = bakedModel.getBonesByName().values().toArray(NO_BONES);
        var snapshots = new AzBoneSnapshot[bones.length];

        for (int i = 0; i < bones.length; i++) {
            snapshots[i] = boneSnapshotsByName.get(bones[i].getName());
        }

        boneArray = bones;
        snapshotArray = snapshots;
        atRest = new boolean[bones.length];
        arrayModel = bakedModel;
    }

    /**
     * Create new bone {@link AzBoneSnapshot} based on the bone's initial snapshot for the currently registered
     * {@link AzBone AzBones}, filtered by the bones already present in the master snapshots map
     */
    private void snapshot() {
        boneSnapshotsByName.clear();

        var bones = bakedModel.getBonesByName().values().toArray(NO_BONES);
        var snapshots = new AzBoneSnapshot[bones.length];

        for (int i = 0; i < bones.length; i++) {
            var snapshot = AzBoneSnapshot.copy(bones[i].getInitialAzSnapshot());
            snapshots[i] = snapshot;
            boneSnapshotsByName.put(bones[i].getName(), snapshot);
        }

        boneArray = bones;
        snapshotArray = snapshots;
        atRest = new boolean[bones.length];
        arrayModel = bakedModel;
    }

    public void setBakedModel(AzBakedModel model) {
        this.bakedModel = (model != null) ? model : AzBakedModel.getDefault();
        // The snapshot map is kept; ensureBoneArrays() pairs the new model's bones with it on the next update.
    }

    public AzBakedModel getBakedModel() {
        return bakedModel;
    }

    public AzBakedModel getTemplateModel() {
        return this.templateModel;
    }

    public Map<String, AzBoneSnapshot> getBoneSnapshotsByName() {
        return boneSnapshotsByName;
    }

    public boolean isEmpty() {
        return bakedModel.getBonesByName().isEmpty();
    }
}
