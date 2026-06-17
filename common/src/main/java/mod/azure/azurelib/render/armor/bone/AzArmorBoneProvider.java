package mod.azure.azurelib.render.armor.bone;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;

public interface AzArmorBoneProvider {

    String BONE_ARMOR_BODY_NAME = "armorBody";

    String BONE_ARMOR_HEAD_NAME = "armorHead";

    String BONE_ARMOR_LEFT_ARM_NAME = "armorLeftArm";

    String BONE_ARMOR_RIGHT_ARM_NAME = "armorRightArm";

    String BONE_ARMOR_LEFT_BOOT_NAME = "armorLeftBoot";

    String BONE_ARMOR_RIGHT_BOOT_NAME = "armorRightBoot";

    String BONE_ARMOR_LEFT_LEG_NAME = "armorLeftLeg";

    String BONE_ARMOR_RIGHT_LEG_NAME = "armorRightLeg";

    String BONE_ARMOR_WAIST_NAME = "armorWaist";

    /**
     * Returns the 'head' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the head model piece, or null if not using it
     */
    @Nullable
    AzBone getHeadBone(AzBakedModel model);

    /**
     * Returns the 'body' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the body model piece, or null if not using it
     */
    @Nullable
    AzBone getBodyBone(AzBakedModel model);

    /**
     * Returns the 'right arm' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the right arm model piece, or null if not using it
     */
    @Nullable
    AzBone getRightArmBone(AzBakedModel model);

    /**
     * Returns the 'left arm' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the left arm model piece, or null if not using it
     */
    @Nullable
    AzBone getLeftArmBone(AzBakedModel model);

    /**
     * Returns the 'right leg' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the right leg model piece, or null if not using it
     */
    @Nullable
    AzBone getRightLegBone(AzBakedModel model);

    /**
     * Returns the 'left leg' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the left leg model piece, or null if not using it
     */
    @Nullable
    AzBone getLeftLegBone(AzBakedModel model);

    /**
     * Returns the 'right boot' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the right boot model piece, or null if not using it
     */
    @Nullable
    AzBone getRightBootBone(AzBakedModel model);

    /**
     * Returns the 'left boot' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the left boot model piece, or null if not using it
     */
    @Nullable
    AzBone getLeftBootBone(AzBakedModel model);

    /**
     * Returns the 'waist' AzBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the waist model piece, or null if not using it
     */
    @Nullable
    AzBone getWaistBone(AzBakedModel model);
}
