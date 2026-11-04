package mod.azure.azurelib.compat.geckolib;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

import javax.annotation.Nullable;

import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.client.RenderUtils;

@SuppressWarnings("rawtypes")
final class GeckoLibArmorBridge {

    private static final ModelBiped REST_POSE = new ModelBiped(1.0F);

    private GeckoLibArmorBridge() {}

    @Nullable
    static ModelBiped getArmorModel(
        EntityLivingBase entity,
        ItemStack stack,
        EntityEquipmentSlot slot,
        ModelBiped defaultModel
    ) {
        ModelBiped model = ForgeHooksClient.getArmorModel(entity, stack, slot, defaultModel);

        return model instanceof GeoArmorRenderer ? model : null;
    }

    static void renderArmorPiece(
        ModelBiped geoArmorModel,
        EntityEquipmentSlot slot,
        ModelRenderer modelPart,
        PoseStack poseStack,
        int packedLight,
        float ageInTicks
    ) {
        if (!(geoArmorModel instanceof GeoArmorRenderer)) {
            return;
        }

        GeoArmorRenderer renderer = (GeoArmorRenderer) geoArmorModel;
        ModelRenderer restPart = getRestPart(geoArmorModel, modelPart);

        hideOtherBones(renderer, slot, modelPart);

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, packedLight & 0xFFFF, packedLight >> 16);
        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(poseStack.last());

        if (restPart != null) {
            GlStateManager.translate(
                (modelPart.rotationPointX - restPart.rotationPointX) * 0.0625F,
                (modelPart.rotationPointY - restPart.rotationPointY) * 0.0625F,
                (modelPart.rotationPointZ - restPart.rotationPointZ) * 0.0625F
            );
        }

        renderer.render(ageInTicks);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private static void hideOtherBones(GeoArmorRenderer renderer, EntityEquipmentSlot slot, ModelRenderer modelPart) {
        String keep = getBoneForPart(renderer, slot, modelPart);

        if (keep == null) {
            return;
        }

        for (String bone : getBonesForSlot(renderer, slot)) {
            if (bone != null && !bone.equals(keep)) {
                renderer.getGeoModelProvider().getBone(bone).setHidden(true);
            }
        }
    }

    @Nullable
    private static String getBoneForPart(GeoArmorRenderer renderer, EntityEquipmentSlot slot, ModelRenderer part) {
        switch (slot) {
            case HEAD:
                return part == renderer.bipedHead ? renderer.headBone : null;
            case CHEST:
                if (part == renderer.bipedBody)
                    return renderer.bodyBone;
                if (part == renderer.bipedRightArm)
                    return renderer.rightArmBone;
                if (part == renderer.bipedLeftArm)
                    return renderer.leftArmBone;
                return null;
            case LEGS:
                if (part == renderer.bipedRightLeg)
                    return renderer.rightLegBone;
                if (part == renderer.bipedLeftLeg)
                    return renderer.leftLegBone;
                return null;
            case FEET:
                if (part == renderer.bipedRightLeg)
                    return renderer.rightBootBone;
                if (part == renderer.bipedLeftLeg)
                    return renderer.leftBootBone;
                return null;
            default:
                return null;
        }
    }

    private static String[] getBonesForSlot(GeoArmorRenderer renderer, EntityEquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return new String[] { renderer.headBone };
            case CHEST:
                return new String[] { renderer.bodyBone, renderer.rightArmBone, renderer.leftArmBone };
            case LEGS:
                return new String[] { renderer.rightLegBone, renderer.leftLegBone };
            case FEET:
                return new String[] { renderer.rightBootBone, renderer.leftBootBone };
            default:
                return new String[0];
        }
    }

    @Nullable
    private static ModelRenderer getRestPart(ModelBiped model, ModelRenderer part) {
        if (part == model.bipedHead)
            return REST_POSE.bipedHead;
        if (part == model.bipedBody)
            return REST_POSE.bipedBody;
        if (part == model.bipedRightArm)
            return REST_POSE.bipedRightArm;
        if (part == model.bipedLeftArm)
            return REST_POSE.bipedLeftArm;
        if (part == model.bipedRightLeg)
            return REST_POSE.bipedRightLeg;
        if (part == model.bipedLeftLeg)
            return REST_POSE.bipedLeftLeg;
        return null;
    }
}
