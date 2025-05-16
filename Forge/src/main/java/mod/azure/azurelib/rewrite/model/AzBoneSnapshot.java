/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */

package mod.azure.azurelib.rewrite.model;

import net.minecraft.client.renderer.Vector3f;

/**
 * A state monitoring class for a given {@link AzBone}.<br>
 */
public class AzBoneSnapshot {

    private final AzBone bone;

    private final Vector3f offsetPosition;

    private final Vector3f rotation;

    private final Vector3f scale;

    private double lastResetRotationTick = 0;

    private double lastResetPositionTick = 0;

    private double lastResetScaleTick = 0;

    private boolean rotAnimInProgress = true;

    private boolean posAnimInProgress = true;

    private boolean scaleAnimInProgress = true;

    public AzBoneSnapshot(AzBone bone) {
        this.bone = bone;
        this.offsetPosition = new Vector3f(bone.getPosX(), bone.getPosY(), bone.getPosZ());
        this.rotation = new Vector3f(bone.getRotX(), bone.getRotY(), bone.getRotZ());
        this.scale = new Vector3f(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static AzBoneSnapshot copy(AzBoneSnapshot snapshot) {
        AzBoneSnapshot newSnapshot = new AzBoneSnapshot(snapshot.bone);

        newSnapshot.offsetPosition.set(
            snapshot.offsetPosition.getX(),
            snapshot.offsetPosition.getY(),
            snapshot.offsetPosition.getZ()
        );
        newSnapshot.rotation.set(snapshot.rotation.getX(), snapshot.rotation.getY(), snapshot.rotation.getZ());
        newSnapshot.scale.set(snapshot.scale.getX(), snapshot.scale.getY(), snapshot.scale.getZ());

        return newSnapshot;
    }

    public AzBone getBone() {
        return this.bone;
    }

    public float getScaleX() {
        return this.scale.getX();
    }

    public float getScaleY() {
        return this.scale.getY();
    }

    public float getScaleZ() {
        return this.scale.getZ();
    }

    public float getOffsetX() {
        return this.offsetPosition.getX();
    }

    public float getOffsetY() {
        return this.offsetPosition.getY();
    }

    public float getOffsetZ() {
        return this.offsetPosition.getZ();
    }

    public float getRotX() {
        return this.rotation.getX();
    }

    public float getRotY() {
        return this.rotation.getY();
    }

    public float getRotZ() {
        return this.rotation.getZ();
    }

    public double getLastResetRotationTick() {
        return this.lastResetRotationTick;
    }

    public double getLastResetPositionTick() {
        return this.lastResetPositionTick;
    }

    public double getLastResetScaleTick() {
        return this.lastResetScaleTick;
    }

    public boolean isRotAnimInProgress() {
        return this.rotAnimInProgress;
    }

    public boolean isPosAnimInProgress() {
        return this.posAnimInProgress;
    }

    public boolean isScaleAnimInProgress() {
        return this.scaleAnimInProgress;
    }

    /**
     * Update the scale state of this snapshot
     */
    public void updateScale(float scaleX, float scaleY, float scaleZ) {
        scale.set(scaleX, scaleY, scaleZ);
    }

    /**
     * Update the offset state of this snapshot
     */
    public void updateOffset(float offsetX, float offsetY, float offsetZ) {
        offsetPosition.set(offsetX, offsetY, offsetZ);
    }

    /**
     * Update the rotation state of this snapshot
     */
    public void updateRotation(float rotX, float rotY, float rotZ) {
        rotation.set(rotX, rotY, rotZ);
    }

    public void startPosAnim() {
        this.posAnimInProgress = true;
    }

    public void stopPosAnim(double tick) {
        this.posAnimInProgress = false;
        this.lastResetPositionTick = tick;
    }

    public void startRotAnim() {
        this.rotAnimInProgress = true;
    }

    public void stopRotAnim(double tick) {
        this.rotAnimInProgress = false;
        this.lastResetRotationTick = tick;
    }

    public void startScaleAnim() {
        this.scaleAnimInProgress = true;
    }

    public void stopScaleAnim(double tick) {
        this.scaleAnimInProgress = false;
        this.lastResetScaleTick = tick;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return this.bone.getName().hashCode();
    }
}
