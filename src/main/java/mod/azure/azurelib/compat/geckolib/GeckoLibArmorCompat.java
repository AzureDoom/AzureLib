package mod.azure.azurelib.compat.geckolib;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.render.vertex.PoseStack;

public final class GeckoLibArmorCompat {

    public static final String MOD_ID = "geckolib3";

    private static Boolean isLoaded;

    private GeckoLibArmorCompat() {}

    public static boolean isLoaded() {
        if (isLoaded == null) {
            isLoaded = Services.PLATFORM.isModLoaded(MOD_ID);
        }
        return isLoaded;
    }

    @Nullable
    public static ModelBiped getArmorModel(
        EntityLivingBase entity,
        ItemStack stack,
        EntityEquipmentSlot slot,
        ModelBiped defaultModel
    ) {
        if (!isLoaded()) {
            return null;
        }

        return GeckoLibArmorBridge.getArmorModel(entity, stack, slot, defaultModel);
    }

    public static void renderArmorPiece(
        ModelBiped geoArmorModel,
        EntityEquipmentSlot slot,
        ModelRenderer modelPart,
        PoseStack poseStack,
        int packedLight,
        float ageInTicks
    ) {
        if (!isLoaded()) {
            return;
        }

        GeckoLibArmorBridge.renderArmorPiece(geoArmorModel, slot, modelPart, poseStack, packedLight, ageInTicks);
    }
}
