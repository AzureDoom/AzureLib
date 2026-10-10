package mod.azure.azurelib.animation.cache;

import java.util.HashMap;
import java.util.Map;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.AzAnimationTimer;
import mod.azure.azurelib.animation.AzAnimatorConfig;
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

    /** Counts animation frames for this animatable; advanced at the end of {@link #update}. */
    private long currentFrame;

    /**
     * The frame controllers wrote most recently, i.e. the frame {@link #update} last finished. {@code -1} until the
     * first update, which never matches a snapshot's write frame.
     */
    private long lastAnimatedFrame = -1;

    public AzBoneCache() {
        this.boneSnapshotsByName = new HashMap<>();
        setBakedModel(AzBakedModel.getDefault());
    }

    public boolean setActiveModel(AzBakedModel model) {
        if (model == null) {
            this.templateModel = null;
            this.bakedModel = AzBakedModel.getDefault();
            boneSnapshotsByName.clear();
            return true;
        }

        if (this.templateModel == model) {
            return false;
        }

        this.templateModel = model;
        this.bakedModel = model.deepCopy();
        boneSnapshotsByName.clear();
        snapshot();

        return true;
    }

    public void update(AzAnimationContext<?> context) {
        AzAnimatorConfig config = context.config();
        AzAnimationTimer timer = context.timer();
        double animTime = timer.getAnimTime();
        Map<String, AzBoneSnapshot> boneSnapshots = getBoneSnapshotsByName();
        double resetTickLength = config.boneResetTime();

        // Updates the cached bone snapshots (only if they have changed).
        for (AzBone bone : bakedModel.getBonesByName().values()) {
            AzCachedBoneUpdateUtil.updateCachedBoneRotation(bone, boneSnapshots, animTime, resetTickLength);
            AzCachedBoneUpdateUtil.updateCachedBonePosition(bone, boneSnapshots, animTime, resetTickLength);
            AzCachedBoneUpdateUtil.updateCachedBoneScale(bone, boneSnapshots, animTime, resetTickLength);
        }

        resetBoneTransformationMarkers();
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
        AzBoneSnapshot snapshot = boneSnapshotsByName.get(bone.getName());
        return snapshot != null && snapshot.isRotationWrittenInFrame(lastAnimatedFrame);
    }

    /**
     * Whether an animation controller moved this bone's position in the most recent animation update. See
     * {@link #wasRotationAnimatedThisFrame(AzBone)}.
     *
     * @param bone a bone from this cache's {@link #getBakedModel() baked model}
     */
    public boolean wasPositionAnimatedThisFrame(AzBone bone) {
        AzBoneSnapshot snapshot = boneSnapshotsByName.get(bone.getName());
        return snapshot != null && snapshot.isPositionWrittenInFrame(lastAnimatedFrame);
    }

    /**
     * Whether an animation controller moved this bone's scale in the most recent animation update. See
     * {@link #wasRotationAnimatedThisFrame(AzBone)}.
     *
     * @param bone a bone from this cache's {@link #getBakedModel() baked model}
     */
    public boolean wasScaleAnimatedThisFrame(AzBone bone) {
        AzBoneSnapshot snapshot = boneSnapshotsByName.get(bone.getName());
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
     * Reset the transformation markers applied to each {@link AzBone} ready for the next render frame
     */
    private void resetBoneTransformationMarkers() {
        bakedModel.getBonesByName().values().forEach(AzBone::resetStateChanges);
    }

    /**
     * Create new bone {@link AzBoneSnapshot} based on the bone's initial snapshot for the currently registered
     * {@link AzBone AzBones}, filtered by the bones already present in the master snapshots map
     */
    private void snapshot() {
        boneSnapshotsByName.clear();

        for (AzBone bone : bakedModel.getBonesByName().values()) {
            boneSnapshotsByName.put(bone.getName(), AzBoneSnapshot.copy(bone.getInitialAzSnapshot()));
        }
    }

    public void setBakedModel(AzBakedModel model) {
        this.bakedModel = (model != null) ? model : AzBakedModel.getDefault();
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
