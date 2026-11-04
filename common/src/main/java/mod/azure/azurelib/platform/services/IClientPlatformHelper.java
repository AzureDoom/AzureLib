package mod.azure.azurelib.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IClientPlatformHelper {

    String getArmorTexture(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        String defaultTexture,
        @Nullable String type
    );

    @Nullable
    HumanoidModel<?> getArmorModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    );

    @Nullable
    HumanoidModel<?> getGeckoLibArmorBaseModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    );

    void renderGeckoLibArmorPiece(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> baseModel,
        ModelPart modelPart,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay
    );
}
