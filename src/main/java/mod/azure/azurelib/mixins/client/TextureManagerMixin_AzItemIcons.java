package mod.azure.azurelib.mixins.client;

import net.minecraft.client.render.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.render.item.icon.AzItemIcons;

/**
 * Installs queued {@link AzItemIcons} once the game's texture manager exists. That happens while the game starts, after
 * every mod's items are registered, so free icon slots can be picked safely.
 */
@Mixin(TextureManager.class)
public abstract class TextureManagerMixin_AzItemIcons {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void azurelib$installItemIcons(CallbackInfo ci) {
        AzItemIcons.onTextureManagerCreated((TextureManager) (Object) this);
    }
}
