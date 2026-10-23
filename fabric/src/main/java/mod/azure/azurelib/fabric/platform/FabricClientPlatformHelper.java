package mod.azure.azurelib.fabric.platform;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.platform.services.IClientPlatformHelper;

public class FabricClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public Identifier getArmorTexture(
        ItemStack stack,
        EquipmentClientInfo.LayerType layerType,
        EquipmentClientInfo.Layer layer,
        Identifier defaultTexture
    ) {
        return defaultTexture;
    }

    @Override
    public int getArmorLayerColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIndex, int fallbackColor) {
        return fallbackColor;
    }

    @Override
    public HumanoidModel<?> getArmorModel(
        ItemStack stack,
        EquipmentClientInfo.LayerType layerType,
        HumanoidModel<?> fallback
    ) {
        return fallback;
    }
}
