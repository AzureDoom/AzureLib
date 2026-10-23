package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;

import javax.annotation.Nullable;

public final class AzHitboxInteractions {

    private AzHitboxInteractions() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static AzHitboxHit findLookedAt(EntityPlayer player, Entity target, Vec3 eye, Vec3 look) {
        Vec3 end = AzHitboxMath.along(eye, look, AzHitboxes.attackReach(player));

        return AzHitboxes.raycast(target, eye, end, AzHitbox::isPickable)
            .filter(hit -> hit.hitbox().role() == AzHitboxRole.INTERACT)
            .orElse(null);
    }

    public static boolean interact(EntityPlayer player, Entity target) {
        if (!(target instanceof AzHitboxHolder)) {
            return false;
        }

        AzHitboxHit hit = player instanceof EntityPlayerMP
            ? AzHitboxDamageTracker.takeInteract((EntityPlayerMP) player, target)
            : findLookedAt(player, target, AzHitboxMath.eyePosition(player), AzHitboxMath.look(player));

        if (hit == null) {
            return false;
        }

        return ((AzHitboxHolder) target).onHitboxInteract(hit, player);
    }
}
