package mod.azure.azurelib.common.internal.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.common.hitbox.client.AzHitboxClientHooks;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_AzHitboxOffscreen {

    @Inject(method = "tick", at = @At("TAIL"))
    private void azurelib$captureOffscreenHitboxes(CallbackInfo ci) {
        AzHitboxClientHooks.tickOffscreen();
    }
}
