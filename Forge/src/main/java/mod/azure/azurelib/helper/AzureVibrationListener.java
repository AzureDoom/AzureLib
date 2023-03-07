package mod.azure.azurelib.helper;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AzureVibrationListener implements GameEventListener {
	@VisibleForTesting
	public static final Object2IntMap<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Object2IntMaps
			.unmodifiable(Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
				object2IntOpenHashMap.put(GameEvent.STEP, 1);
				object2IntOpenHashMap.put(GameEvent.FLAP, 2);
				object2IntOpenHashMap.put(GameEvent.SWIM, 3);
				object2IntOpenHashMap.put(GameEvent.ELYTRA_GLIDE, 4);
				object2IntOpenHashMap.put(GameEvent.HIT_GROUND, 5);
				object2IntOpenHashMap.put(GameEvent.TELEPORT, 5);
				object2IntOpenHashMap.put(GameEvent.SPLASH, 6);
				object2IntOpenHashMap.put(GameEvent.ENTITY_SHAKE, 6);
				object2IntOpenHashMap.put(GameEvent.BLOCK_CHANGE, 6);
				object2IntOpenHashMap.put(GameEvent.NOTE_BLOCK_PLAY, 6);
				object2IntOpenHashMap.put(GameEvent.PROJECTILE_SHOOT, 7);
				object2IntOpenHashMap.put(GameEvent.DRINK, 7);
				object2IntOpenHashMap.put(GameEvent.PRIME_FUSE, 7);
				object2IntOpenHashMap.put(GameEvent.PROJECTILE_LAND, 8);
				object2IntOpenHashMap.put(GameEvent.EAT, 8);
				object2IntOpenHashMap.put(GameEvent.ENTITY_INTERACT, 8);
				object2IntOpenHashMap.put(GameEvent.ENTITY_DAMAGE, 8);
				object2IntOpenHashMap.put(GameEvent.EQUIP, 9);
				object2IntOpenHashMap.put(GameEvent.SHEAR, 9);
				object2IntOpenHashMap.put(GameEvent.ENTITY_ROAR, 9);
				object2IntOpenHashMap.put(GameEvent.BLOCK_CLOSE, 10);
				object2IntOpenHashMap.put(GameEvent.BLOCK_DEACTIVATE, 10);
				object2IntOpenHashMap.put(GameEvent.BLOCK_DETACH, 10);
				object2IntOpenHashMap.put(GameEvent.DISPENSE_FAIL, 10);
				object2IntOpenHashMap.put(GameEvent.BLOCK_OPEN, 11);
				object2IntOpenHashMap.put(GameEvent.BLOCK_ACTIVATE, 11);
				object2IntOpenHashMap.put(GameEvent.BLOCK_ATTACH, 11);
				object2IntOpenHashMap.put(GameEvent.ENTITY_PLACE, 12);
				object2IntOpenHashMap.put(GameEvent.BLOCK_PLACE, 12);
				object2IntOpenHashMap.put(GameEvent.FLUID_PLACE, 12);
				object2IntOpenHashMap.put(GameEvent.ENTITY_DIE, 13);
				object2IntOpenHashMap.put(GameEvent.BLOCK_DESTROY, 13);
				object2IntOpenHashMap.put(GameEvent.FLUID_PICKUP, 13);
				object2IntOpenHashMap.put(GameEvent.ITEM_INTERACT_FINISH, 14);
				object2IntOpenHashMap.put(GameEvent.CONTAINER_CLOSE, 14);
				object2IntOpenHashMap.put(GameEvent.PISTON_CONTRACT, 14);
				object2IntOpenHashMap.put(GameEvent.PISTON_EXTEND, 15);
				object2IntOpenHashMap.put(GameEvent.CONTAINER_OPEN, 15);
				object2IntOpenHashMap.put(GameEvent.EXPLODE, 15);
				object2IntOpenHashMap.put(GameEvent.LIGHTNING_STRIKE, 15);
				object2IntOpenHashMap.put(GameEvent.INSTRUMENT_PLAY, 15);
			}));
	protected final PositionSource listenerSource;
	protected final int listenerRange;
	protected final AzureVibrationListenerConfig config;
	@Nullable
	protected AzureVibrationInfo currentVibration;
	protected int travelTimeInTicks;
	private final AzureVibrationSelector selectionStrategy;

	public static Codec<AzureVibrationListener> codec(AzureVibrationListenerConfig vibrationListenerConfig) {
		return RecordCodecBuilder.create(instance -> instance
				.group((PositionSource.CODEC.fieldOf("source"))
						.forGetter(vibrationListener -> ((AzureVibrationListener) vibrationListener).listenerSource),
						(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range")).forGetter(
								vibrationListener -> ((AzureVibrationListener) vibrationListener).listenerRange),
						AzureVibrationInfo.CODEC.optionalFieldOf("event")
								.forGetter(vibrationListener -> Optional
										.ofNullable(((AzureVibrationListener) vibrationListener).currentVibration)),
						(AzureVibrationSelector.CODEC.fieldOf("selector")).forGetter(
								vibrationListener -> ((AzureVibrationListener) vibrationListener).selectionStrategy),
						(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay")).orElse(0).forGetter(
								vibrationListener -> ((AzureVibrationListener) vibrationListener).travelTimeInTicks))
				.apply(instance,
						(positionSource, integer, optional, vibrationSelector, integer2) -> new AzureVibrationListener(
								(PositionSource) positionSource, (int) integer, vibrationListenerConfig,
								optional.orElse(null), (AzureVibrationSelector) vibrationSelector, (int) integer2)));
	}

	public AzureVibrationListener(PositionSource positionSource, int i,
			AzureVibrationListenerConfig vibrationListenerConfig, @Nullable AzureVibrationInfo vibrationInfo,
			AzureVibrationSelector vibrationSelector, int j) {
		this.listenerSource = positionSource;
		this.listenerRange = i;
		this.config = vibrationListenerConfig;
		this.currentVibration = vibrationInfo;
		this.travelTimeInTicks = j;
		this.selectionStrategy = vibrationSelector;
	}

	public AzureVibrationListener(PositionSource positionSource, int i,
			AzureVibrationListenerConfig vibrationListenerConfig) {
		this(positionSource, i, vibrationListenerConfig, null, new AzureVibrationSelector(), 0);
	}

	public static int getGameEventFrequency(GameEvent gameEvent) {
		return VIBRATION_FREQUENCY_FOR_EVENT.getOrDefault((Object) gameEvent, 0);
	}

	public void tick(Level level) {
		if (level instanceof ServerLevel) {
			var serverLevel = (ServerLevel) level;
			if (this.currentVibration == null)
				this.selectionStrategy.chosenCandidate(serverLevel.getGameTime()).ifPresent(vibrationInfo -> {
					this.currentVibration = vibrationInfo;
					this.travelTimeInTicks = Mth.floor(this.currentVibration.distance());
					this.config.onSignalSchedule();
					this.selectionStrategy.startOver();
				});
			if (this.currentVibration != null) {
				--this.travelTimeInTicks;
				if (this.travelTimeInTicks <= 0) {
					this.travelTimeInTicks = 0;
					this.config.onSignalReceive(serverLevel, this, new BlockPos(this.currentVibration.pos()),
							this.currentVibration.gameEvent(),
							this.currentVibration.getEntity(serverLevel).orElse(null),
							this.currentVibration.getProjectileOwner(serverLevel).orElse(null),
							this.currentVibration.distance());
					this.currentVibration = null;
				}
			}
		}
	}

	@Override
	public PositionSource getListenerSource() {
		return this.listenerSource;
	}

	@Override
	public int getListenerRadius() {
		return this.listenerRange;
	}

	@Override
	public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
		if (this.currentVibration != null)
			return false;
		if (!this.config.isValidVibration(gameEvent, context))
			return false;
		var optional = this.listenerSource.getPosition(serverLevel);
		if (optional.isEmpty())
			return false;
		var vec32 = optional.get();
		if (!this.config.shouldListen(serverLevel, this, new BlockPos(vec3), gameEvent, context))
			return false;
		if (AzureVibrationListener.isOccluded(serverLevel, vec3, vec32))
			return false;
		this.scheduleVibration(serverLevel, gameEvent, context, vec3, vec32);
		return true;
	}

	public void forceGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
		this.listenerSource.getPosition(serverLevel)
				.ifPresent(vec32 -> this.scheduleVibration(serverLevel, gameEvent, context, vec3, (Vec3) vec32));
	}

	public void scheduleVibration(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3,
			Vec3 vec32) {
		this.selectionStrategy.addCandidate(
				new AzureVibrationInfo(gameEvent, (float) vec3.distanceTo(vec32), vec3, context.sourceEntity()),
				serverLevel.getGameTime());
	}

	private static boolean isOccluded(Level level, Vec3 vec3, Vec3 vec32) {
		var vec33 = new Vec3((double) Mth.floor(vec3.x) + 0.5, (double) Mth.floor(vec3.y) + 0.5,
				(double) Mth.floor(vec3.z) + 0.5);
		var vec34 = new Vec3((double) Mth.floor(vec32.x) + 0.5, (double) Mth.floor(vec32.y) + 0.5,
				(double) Mth.floor(vec32.z) + 0.5);
		for (Direction direction : Direction.values()) {
			var vec35 = vec33.relative(direction, 1.0E-5f);
			if (level
					.isBlockInLine(new ClipBlockStateContext(vec35, vec34,
							blockState -> blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)))
					.getType() == HitResult.Type.BLOCK)
				continue;
			return false;
		}
		return true;
	}

	public static interface AzureVibrationListenerConfig {
		default public TagKey<GameEvent> getListenableEvents() {
			return GameEventTags.WARDEN_CAN_LISTEN;
		}

		default public boolean canTriggerAvoidVibration() {
			return true;
		}

		default public boolean isValidVibration(GameEvent event, GameEvent.Context context) {
			if (!event.is(this.getListenableEvents()))
				return false;
			var entity = context.sourceEntity();
			if (entity != null) {
				if (entity.isSpectator())
					return false;
				if (entity.isSteppingCarefully() && event.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING))
					return false;
			}
			if (context.affectedState() != null)
				return !context.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
			return true;
		}

		public boolean shouldListen(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4,
				GameEvent.Context var5);

		public void onSignalReceive(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4,
				@Nullable Entity var5, @Nullable Entity var6, float var7);

		default public void onSignalSchedule() {
		}
	}
}