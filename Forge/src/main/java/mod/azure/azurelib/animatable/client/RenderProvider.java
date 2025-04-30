package mod.azure.azurelib.animatable.client;

import mod.azure.azurelib.animatable.GeoItem;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Internal interface for safely providing a custom renderer instances at runtime.<br>
 * This can be safely instantiated as a new anonymous class inside your {@link Item} class
 */
@Deprecated()
public interface RenderProvider {
    RenderProvider DEFAULT = new RenderProvider() {};

    static RenderProvider of(ItemStack itemStack) {
        return of(itemStack.getItem());
    }

    static RenderProvider of(Item item) {
        if (item instanceof GeoItem) {
            return (RenderProvider) ((GeoItem) item).getRenderProvider().get();
        }

        return DEFAULT;
    }

    default ItemStackTileEntityRenderer getCustomRenderer() {
        return ItemStackTileEntityRenderer.instance;
    }

    default Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlotType equipmentSlot, BipedModel<LivingEntity> original) {
        BipedModel<LivingEntity> replacement = getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);

        if (replacement != original) {
            original.copyPropertiesTo(replacement);
            return replacement;
        }

        return original;
    }

    default BipedModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlotType equipmentSlot, BipedModel<LivingEntity> original) {
        return original;
    }
}