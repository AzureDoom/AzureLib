package mod.azure.azurelib.render.armor.bone;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;

public class AzDefaultArmorBoneProvider implements AzArmorBoneProvider {

    @Nullable
    public AzBone getHeadBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_HEAD_NAME);
    }

    @Nullable
    public AzBone getBodyBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_BODY_NAME);
    }

    @Nullable
    public AzBone getRightArmBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_RIGHT_ARM_NAME);
    }

    @Nullable
    public AzBone getLeftArmBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_LEFT_ARM_NAME);
    }

    @Nullable
    public AzBone getRightLegBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_RIGHT_LEG_NAME);
    }

    @Nullable
    public AzBone getLeftLegBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_LEFT_LEG_NAME);
    }

    @Nullable
    public AzBone getRightBootBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_RIGHT_BOOT_NAME);
    }

    @Nullable
    public AzBone getLeftBootBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_LEFT_BOOT_NAME);
    }

    @Nullable
    public AzBone getWaistBone(AzBakedModel model) {
        return model.getBoneOrNull(BONE_ARMOR_WAIST_NAME);
    }
}
