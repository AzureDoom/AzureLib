package mod.azure.azurelib.fabric.core2.example.entities.ovamorph;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

public class HatchManager {

    private static final String HATCH_DURATION_IN_TICKS_KEY = "hatchDurationInTicks";

    private static final String HATCHED_KEY = "hatched";

    private static final String MAXIMUM_SPAWN_COUNT_KEY = "maximumSpawnCount";

    private static final String REMAINING_SPAWN_DELAY_IN_TICKS_KEY = "remainingSpawnDelayInTicks";

    private static final String SPAWN_COUNT_KEY = "spawnCount";

    private final LivingEntity entity;

    private final EntityDataAccessor<Boolean> hatchedEDA;

    private final EntityDataAccessor<Byte> maximumSpawnCountEDA;

    private final int spawnDelayInTicks;

    private int hatchDurationInTicks;

    private int remainingSpawnDelayInTicks;

    private int spawnCount;

    public HatchManager(LivingEntity entity, EntityDataAccessor<Boolean> hatchedEDA, EntityDataAccessor<Byte> maximumSpawnCountEDA, int hatchDurationInTicks, int spawnDelayInTicks) {
        this.entity = entity;
        this.hatchedEDA = hatchedEDA;
        this.maximumSpawnCountEDA = maximumSpawnCountEDA;
        this.hatchDurationInTicks = hatchDurationInTicks;
        this.spawnDelayInTicks = spawnDelayInTicks;
        this.remainingSpawnDelayInTicks = spawnDelayInTicks;
        this.spawnCount = 0;
    }

    public void tick() {
        if (hatched()) {
            hatchDurationInTicks = Math.max(hatchDurationInTicks - 1, 0);
        }

        var level = entity.level();

        if (level.isClientSide) {
            return;
        }

        var hasFullyHatched = hatchDurationInTicks <= 0;
        var canSpawn = spawnCount < maximumSpawnCount();

        if (!hasFullyHatched || !canSpawn) {
            return;
        }

        remainingSpawnDelayInTicks = Math.max(remainingSpawnDelayInTicks - 1, 0);

        if (remainingSpawnDelayInTicks == 0) {
//            spawnFacehugger(level);
            // Reset spawn delay.
            remainingSpawnDelayInTicks = spawnDelayInTicks;
            // Increment the spawns created.
            spawnCount++;
        }
    }

    public boolean hatched() {
        return entity.getEntityData().get(hatchedEDA);
    }

    public void hatch() {
        entity.getEntityData().set(hatchedEDA, true);
    }

    public byte maximumSpawnCount() {
        return entity.getEntityData().get(maximumSpawnCountEDA);
    }

    public void load(CompoundTag compoundTag) {
        this.hatchDurationInTicks = compoundTag.getInt(HATCH_DURATION_IN_TICKS_KEY);
        this.remainingSpawnDelayInTicks = compoundTag.getInt(REMAINING_SPAWN_DELAY_IN_TICKS_KEY);
        this.spawnCount = compoundTag.getInt(SPAWN_COUNT_KEY);

        entity.getEntityData().set(hatchedEDA, compoundTag.getBoolean(HATCHED_KEY));
        entity.getEntityData().set(maximumSpawnCountEDA, compoundTag.getByte(MAXIMUM_SPAWN_COUNT_KEY));
    }

    public void save(CompoundTag compoundTag) {
        compoundTag.putInt(HATCH_DURATION_IN_TICKS_KEY, hatchDurationInTicks);
        compoundTag.putInt(REMAINING_SPAWN_DELAY_IN_TICKS_KEY, remainingSpawnDelayInTicks);
        compoundTag.putInt(SPAWN_COUNT_KEY, spawnCount);

        compoundTag.putBoolean(HATCHED_KEY, entity.getEntityData().get(hatchedEDA));
        compoundTag.putByte(MAXIMUM_SPAWN_COUNT_KEY, entity.getEntityData().get(maximumSpawnCountEDA));
    }

//    private void spawnFacehugger(Level level) {
//        var facehugger = AVPREntityTypes.FACEHUGGER.create(level);
//
//        if (facehugger == null) {
//            // TODO: Log.
//            return;
//        }
//
//        // TODO: We need to transfer the ovamorph genes to the spawned facehugger(s).
//
//        facehugger.moveTo(entity.blockPosition(), entity.getYRot(), entity.getXRot());
//
//        // Explicitly set the yaw and pitch to ensure accurate orientation
//        facehugger.setYRot(entity.getYRot());
//        facehugger.setXRot(entity.getXRot());
//
//        // Synchronize the visual body rotation (if applicable for mobs)
//        facehugger.yBodyRot = entity.yBodyRot; // Body rotation
//        facehugger.yHeadRot = entity.yHeadRot; // Head rotation
//
//        level.addFreshEntity(facehugger);
//    }
}
