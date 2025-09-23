package mod.azure.azurelib.rewrite.testing.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PistolItem extends Item {

    private final PistolAnimationDispatcher dispatcher;

    public PistolItem() {
        super(new Properties());
        this.dispatcher = new PistolAnimationDispatcher();
    }

    @Override
    public void onUseTick(
        @NotNull Level level,
        @NotNull LivingEntity livingEntity,
        @NotNull ItemStack stack,
        int remainingUseDuration
    ) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (livingEntity instanceof Player player && !level.isClientSide()) {
            dispatcher.serverFire(player, stack);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
        @NotNull Level world,
        Player user,
        @NotNull InteractionHand hand
    ) {
        final var itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }
}
