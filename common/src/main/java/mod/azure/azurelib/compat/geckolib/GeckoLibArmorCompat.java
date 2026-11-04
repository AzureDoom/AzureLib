package mod.azure.azurelib.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.platform.Services;

public final class GeckoLibArmorCompat {

    public static final String MOD_ID = "geckolib";

    private static Boolean isLoaded;

    public static boolean isLoaded() {
        if (isLoaded == null) {
            isLoaded = Services.PLATFORM.isModLoaded(MOD_ID);
        }
        return isLoaded;
    }

    public static @Nullable HumanoidModel<?> getArmorModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> defaultModel
    ) {
        if (!isLoaded()) {
            return null;
        }

        var model = Services.clientPlatform().getGeckoLibArmorModel(entity, stack, slot, defaultModel);

        return GeckoLibArmorBridge.isGeoArmorRenderer(model) ? model : null;
    }

    public static void renderArmorPiece(
        HumanoidModel<?> geoArmorModel,
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        ModelPart modelPart,
        PoseStack poseStack,
        int packedLight,
        int packedOverlay
    ) {
        if (!isLoaded()) {
            return;
        }

        GeckoLibArmorBridge.renderArmorPiece(
            geoArmorModel,
            entity,
            stack,
            slot,
            modelPart,
            poseStack,
            packedLight,
            packedOverlay
        );
    }

    private GeckoLibArmorCompat() { /* NO-OP */}
}
