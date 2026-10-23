package mod.azure.azurelib.mixins;

import mod.azure.azurelib.compat.morph.WalkersMorphBridge;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_AzMorphCompat {

    @Inject(method = "tick", at = @At("RETURN"))
    private void azurelib$tickMorphCompat(CallbackInfo callback) {
        WalkersMorphBridge.clientTick();
    }
}
