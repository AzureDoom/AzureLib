package mod.azure.azurelib.hitbox;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;

public interface AzHitboxHolder {

    AzHitboxSet getHitboxSet();

    default float onHitboxHit(AzHitboxHit hit, DamageSource source, float amount) {
        return amount * hit.hitbox().damageMultiplier();
    }

    default EnumActionResult onHitboxInteract(AzHitboxHit hit, EntityPlayer player, EnumHand hand) {
        return EnumActionResult.PASS;
    }
}
