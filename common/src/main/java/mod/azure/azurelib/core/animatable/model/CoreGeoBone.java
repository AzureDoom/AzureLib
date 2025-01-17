/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.animatable.model;

import java.util.List;

import mod.azure.azurelib.core.state.BoneSnapshot;

/**
 * Base class for AzureLib {@link CoreGeoModel model} bones.<br>
 * Mostly a placeholder to allow for splitting up core (non-Minecraft) libraries
 *
 * @deprecated
 */
public interface CoreGeoBone {

    String getName();

    CoreGeoBone getParent();

    float getRotX();

    void setRotX(float value);

    float getRotY();

    void setRotY(float value);

    float getRotZ();

    void setRotZ(float value);

    float getPosX();

    void setPosX(float value);

    float getPosY();

    void setPosY(float value);

    float getPosZ();

    void setPosZ(float value);

    float getScaleX();

    void setScaleX(float value);

    float getScaleY();

    void setScaleY(float value);

    float getScaleZ();

    void setScaleZ(float value);

    default void updateRotation(float xRot, float yRot, float zRot) {
        setRotX(xRot);
        setRotY(yRot);
        setRotZ(zRot);
    }

    default void updatePosition(float posX, float posY, float posZ) {
        setPosX(posX);
        setPosY(posY);
        setPosZ(posZ);
    }

    default void updateScale(float scaleX, float scaleY, float scaleZ) {
        setScaleX(scaleX);
        setScaleY(scaleY);
        setScaleZ(scaleZ);
    }

    default void updatePivot(float pivotX, float pivotY, float pivotZ) {
        setPivotX(pivotX);
        setPivotY(pivotY);
        setPivotZ(pivotZ);
    }

    float getPivotX();

    void setPivotX(float value);

    float getPivotY();

    void setPivotY(float value);

    float getPivotZ();

    void setPivotZ(float value);

    boolean isHidden();

    void setHidden(boolean hidden);

    boolean isHidingChildren();

    void setChildrenHidden(boolean hideChildren);

    void saveInitialSnapshot();

    void markScaleAsChanged();

    void markRotationAsChanged();

    void markPositionAsChanged();

    boolean hasScaleChanged();

    boolean hasRotationChanged();

    boolean hasPositionChanged();

    void resetStateChanges();

    BoneSnapshot getInitialSnapshot();

    List<? extends CoreGeoBone> getChildBones();

    default BoneSnapshot saveSnapshot() {
        return new BoneSnapshot(this);
    }
}
