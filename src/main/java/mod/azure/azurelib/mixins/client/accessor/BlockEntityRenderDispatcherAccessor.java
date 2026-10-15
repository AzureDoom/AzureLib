package mod.azure.azurelib.mixins.client.accessor;

import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** Access to the dispatcher's class-to-renderer map. */
@Mixin(BlockEntityRenderDispatcher.class)
public interface BlockEntityRenderDispatcherAccessor {

    @SuppressWarnings("rawtypes")
    @Accessor("renderers")
    Map azurelib$getRenderers();
}
