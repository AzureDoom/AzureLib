package mod.azure.azurelib.entities;

import java.util.function.ToIntFunction;

import mod.azure.azurelib.AzureLibMod.AzureEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class TimerSpellTurrentBlock extends DirectionalBlock implements EntityBlock {

	public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.AGE_15;

	public TimerSpellTurrentBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.CANDLE).lightLevel(litBlockEmission(15)).noOcclusion());
	}

	private static ToIntFunction<BlockState> litBlockEmission(int p_50760_) {
		return (p_50763_) -> {
			return BlockStateProperties.MAX_LEVEL_15;
		};
	}

	public static IntegerProperty getLightLevel() {
		return LIGHT_LEVEL;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(LIGHT_LEVEL).add(FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return AzureEntities.TIMER_SPELL_TURRET_ENTITY.get().create(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}

}
