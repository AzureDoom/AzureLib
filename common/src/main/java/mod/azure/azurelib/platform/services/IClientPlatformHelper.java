package mod.azure.azurelib.platform.services;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IClientPlatformHelper {

    Identifier getArmorTexture(
        ItemStack stack,
        EquipmentClientInfo.LayerType layerType,
        EquipmentClientInfo.Layer layer,
        Identifier defaultTexture
    );

    /**
     * @param fallbackColor The vanilla-computed color for this layer. Return 0 to skip the layer.
     */
    int getArmorLayerColor(
        ItemStack stack,
        EquipmentClientInfo.Layer layer,
        int layerIndex,
        int fallbackColor
    );

    @Nullable
    HumanoidModel<?> getArmorModel(
        ItemStack stack,
        EquipmentClientInfo.LayerType layerType,
        HumanoidModel<?> fallback
    );
}
