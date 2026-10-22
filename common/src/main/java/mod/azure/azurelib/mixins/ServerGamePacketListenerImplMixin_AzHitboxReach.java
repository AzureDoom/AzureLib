package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.hitbox.AzHitboxes;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_AzHitboxReach {

    @WrapOperation(
        method = "handleInteract", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"
        ), require = 0
    )
    private AABB azurelib$hitboxInteractionBox(Entity target, Operation<AABB> original) {
        return AzHitboxes.getInteractionBox(target, original.call(target));
    }
}
