package mod.azure.azurelib.neoforge.platform;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.platform.services.IClientPlatformHelper;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public Identifier getArmorTexture(
        ItemStack stack,
        EquipmentClientInfo.LayerType layerType,
        EquipmentClientInfo.Layer layer,
        Identifier defaultTexture
    ) {
        return ClientHooks.getArmorTexture(stack, layerType, layer, defaultTexture);
    }

    @Override
    public int getArmorLayerColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIndex, int fallbackColor) {
        return IClientItemExtensions.of(stack).getArmorLayerTintColor(stack, layer, layerIndex, fallbackColor);
    }

    @Override
    public @Nullable HumanoidModel<?> getArmorModel(
        ItemStack stack,
        EquipmentClientInfo.LayerType layerType,
        HumanoidModel<?> fallback
    ) {
        Model<?> model = IClientItemExtensions.of(stack).getGenericArmorModel(stack, layerType, fallback);
        return model instanceof HumanoidModel<?> humanoid ? humanoid : null;
    }
}
