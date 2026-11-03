package mod.azure.azurelib.common.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.platform.Services;

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
        HumanoidModel<LivingEntity> defaultModel
    ) {
        if (!isLoaded()) {
            return null;
        }

        return GeckoLibArmorBridge.getArmorModel(entity, stack, slot, defaultModel);
    }

    public static void renderArmorPiece(
        HumanoidModel<?> geoArmorModel,
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        ModelPart modelPart,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        float partialTick,
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
            bufferSource,
            partialTick,
            packedLight,
            packedOverlay
        );
    }

    private GeckoLibArmorCompat() { /* NO-OP */}
}
