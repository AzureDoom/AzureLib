package mod.azure.azurelib.mixins.client;

import net.minecraft.client.render.ItemInHandRenderer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.render.item.AzItemRendererAdapter;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.util.client.AzRenderTick;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin_AzItems {

    @Unique
    private static boolean azurelib$firstPerson;

    @Inject(method = "renderHand(F)V", at = @At("HEAD"))
    private void azurelib$beginFirstPerson(float tickDelta, CallbackInfo ci) {
        azurelib$firstPerson = true;
        AzRenderTick.set(tickDelta);
    }

    @Inject(method = "renderHand(F)V", at = @At("RETURN"))
    private void azurelib$endFirstPerson(float tickDelta, CallbackInfo ci) {
        azurelib$firstPerson = false;
    }

    @Inject(
        method = "render(Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void azurelib$renderAzItem(MobEntity mob, ItemStack itemInHand, CallbackInfo ci) {
        AzItemRendererAdapter adapter = AzItemRendererRegistry.adapterOrNull(Mc.item(itemInHand));
        AzItemRendererAdapter.ItemRenderType type = azurelib$firstPerson
            ? AzItemRendererAdapter.ItemRenderType.EQUIPPED_FIRST_PERSON
            : AzItemRendererAdapter.ItemRenderType.EQUIPPED;
        if (adapter == null || !adapter.handles(type)) {
            return;
        }
        adapter.renderItem(type, itemInHand);
        ci.cancel();
    }
}
