package mod.azure.azurelib.rewrite.testing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MutantZombieEntity extends Monster {

    public MutantZombieEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) { /* DISABLES VANILLA WALK SOUND */}

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (this.level().isClientSide() || stack.isEmpty()) {
            return super.mobInteract(player, hand);
        }

        setItemSlot(getEquipmentSlotForItem(stack), stack.copy());

        return InteractionResult.SUCCESS;
    }
}
