package mod.azure.azurelib.fabric.core2.example.entities.ovamorph;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class Ovamorph extends Monster {

    private static final EntityDataAccessor<Boolean> HATCHED = SynchedEntityData.defineId(Ovamorph.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Byte> MAX_SPAWN_COUNT = SynchedEntityData.defineId(Ovamorph.class, EntityDataSerializers.BYTE);

    public static AttributeSupplier.Builder createOvamorphAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.ATTACK_DAMAGE, 0)
            .add(Attributes.MOVEMENT_SPEED, 0);
    }

    private final OvamorphAnimationDispatcher animationDispatcher;

    private final HatchManager hatchManager;

    public Ovamorph(EntityType<? extends Ovamorph> entityType, Level level) {
        super(entityType, level);
        this.animationDispatcher = new OvamorphAnimationDispatcher(this);
        this.hatchManager = new HatchManager(this, HATCHED, MAX_SPAWN_COUNT, 3* 20, 3 * 20);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HATCHED, false);
        builder.define(MAX_SPAWN_COUNT, (byte) (random.nextInt(3) + 1));
    }

    @Override
    public void tick() {
        super.tick();
        hatchManager.tick();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (!level().isClientSide) {
            hatchManager.hatch();
            animationDispatcher.open();
        }

        return super.hurt(damageSource, damage);
    }

    // Prevent the ovamorph from drowning or otherwise running out of air.
    @Override
    public int getAirSupply() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        hatchManager.load(compoundTag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        hatchManager.save(compoundTag);
    }

    public HatchManager hatchManager() {
        return hatchManager;
    }
}
