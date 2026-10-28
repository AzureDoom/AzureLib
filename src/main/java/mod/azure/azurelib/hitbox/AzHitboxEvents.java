package mod.azure.azurelib.hitbox;

import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class AzHitboxEvents {

    private AzHitboxEvents() {
        throw new UnsupportedOperationException();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof AzHitboxHolder) || event.getEntityLiving().world.isRemote) {
            return;
        }

        event.setAmount(AzHitboxDamageTracker.applyHurt(event.getEntityLiving(), event.getSource(), event.getAmount()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onLivingHurtFinal(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof AzHitboxHolder) || event.getEntityLiving().world.isRemote) {
            return;
        }

        AzHitboxDamageTracker.confirmHurt(
            event.getEntityLiving(),
            event.getSource(),
            event.isCanceled() ? 0 : event.getAmount()
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        EnumActionResult result = AzHitboxInteractions.interact(
            event.getEntityPlayer(),
            event.getTarget(),
            event.getHand()
        );

        if (result != EnumActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
