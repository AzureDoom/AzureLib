package mod.azure.azurelib.entities;

import java.util.function.ToIntFunction;

import mod.azure.azurelib.AzureLibMod;
import mod.azure.azurelib.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TickingLightBlock extends BaseEntityBlock {

	public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.AGE_15;

	public TickingLightBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.CANDLE).lightLevel(litBlockEmission(15)).pushReaction(PushReaction.DESTROY).noOcclusion());
	}

	private static ToIntFunction<BlockState> litBlockEmission(int p_50760_) {
		return p_50763_ -> BlockStateProperties.MAX_LEVEL_15;
	}

	public static IntegerProperty getLightLevel() {
		return LIGHT_LEVEL;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(LIGHT_LEVEL);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TickingLightEntity(pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
		return Shapes.empty();
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
		return true;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, Services.PLATFORM.getTickingLightEntity(), TickingLightEntity::tick);
	}

}
