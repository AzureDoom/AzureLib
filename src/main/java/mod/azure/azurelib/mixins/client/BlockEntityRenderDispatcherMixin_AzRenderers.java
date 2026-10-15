package mod.azure.azurelib.mixins.client;

import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.render.registry.AzBlockEntityRendererRegistry;

/**
 * Installs renderers registered through {@link AzBlockEntityRendererRegistry} once vanilla has filled the dispatcher's
 * map and wired its own renderers.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin_AzRenderers {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void azurelib$installRenderers(CallbackInfo ci) {
        AzBlockEntityRendererRegistry.onDispatcherCreated((BlockEntityRenderDispatcher) (Object) this);
    }
}
