package mod.azure.azurelib.common.internal.mixins;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.common.hitbox.AzHitboxInteractions;

@Mixin(Player.class)
public abstract class PlayerMixin_AzHitboxInteract {

    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void azurelib$interactHitbox(
        Entity target,
        InteractionHand hand,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        var result = AzHitboxInteractions.interact((Player) (Object) this, target, hand);

        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }
}
