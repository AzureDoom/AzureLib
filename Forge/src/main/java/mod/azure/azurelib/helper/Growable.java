package mod.azure.azurelib.helper;

import static java.lang.Math.min;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public interface Growable {
	float getGrowth();

	void setGrowth(float growth);

	float getMaxGrowth();

	default void grow(LivingEntity entity, float amount) {
		setGrowth(min(getGrowth() + amount, getMaxGrowth()));
		if (getGrowth() >= getMaxGrowth())
			growUp(entity);
	}

	LivingEntity growInto();

	default void growUp(LivingEntity entity) {
		World world = entity.level;
		if (!world.isClientSide()) {
			LivingEntity newEntity = growInto();
			if (newEntity == null)
				return;
			newEntity.moveTo(entity.blockPosition(), entity.yRot, entity.xRot);
			world.addFreshEntity(newEntity);
			entity.remove();
		}
	}

	default float getGrowthNeededUntilGrowUp() {
		return getMaxGrowth() - getGrowth();
	}

	default float getGrowthMultiplier() {
		return 1.0f;
	}
}
