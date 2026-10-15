package mod.azure.azurelib.mixins.client.accessor;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** Access to the dispatcher's class-to-renderer map. */
@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccessor {

    @SuppressWarnings("rawtypes")
    @Accessor("renderers")
    Map azurelib$getRenderers();
}
