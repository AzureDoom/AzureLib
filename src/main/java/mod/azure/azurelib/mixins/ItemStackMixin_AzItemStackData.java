package mod.azure.azurelib.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.util.AzItemIds;
import mod.azure.azurelib.util.AzItemStackDataHolder;

/**
 * Gives Beta item stacks a small AzureLib-owned NBT compound (this replaces StationAPI's per-stack NBT).
 * <p>
 * Persisted alongside the vanilla stack data under {@link #KEY} and copied by {@code copy()}. Stacks that never had
 * AzureLib data write nothing, so other mods' items and old saves are untouched.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzItemStackData implements AzItemStackDataHolder {

    @Unique
    private static final String KEY = "azurelib";

    @Unique
    @Nullable
    private NbtCompound azurelib$data;

    @Override
    public NbtCompound azurelib$getOrCreateData() {
        if (this.azurelib$data == null) {
            this.azurelib$data = new NbtCompound();
        }
        return this.azurelib$data;
    }

    @Override
    public @Nullable NbtCompound azurelib$getDataOrNull() {
        return this.azurelib$data;
    }

    @Override
    public void azurelib$setData(@Nullable NbtCompound data) {
        this.azurelib$data = data;
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void azurelib$writeData(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (this.azurelib$data != null) {
            Mc.nbtPutCompound(nbt, KEY, this.azurelib$data);
        }
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void azurelib$readData(NbtCompound nbt, CallbackInfo ci) {
        this.azurelib$data = Mc.nbtHas(nbt, KEY) ? Mc.nbtGetCompound(nbt, KEY) : null;
    }

    @Inject(method = "copy", at = @At("RETURN"))
    private void azurelib$copyData(CallbackInfoReturnable<ItemStack> cir) {
        if (this.azurelib$data != null && cir.getReturnValue() != null) {
            ((AzItemStackDataHolder) (Object) cir.getReturnValue())
                .azurelib$setData(AzItemIds.copyData(this.azurelib$data));
        }
    }
}
