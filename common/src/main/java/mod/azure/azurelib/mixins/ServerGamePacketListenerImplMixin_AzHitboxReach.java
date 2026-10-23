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
        method = "handleInteract",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"
        ),
        require = 0
    )
    private double azurelib$hitboxInteractionReach(
        net.minecraft.server.level.ServerPlayer player,
        Entity target,
        Operation<Double> original
    ) {
        AABB box = AzHitboxes.getInteractionBox(target, target.getBoundingBox());

        double x = Math.max(box.minX, Math.min(player.getX(), box.maxX));
        double y = Math.max(box.minY, Math.min(player.getY(), box.maxY));
        double z = Math.max(box.minZ, Math.min(player.getZ(), box.maxZ));

        return player.distanceToSqr(x, y, z);
    }
}
