package mod.azure.azurelib.testing.block;

import mod.azure.azurelib.FabricAzureLibMod;
import mod.azure.azurelib.testing.block.be.StargateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StargateBlock extends BaseEntityBlock {

    public StargateBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.DRIPSTONE_BLOCK).strength(5.0f, 8.0f).noOcclusion());
    }

    /**
     * Creates a new {@link BlockEntity} instance for the Stargate block at the specified position and state.
     *
     * @param pos   The position of the block in the world.
     * @param state The current block state for this block entity.
     * @return A new {@link BlockEntity} instance associated with the Stargate block, or {@code null} if none is
     *         available.
     */
    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return FabricAzureLibMod.STARGATE_BLOCK_ENTITY.create(pos, state);
    }

    /**
     * Determines the appropriate ticker for a block entity to handle its periodic updates.
     *
     * @param level The current level or world instance.
     * @param state The block state of the associated block.
     * @param type  The type of the block entity to obtain the ticker for.
     * @param <T>   A subtype of BlockEntity.
     * @return A BlockEntityTicker for the specified block entity type, or null if no ticker is applicable.
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        @NotNull Level level,
        @NotNull BlockState state,
        @NotNull BlockEntityType<T> type
    ) {
        return createTickerHelper(type, FabricAzureLibMod.STARGATE_BLOCK_ENTITY, StargateBlockEntity::tick);
    }
}
