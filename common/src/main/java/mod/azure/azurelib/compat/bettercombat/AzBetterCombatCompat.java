
package mod.azure.azurelib.compat.bettercombat;

import net.bettercombat.api.AttackHand;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;

public final class AzBetterCombatCompat {

    private static final String CONTROLLER = "better_combat";

    private static boolean initialized;

    private AzBetterCombatCompat() {}

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;

        BetterCombatClientEvents.ATTACK_START.register(
            AzBetterCombatCompat::onAttackStart
        );
    }

    private static void onAttackStart(
        AbstractClientPlayer player,
        AttackHand attack
    ) {
        if (!(player instanceof LocalPlayer)) {
            return;
        }

        if (attack == null) {
            return;
        }

        ItemStack stack = attack.itemStack();

        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (AzItemRendererRegistry.getOrNull(stack.getItem()) == null) {
            return;
        }

        if (stack.get(AzureLib.AZ_ID.get()) == null) {
            return;
        }

        String animation = AzBetterCombatAnimations.resolve(attack);

        if (animation == null || animation.isEmpty()) {
            return;
        }

        AzCommand.create(CONTROLLER, animation)
            .sendForItem(player, stack);
    }
}
