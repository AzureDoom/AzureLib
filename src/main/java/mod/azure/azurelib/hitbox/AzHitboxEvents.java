package mod.azure.azurelib.hitbox;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

public final class AzHitboxEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof AzHitboxHolder) || event.entityLiving.worldObj.isRemote) {
            return;
        }

        event.ammount = AzHitboxDamageTracker.applyHurt(event.entityLiving, event.source, event.ammount);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityInteract(EntityInteractEvent event) {
        if (AzHitboxInteractions.interact(event.entityPlayer, event.target)) {
            event.setCanceled(true);
        }
    }
}
