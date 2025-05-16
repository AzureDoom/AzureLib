package mod.azure.azurelib.rewrite.render.armor.bone;

import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.model.AzBone;

public class AzDefaultArmorBoneProvider implements AzArmorBoneProvider {

    public AzBone getHeadBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_HEAD_NAME);
    }

    public AzBone getBodyBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_BODY_NAME);
    }

    public AzBone getRightArmBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_RIGHT_ARM_NAME);
    }

    public AzBone getLeftArmBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_LEFT_ARM_NAME);
    }

    public AzBone getRightLegBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_RIGHT_LEG_NAME);
    }

    public AzBone getLeftLegBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_LEFT_LEG_NAME);
    }

    public AzBone getRightBootBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_RIGHT_BOOT_NAME);
    }

    public AzBone getLeftBootBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_LEFT_BOOT_NAME);
    }
}
