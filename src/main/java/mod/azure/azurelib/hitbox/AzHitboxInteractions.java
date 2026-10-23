package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public final class AzHitboxInteractions {

    private AzHitboxInteractions() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static AzHitboxHit findLookedAt(EntityPlayer player, Entity target) {
        Vec3d eye = player.getPositionEyes(1f);
        Vec3d end = eye.add(player.getLook(1f).scale(AzHitboxes.attackReach(player)));

        return AzHitboxes.raycast(target, eye, end, AzHitbox::isPickable)
            .filter(hit -> hit.hitbox().role() == AzHitboxRole.INTERACT)
            .orElse(null);
    }

    public static EnumActionResult interact(EntityPlayer player, Entity target, EnumHand hand) {
        if (!(target instanceof AzHitboxHolder) || player.isSpectator()) {
            return EnumActionResult.PASS;
        }

        AzHitboxHit hit = player instanceof EntityPlayerMP
            ? AzHitboxDamageTracker.takeInteract((EntityPlayerMP) player, target)
            : findLookedAt(player, target);

        if (hit == null) {
            return EnumActionResult.PASS;
        }

        return ((AzHitboxHolder) target).onHitboxInteract(hit, player, hand);
    }
}
