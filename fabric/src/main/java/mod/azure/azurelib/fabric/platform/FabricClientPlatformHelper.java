package mod.azure.azurelib.fabric.platform;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import mod.azure.azurelib.common.platform.services.IClientPlatformHelper;

public class FabricClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public ResourceLocation getArmorTexture(
        LivingEntity entity,
        ItemStack stack,
        ArmorMaterial.Layer layer,
        boolean innerModel,
        EquipmentSlot slot
    ) {
        return layer.texture(innerModel);
    }

    @Override
    public int getArmorLayerColor(LivingEntity entity, ItemStack stack, ArmorMaterial.Layer layer, int layerIndex) {
        if (!layer.dyeable()) {
            return -1;
        }
        return stack.is(ItemTags.DYEABLE)
            ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(stack, -6265536))
            : -1;
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
}
