package mod.azure.azurelib.mixins.client;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.render.registry.AzEntityRendererRegistry;

/**
 * Installs renderers registered through {@link AzEntityRendererRegistry} once vanilla has filled the dispatcher's map
 * and wired its own renderers, so mod renderers can also replace vanilla ones.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin_AzRenderers {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void azurelib$installRenderers(CallbackInfo ci) {
        AzEntityRendererRegistry.onDispatcherCreated((EntityRenderDispatcher) (Object) this);
    }
}
