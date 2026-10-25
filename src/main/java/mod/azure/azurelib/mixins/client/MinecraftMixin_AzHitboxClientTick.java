package mod.azure.azurelib.mixins.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.hitbox.client.AzHitboxClientTicker;

/** Beta's stand-in for 1.7.10's {@code ClientTickEvent} registration. MCP: {@code Minecraft#runTick}. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin_AzHitboxClientTick {

    @Inject(method = "tick", at = @At("TAIL"))
    private void azurelib$tickHitboxes(CallbackInfo ci) {
        AzHitboxClientTicker.tick();
    }
}
