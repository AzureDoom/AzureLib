package mod.azure.azurelib.common.platform.services;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IClientPlatformHelper {

    ResourceLocation getArmorTexture(
        LivingEntity entity,
        ItemStack stack,
        ArmorMaterial.Layer layer,
        boolean innerModel,
        EquipmentSlot slot
    );

    int getArmorLayerColor(
        LivingEntity entity,
        ItemStack stack,
        ArmorMaterial.Layer layer,
        int layerIndex
    );

    @Nullable
    HumanoidModel<?> getArmorModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    );
}
