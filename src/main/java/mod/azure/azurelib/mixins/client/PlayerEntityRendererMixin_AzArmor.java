package mod.azure.azurelib.mixins.client;

import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.client.AzRenderTick;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin_AzArmor {

    @Inject(
        method = "bindTexture(Lnet/minecraft/entity/mob/player/PlayerEntity;IF)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void azurelib$renderAzArmor(
        PlayerEntity playerEntity,
        int pass,
        float tickDelta,
        CallbackInfoReturnable<Boolean> cir
    ) {
        ItemStack stack = Mc.armor(playerEntity, 3 - pass);
        AzArmorRenderer renderer = stack == null ? null : AzArmorRendererRegistry.getOrNull(Mc.item(stack));
        if (renderer == null) {
            return;
        }
        AzRenderTick.set(tickDelta);
        renderer.rendererPipeline()
            .armorModel()
            .renderArmor(playerEntity, stack, AzEquipmentSlot.fromArmorType(pass), tickDelta);
        cir.setReturnValue(false);
    }
}
