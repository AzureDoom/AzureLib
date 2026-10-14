package mod.azure.azurelib.util;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface AzEquipmentHolder {

    @Nullable
    ItemStack getAzEquipment(int slot);
}
