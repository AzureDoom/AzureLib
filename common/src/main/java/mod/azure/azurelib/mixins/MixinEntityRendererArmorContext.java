package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import mod.azure.azurelib.render.armor.AzArmorRenderStateCache;

/**
 * Captures the entity associated with each 26.2 render state so AzureLib's armor renderer can continue supporting its
 * entity-aware configuration callbacks during the later HumanoidArmorLayer submit phase.
 */
@Mixin(value = EntityRenderer.class, priority = 5000)
public abstract class MixinEntityRendererArmorContext<T extends Entity, S extends EntityRenderState> {

    @WrapMethod(
        method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
    )
    private S azurelib$captureArmorEntity(T entity, float partialTicks, Operation<S> original) {
        S renderState = original.call(entity, partialTicks);
        AzArmorRenderStateCache.capture(renderState, entity);
        return renderState;
    }
}
