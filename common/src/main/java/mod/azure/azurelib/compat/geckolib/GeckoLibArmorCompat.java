package mod.azure.azurelib.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.platform.Services;

public final class GeckoLibArmorCompat {

    public static final String MOD_ID = "geckolib3";

    private static Boolean isLoaded;

    public static boolean isLoaded() {
        if (isLoaded == null) {
            isLoaded = Services.PLATFORM.isModLoaded(MOD_ID);
        }
        return isLoaded;
    }

    public static @Nullable HumanoidModel<?> getArmorBaseModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> defaultModel
    ) {
        if (!isLoaded()) {
            return null;
        }

        return Services.clientPlatform().getGeckoLibArmorBaseModel(entity, stack, slot, defaultModel);
    }

    public static void renderArmorPiece(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> baseModel,
        ModelPart modelPart,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay
    ) {
        if (!isLoaded()) {
            return;
        }

        Services.clientPlatform()
            .renderGeckoLibArmorPiece(
                entity,
                stack,
                slot,
                baseModel,
                modelPart,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
            );
    }

    private GeckoLibArmorCompat() { /* NO-OP */}
}
