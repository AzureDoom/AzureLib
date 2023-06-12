package mod.azure.azurelib.helper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class AzureVibrationUser implements VibrationSystem.User {
	private final Mob mob;
	@SuppressWarnings("unused")
	private final float moveSpeed;
	private final int range;
	private final PositionSource positionSource;

	public AzureVibrationUser(Mob entity, float speed, int range) {
		this.positionSource = new EntityPositionSource(entity, entity.getEyeHeight());
		this.mob = entity;
		this.moveSpeed = speed;
		this.range = range;
	}

	@Override
	public int getListenerRadius() {
		return range;
	}

	@Override
	public PositionSource getPositionSource() {
		return this.positionSource;
	}

	@Override
	public TagKey<GameEvent> getListenableEvents() {
		return GameEventTags.WARDEN_CAN_LISTEN;
	}

	@Override
	public boolean canTriggerAvoidVibration() {
		return true;
	}

	@Override
	public boolean isValidVibration(GameEvent gameEvent, Context context) {
		if (!gameEvent.is(this.getListenableEvents()))
			return false;

		var entity = context.sourceEntity();
		if (entity != null) {
			if (entity.isSpectator())
				return false;
			if (entity.isSteppingCarefully() && gameEvent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING))
				return false;
			if (entity.dampensVibrations())
				return false;
		}
		if (context.affectedState() != null)
			return !context.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
		return true;
	}

	@Override
	public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
		if (mob.isNoAi() || mob.isDeadOrDying() || !mob.level().getWorldBorder().isWithinBounds(blockPos) || mob.isRemoved())
			return false;
		var entity = context.sourceEntity();
		return !(entity instanceof LivingEntity) || canTargetEntity((LivingEntity) entity);
	}

	@Override
	public void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity2, float f) {
		if (this.mob.isDeadOrDying())
			return;
		if (this.mob.isVehicle())
			return;
		if (this.mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).isPresent())
			return;
	}

	@Contract(value = "null->false")
	public boolean canTargetEntity(@Nullable Entity entity) {
		if (!(entity instanceof LivingEntity))
			return false;
		var livingEntity = (LivingEntity) entity;
		if (this.mob.level() != entity.level())
			return false;
		if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
			return false;
		if (this.mob.isVehicle())
			return false;
		if (this.mob.isAlliedTo(entity))
			return false;
		if (livingEntity.getMobType() == MobType.UNDEAD)
			return false;
		if (livingEntity.getType() == EntityType.ARMOR_STAND)
			return false;
		if (livingEntity.getType() == EntityType.WARDEN)
			return false;
		if (livingEntity instanceof Bat)
			return false;
		if (entity instanceof Marker)
			return false;
		if (entity instanceof AreaEffectCloud)
			return false;
		if (livingEntity.isInvulnerable())
			return false;
		if (livingEntity.isDeadOrDying())
			return false;
		if (!this.mob.level().getWorldBorder().isWithinBounds(livingEntity.getBoundingBox()))
			return false;
		return true;
	}
}