package mod.azure.azurelib.testing.block.be;

import mod.azure.azurelib.NeoForgeAzureLibMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StargateBlockEntity extends BlockEntity {

    public final StargateBlockAnimationDispatcher animationDispatcher;

    public StargateBlockEntity(BlockPos pos, BlockState blockState) {
        super(NeoForgeAzureLibMod.AzureEntities.STARGATE_BLOCK_ENTITY.get(), pos, blockState);
        this.animationDispatcher = new StargateBlockAnimationDispatcher(this);
    }

    /**
     * Handles the tick behavior for the StargateBlockEntity, triggering server-side spinning animations if the block
     * entity and level instance are valid and the method is executed on the client side.
     *
     * @param level       The current level or world instance.
     * @param pos         The position of the block in the world.
     * @param state       The current block state of the associated block.
     * @param blockEntity The StargateBlockEntity instance to operate on.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, StargateBlockEntity blockEntity) {
        if (blockEntity.level != null && level.isClientSide()) {
            blockEntity.animationDispatcher.serverSpin();
        }
    }
}
