package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.render.item.AzItemDisplayContextCapture;

/**
 * Exposes the item's {@link ItemDisplayContext} to AzureLib's special item renderer, which 26.x no longer passes it to.
 * Captured for the duration of the render state's submit and restored afterward, so nested item renders work.
 */
@Mixin(ItemStackRenderState.class)
public abstract class ItemStackRenderStateMixin_AzDisplayContext {

    @Shadow
    ItemDisplayContext displayContext;

    @Inject(method = "submit", at = @At("HEAD"))
    private void azurelib$captureDisplayContext(
        CallbackInfo ci,
        @Share("previous_display_context") LocalRef<ItemDisplayContext> previous
    ) {
        previous.set(AzItemDisplayContextCapture.set(this.displayContext));
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void azurelib$restoreDisplayContext(
        CallbackInfo ci,
        @Share("previous_display_context") LocalRef<ItemDisplayContext> previous
    ) {
        AzItemDisplayContextCapture.set(previous.get());
    }
}
