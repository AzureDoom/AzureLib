package mod.azure.azurelib.mixins.client;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.render.item.AzItemRendererAdapter;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.util.client.AzRenderTick;

@Pseudo
@Mixin(
    targets = "net.modificationstation.stationapi.impl.client.arsenic.renderer.render.ArsenicOverlayRenderer",
    remap = false
)
public abstract class ArsenicOverlayRendererMixin_AzItems {

    @Unique
    private static boolean azurelib$firstPerson;

    @Inject(method = "renderItem(F)V", at = @At("HEAD"), require = 0, remap = false)
    private void azurelib$beginFirstPerson(float tickDelta, CallbackInfo ci) {
        azurelib$firstPerson = true;
        AzRenderTick.set(tickDelta);
    }

    @Inject(method = "renderItem(F)V", at = @At("RETURN"), require = 0, remap = false)
    private void azurelib$endFirstPerson(float tickDelta, CallbackInfo ci) {
        azurelib$firstPerson = false;
    }

    @Inject(method = "renderItem3D", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void azurelib$renderAzItem(LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        AzItemRendererAdapter adapter = AzItemRendererRegistry.adapterOrNull(Mc.item(stack));
        AzItemRendererAdapter.ItemRenderType type = azurelib$firstPerson
            ? AzItemRendererAdapter.ItemRenderType.EQUIPPED_FIRST_PERSON
            : AzItemRendererAdapter.ItemRenderType.EQUIPPED;
        if (adapter == null || !adapter.handles(type)) {
            return;
        }
        adapter.renderItem(type, stack);
        ci.cancel();
    }
}
