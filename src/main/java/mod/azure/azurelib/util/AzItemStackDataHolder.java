package mod.azure.azurelib.util;

import net.minecraft.nbt.NbtCompound;

import javax.annotation.Nullable;

public interface AzItemStackDataHolder {

    NbtCompound azurelib$getOrCreateData();

    @Nullable
    NbtCompound azurelib$getDataOrNull();

    void azurelib$setData(@Nullable NbtCompound data);
}
