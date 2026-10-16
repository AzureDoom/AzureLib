package mod.azure.azurelib.animation;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

import mod.azure.azurelib.core.utils.Interpolations;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * Eases bones that no controller animated this frame back towards their bind pose.
 * <p>
 * Each channel has two overloads. The {@link AzBoneSnapshot} overloads are what
 * {@link mod.azure.azurelib.animation.cache.AzBoneCache} uses: the caller passes the bone's saved snapshot directly,
 * and the return value says whether the channel has settled, so the cache can stop visiting bones that are at rest. The
 * {@code Map} overloads look the snapshot up by name and are kept for compatibility.
 * </p>
 */
public class AzCachedBoneUpdateUtil {

    /**
     * Eases a bone's position back towards its initial snapshot.
     *
     * @param bone            the bone whose position is to be updated
     * @param saveSnapshot    the bone's saved snapshot, or {@code null} if it has none
     * @param animTime        the current animation time
     * @param resetTickLength the duration over which the position reset occurs
     * @return {@code true} if the channel is at rest: no snapshot to ease from, or the reset has finished.
     *         {@code false} if the bone was changed this frame or is still easing back.
     */
    public static boolean updateCachedBonePosition(
        AzBone bone,
        @Nullable AzBoneSnapshot saveSnapshot,
        double animTime,
        double resetTickLength
    ) {
        if (bone.hasPositionChanged()) {
            return false;
        }

        if (saveSnapshot == null) {
            return true;
        }

        AzBoneSnapshot initialSnapshot = bone.getInitialAzSnapshot();

        if (saveSnapshot.isPosAnimInProgress()) {
            saveSnapshot.stopPosAnim(animTime);
        }

        double percentageReset = Math.min(
            (animTime - saveSnapshot.getLastResetPositionTick()) / resetTickLength,
            1
        );

        bone.setPosX(
            (float) Interpolations.lerp(saveSnapshot.getOffsetX(), initialSnapshot.getOffsetX(), percentageReset)
        );
        bone.setPosY(
            (float) Interpolations.lerp(saveSnapshot.getOffsetY(), initialSnapshot.getOffsetY(), percentageReset)
        );
        bone.setPosZ(
            (float) Interpolations.lerp(saveSnapshot.getOffsetZ(), initialSnapshot.getOffsetZ(), percentageReset)
        );

        if (percentageReset >= 1) {
            saveSnapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
            return true;
        }

        return false;
    }

    /**
     * Eases a bone's rotation back towards its initial snapshot. See
     * {@link #updateCachedBonePosition(AzBone, AzBoneSnapshot, double, double)} for the return value.
     */
    public static boolean updateCachedBoneRotation(
        AzBone bone,
        @Nullable AzBoneSnapshot saveSnapshot,
        double animTime,
        double resetTickLength
    ) {
        if (bone.hasRotationChanged()) {
            return false;
        }

        if (saveSnapshot == null) {
            return true;
        }

        AzBoneSnapshot initialSnapshot = bone.getInitialAzSnapshot();

        if (saveSnapshot.isRotAnimInProgress()) {
            saveSnapshot.stopRotAnim(animTime);
        }

        double percentageReset = Math.min(
            (animTime - saveSnapshot.getLastResetRotationTick()) / resetTickLength,
            1
        );

        bone.setRotX(
            (float) Interpolations.lerp(saveSnapshot.getRotX(), initialSnapshot.getRotX(), percentageReset)
        );
        bone.setRotY(
            (float) Interpolations.lerp(saveSnapshot.getRotY(), initialSnapshot.getRotY(), percentageReset)
        );
        bone.setRotZ(
            (float) Interpolations.lerp(saveSnapshot.getRotZ(), initialSnapshot.getRotZ(), percentageReset)
        );

        if (percentageReset >= 1) {
            saveSnapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
            return true;
        }

        return false;
    }

    /**
     * Eases a bone's scale back towards its initial snapshot. See
     * {@link #updateCachedBonePosition(AzBone, AzBoneSnapshot, double, double)} for the return value.
     */
    public static boolean updateCachedBoneScale(
        AzBone bone,
        @Nullable AzBoneSnapshot saveSnapshot,
        double animTime,
        double resetTickLength
    ) {
        if (bone.hasScaleChanged()) {
            return false;
        }

        if (saveSnapshot == null) {
            return true;
        }

        AzBoneSnapshot initialSnapshot = bone.getInitialAzSnapshot();

        if (saveSnapshot.isScaleAnimInProgress()) {
            saveSnapshot.stopScaleAnim(animTime);
        }

        double percentageReset = Math.min(
            (animTime - saveSnapshot.getLastResetScaleTick()) / resetTickLength,
            1
        );

        bone.setScaleX(
            (float) Interpolations.lerp(saveSnapshot.getScaleX(), initialSnapshot.getScaleX(), percentageReset)
        );
        bone.setScaleY(
            (float) Interpolations.lerp(saveSnapshot.getScaleY(), initialSnapshot.getScaleY(), percentageReset)
        );
        bone.setScaleZ(
            (float) Interpolations.lerp(saveSnapshot.getScaleZ(), initialSnapshot.getScaleZ(), percentageReset)
        );

        if (percentageReset >= 1) {
            saveSnapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
            return true;
        }

        return false;
    }

    /**
     * Looks up the bone's snapshot by name and delegates to
     * {@link #updateCachedBonePosition(AzBone, AzBoneSnapshot, double, double)}.
     */
    public static void updateCachedBonePosition(
        AzBone bone,
        Map<String, AzBoneSnapshot> boneSnapshots,
        double animTime,
        double resetTickLength
    ) {
        if (!bone.hasPositionChanged()) {
            updateCachedBonePosition(bone, boneSnapshots.get(bone.getName()), animTime, resetTickLength);
        }
    }

    /**
     * Looks up the bone's snapshot by name and delegates to
     * {@link #updateCachedBoneRotation(AzBone, AzBoneSnapshot, double, double)}.
     */
    public static void updateCachedBoneRotation(
        AzBone bone,
        Map<String, AzBoneSnapshot> boneSnapshots,
        double animTime,
        double resetTickLength
    ) {
        if (!bone.hasRotationChanged()) {
            updateCachedBoneRotation(bone, boneSnapshots.get(bone.getName()), animTime, resetTickLength);
        }
    }

    /**
     * Looks up the bone's snapshot by name and delegates to
     * {@link #updateCachedBoneScale(AzBone, AzBoneSnapshot, double, double)}.
     */
    public static void updateCachedBoneScale(
        AzBone bone,
        Map<String, AzBoneSnapshot> boneSnapshots,
        double animTime,
        double resetTickLength
    ) {
        if (!bone.hasScaleChanged()) {
            updateCachedBoneScale(bone, boneSnapshots.get(bone.getName()), animTime, resetTickLength);
        }
    }
}
