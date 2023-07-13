package mod.azure.azurelib.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem.Data;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem.Listener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem.User;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLEnvironment;

public interface AzureTicker {
	public static void tick(Level level, Data data, User user) {
		if (!(level instanceof ServerLevel)) {
			return;
		}
		ServerLevel serverLevel = (ServerLevel) level;
		if (data.getCurrentVibration() == null) {
			AzureTicker.trySelectAndScheduleVibration(serverLevel, data, user);
		}
		if (data.getCurrentVibration() == null) {
			return;
		}
		boolean bl = data.getTravelTimeInTicks() > 0;
		data.decrementTravelTime();
		if (data.getTravelTimeInTicks() <= 0) {
			bl = AzureTicker.receiveVibration(serverLevel, data, user, data.getCurrentVibration());
		}
		if (bl) {
			user.onDataChanged();
		}
	}

	private static void trySelectAndScheduleVibration(ServerLevel serverLevel, Data data, User user) {
		data.getSelectionStrategy().chosenCandidate(serverLevel.getGameTime()).ifPresent(vibrationInfo -> {
			data.setCurrentVibration((VibrationInfo) vibrationInfo);
			Vec3 vec3 = vibrationInfo.pos();
			data.setTravelTimeInTicks(user.calculateTravelTimeInTicks(vibrationInfo.distance()));
			if (!FMLEnvironment.production)
				serverLevel.sendParticles(new VibrationParticleOption(user.getPositionSource(), data.getTravelTimeInTicks()), vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
			user.onDataChanged();
			data.getSelectionStrategy().startOver();
		});
	}

	private static boolean receiveVibration(ServerLevel serverLevel, Data data, User user, VibrationInfo vibrationInfo) {
		BlockPos blockPos = BlockPos.containing(vibrationInfo.pos());
		BlockPos blockPos2 = user.getPositionSource().getPosition(serverLevel).map(BlockPos::containing).orElse(blockPos);
		if (user.requiresAdjacentChunksToBeTicking() && !AzureTicker.areAdjacentChunksTicking(serverLevel, blockPos2)) {
			return false;
		}
		user.onReceiveVibration(serverLevel, blockPos, vibrationInfo.gameEvent(), vibrationInfo.getEntity(serverLevel).orElse(null), vibrationInfo.getProjectileOwner(serverLevel).orElse(null), Listener.distanceBetweenInBlocks(blockPos, blockPos2));
		data.setCurrentVibration(null);
		return true;
	}

	private static boolean areAdjacentChunksTicking(Level level, BlockPos blockPos) {
		ChunkPos chunkPos = new ChunkPos(blockPos);
		for (int i = chunkPos.x - 1; i < chunkPos.x + 1; ++i) {
			for (int j = chunkPos.z - 1; j < chunkPos.z + 1; ++j) {
				LevelChunk chunkAccess = level.getChunkSource().getChunkNow(i, j);
				if (chunkAccess != null && level.shouldTickBlocksAt(chunkAccess.getPos().toLong()))
					continue;
				return false;
			}
		}
		return true;
	}
}