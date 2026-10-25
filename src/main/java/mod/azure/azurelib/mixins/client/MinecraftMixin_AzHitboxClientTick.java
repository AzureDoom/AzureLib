package mod.azure.azurelib.mixins.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.hitbox.client.AzHitboxClientTicker;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_AzHitboxClientTick {

    @Inject(method = "tick", at = @At("TAIL"), require = 1)
    private void azurelib$tickHitboxes(CallbackInfo ci) {
        AzHitboxClientTicker.tick();
    }
}
