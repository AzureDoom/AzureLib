package mod.azure.azurelib.animation;

import java.util.Map;

import mod.azure.azurelib.core.utils.Interpolations;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

public class AzCachedBoneUpdateUtil {

    /**
     * Updates the cached position of a given bone by interpolating its offsets towards its initial snapshot. Stops
     * ongoing position animations if necessary and updates the bone's position based on the reset percentage.
     *
     * @param bone            the bone whose position is to be updated
     * @param boneSnapshots   a map containing snapshots of bones by their names
     * @param animTime        the current animation time
     * @param resetTickLength the duration over which the position reset occurs
     */
    public static void updateCachedBonePosition(
        AzBone bone,
        Map<String, AzBoneSnapshot> boneSnapshots,
        double animTime,
        double resetTickLength
    ) {
        if (bone.hasPositionChanged()) {
            return;
        }

        var initialSnapshot = bone.getInitialAzSnapshot();
        var saveSnapshot = boneSnapshots.get(bone.getName());

        if (saveSnapshot == null) {
            return;
        }

        if (saveSnapshot.isPosAnimInProgress()) {
            saveSnapshot.stopPosAnim(animTime);
        }

        var percentageReset = Math.min(
            (animTime - saveSnapshot.getLastResetPositionTick()) / resetTickLength,
            1
        );

        bone.setPosX(
            (float) Interpolations.lerp(
                saveSnapshot.getOffsetX(),
                initialSnapshot.getOffsetX(),
                percentageReset
            )
        );
        bone.setPosY(
            (float) Interpolations.lerp(
                saveSnapshot.getOffsetY(),
                initialSnapshot.getOffsetY(),
                percentageReset
            )
        );
        bone.setPosZ(
            (float) Interpolations.lerp(
                saveSnapshot.getOffsetZ(),
                initialSnapshot.getOffsetZ(),
                percentageReset
            )
        );

        if (percentageReset >= 1) {
            saveSnapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
        }
    }

    /**
     * Updates the cached rotation of a given bone by interpolating its rotation values towards its initial snapshot.
     * Stops any ongoing rotation animations if necessary and updates the bone's rotation based on the reset percentage.
     *
     * @param bone            the bone whose rotation is to be updated
     * @param boneSnapshots   a map containing snapshots of bones by their names
     * @param animTime        the current animation time
     * @param resetTickLength the duration over which the rotation reset occurs
     */
    public static void updateCachedBoneRotation(
        AzBone bone,
        Map<String, AzBoneSnapshot> boneSnapshots,
        double animTime,
        double resetTickLength
    ) {
        if (bone.hasRotationChanged()) {
            return;
        }

        var initialSnapshot = bone.getInitialAzSnapshot();
        var saveSnapshot = boneSnapshots.get(bone.getName());

        if (saveSnapshot == null) {
            return;
        }

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
        }
    }

    /**
     * Updates the cached scale of a given bone by interpolating its scale values towards its initial snapshot. Stops
     * any ongoing scale animations if necessary and updates the bone's scale based on the reset percentage.
     *
     * @param bone            the bone whose scale is to be updated
     * @param boneSnapshots   a map containing snapshots of bones by their names
     * @param animTime        the current animation time
     * @param resetTickLength the duration over which the scale reset occurs
     */
    public static void updateCachedBoneScale(
        AzBone bone,
        Map<String, AzBoneSnapshot> boneSnapshots,
        double animTime,
        double resetTickLength
    ) {
        if (bone.hasScaleChanged()) {
            return;
        }

        var initialSnapshot = bone.getInitialAzSnapshot();
        var saveSnapshot = boneSnapshots.get(bone.getName());

        if (saveSnapshot == null) {
            return;
        }

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
        }
    }
}
