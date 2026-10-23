package mod.azure.azurelib.neoforge.platform;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.platform.services.IClientPlatformHelper;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public ResourceLocation getArmorTexture(
        LivingEntity entity,
        ItemStack stack,
        ArmorMaterial.Layer layer,
        boolean innerModel,
        EquipmentSlot slot
    ) {
        return ClientHooks.getArmorTexture(entity, stack, layer, innerModel, slot);
    }

    @Override
    public int getArmorLayerColor(LivingEntity entity, ItemStack stack, ArmorMaterial.Layer layer, int layerIndex) {
        var extensions = IClientItemExtensions.of(stack);
        int fallback = extensions.getDefaultDyeColor(stack);
        return extensions.getArmorLayerTintColor(stack, entity, layer, layerIndex, fallback);
    }

    @Override
    public @Nullable HumanoidModel<?> getArmorModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    ) {
        Model model = ClientHooks.getArmorModel(entity, stack, slot, fallback);
        return model instanceof HumanoidModel<?> humanoid ? humanoid : null;
    }
}
