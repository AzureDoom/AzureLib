
package mod.azure.azurelib.common.compat.bettercombat;

import net.bettercombat.api.AttackHand;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class AzBetterCombatAnimations {

    private static final Map<Item, Function<AttackHand, String>> HANDLERS = new ConcurrentHashMap<>();

    private AzBetterCombatAnimations() {}

    public static void register(
        Item item,
        Function<AttackHand, String> animationSelector
    ) {
        HANDLERS.put(item, animationSelector);
    }

    public static void register(
        Item item,
        String animation
    ) {
        register(item, attack -> animation);
    }

    @Nullable
    public static String resolve(AttackHand attack) {
        if (attack == null || attack.itemStack() == null) {
            return null;
        }

        Item item = attack.itemStack().getItem();

        Function<AttackHand, String> handler = HANDLERS.get(item);

        return handler == null ? null : handler.apply(attack);
    }
}
