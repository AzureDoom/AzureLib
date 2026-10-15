package mod.azure.azurelib.mixins.client;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.client.sound.AzModSounds;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin_AzModSounds {

    @Inject(method = "load(Lnet/minecraft/client/options/GameOptions;)V", at = @At("RETURN"))
    private void azurelib$registerModSounds(GameOptions options, CallbackInfo ci) {
        AzModSounds.registerAll((SoundEngine) (Object) this);
    }
}
