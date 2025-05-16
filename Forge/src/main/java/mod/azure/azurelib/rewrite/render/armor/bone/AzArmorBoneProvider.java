package mod.azure.azurelib.rewrite.render.armor.bone;



import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.model.AzBone;

import javax.annotation.Nullable;

public interface AzArmorBoneProvider {

    String BONE_ARMOR_BODY_NAME = "armorBody";

    String BONE_ARMOR_HEAD_NAME = "armorHead";

    String BONE_ARMOR_LEFT_ARM_NAME = "armorLeftArm";

    String BONE_ARMOR_RIGHT_ARM_NAME = "armorRightArm";

    String BONE_ARMOR_LEFT_BOOT_NAME = "armorLeftBoot";

    String BONE_ARMOR_RIGHT_BOOT_NAME = "armorRightBoot";

    String BONE_ARMOR_LEFT_LEG_NAME = "armorLeftLeg";

    String BONE_ARMOR_RIGHT_LEG_NAME = "armorRightLeg";

    /**
     * Returns the 'head' GeoBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the head model piece, or null if not using it
     */
    
    AzBone getHeadBone(AzBakedModel model);

    /**
     * Returns the 'body' GeoBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the body model piece, or null if not using it
     */
    
    AzBone getBodyBone(AzBakedModel model);

    /**
     * Returns the 'right arm' GeoBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the right arm model piece, or null if not using it
     */
    
    AzBone getRightArmBone(AzBakedModel model);

    /**
     * Returns the 'left arm' GeoBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the left arm model piece, or null if not using it
     */
    
    AzBone getLeftArmBone(AzBakedModel model);

    /**
     * Returns the 'right leg' GeoBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the right leg model piece, or null if not using it
     */
    
    AzBone getRightLegBone(AzBakedModel model);

    /**
     * Returns the 'left leg' GeoBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the left leg model piece, or null if not using it
     */
    
    AzBone getLeftLegBone(AzBakedModel model);

    /**
     * Returns the 'right boot' GeoBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the right boot model piece, or null if not using it
     */
    
    AzBone getRightBootBone(AzBakedModel model);

    /**
     * Returns the 'left boot' GeoBone from this model.<br>
     * Override if your geo model has different bone names for these bones
     *
     * @return The bone for the left boot model piece, or null if not using it
     */
    
    AzBone getLeftBootBone(AzBakedModel model);
}
