package mod.azure.azurelib.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.compat.geckolib.FabricGeckoLibArmor;
import mod.azure.azurelib.compat.geckolib.GeckoLibArmorCompat;
import mod.azure.azurelib.platform.services.IClientPlatformHelper;

public class FabricClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public String getArmorTexture(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        String defaultTexture,
        @Nullable String type
    ) {
        return defaultTexture;
    }

    @Override
    public HumanoidModel<?> getArmorModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    ) {
        return fallback;
    }

    @Override
    public @Nullable HumanoidModel<?> getGeckoLibArmorBaseModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    ) {
        if (!GeckoLibArmorCompat.isLoaded()) {
            return null;
        }

        return FabricGeckoLibArmor.getBaseModel(entity, stack, slot, fallback);
    }

    @Override
    public void renderGeckoLibArmorPiece(
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
        if (!GeckoLibArmorCompat.isLoaded()) {
            return;
        }

        FabricGeckoLibArmor.renderArmorPiece(
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
}
