package mod.azure.azurelib.mixins;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.render.AzFrameArena;

/**
 * Marks the end of a frame for {@link AzFrameArena}. {@code GameRenderer#render} draws the level (including the
 * first-person hand pass) and then the GUI (including picture-in-picture entity renders), so once it returns every
 * vertex recording extracted this frame has been replayed.
 * <p>
 * Not required: if the target ever moves, the arena notices the missing frame end and turns pooling off instead of the
 * game failing to start.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin_AzFrameArena {

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void azurelib$endFrame(CallbackInfo ci) {
        AzFrameArena.endFrame();
    }
}
