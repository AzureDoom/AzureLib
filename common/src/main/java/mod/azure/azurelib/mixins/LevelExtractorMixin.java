package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;

import mod.azure.azurelib.render.AzRenderContext;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {

    @WrapMethod(method = "extract", require = 1)
    private void azurelib$markLevelExtract(
        DeltaTracker deltaTracker,
        Camera camera,
        float worldPartialTicks,
        Operation<Void> original
    ) {
        AzRenderContext.beginLevelExtract();
        try {
            original.call(deltaTracker, camera, worldPartialTicks);
        } finally {
            AzRenderContext.endLevelExtract();
        }
    }
}
