package mod.azure.azurelib.rewrite.testing;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.api.common.ai.pathing.AzureNavigation;
import mod.azure.azurelib.rewrite.testing.ai.DelayedAttackGoal;
import mod.azure.azurelib.rewrite.util.MoveAnalysis;

public class MarauderEntity extends Monster {

    protected static final EntityDataAccessor<Float> SPAWN_TICKS = SynchedEntityData.defineId(
        MarauderEntity.class,
        EntityDataSerializers.FLOAT
    );

    public int MAX_SPAWN_ANIMATION_TICKS = 290;

    /**
     * Handles the animation state transitions for the {@link MarauderEntity}. This dispatcher is responsible for
     * deciding and applying the appropriate animations to the entity based on its current state and actions, such as
     * walking, running, idling, spawning, attacking, or dying. This instance operates primarily on the client side to
     * handle visual representation of the {@link MarauderEntity} and is updated within the entity's tick lifecycle.
     */
    public final MarauderAnimationDispatcher animationDispatcher;

    private final MoveAnalysis moveAnalysis;

    public MarauderEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.animationDispatcher = new MarauderAnimationDispatcher(this);
        this.moveAnalysis = new MoveAnalysis(this);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new AzureNavigation(this, level);
    }

    @Override
    public float maxUpStep() {
        return 2.0F;
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 80 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void tick() {
        super.tick();
        moveAnalysis.update();

        if (!this.level().isClientSide && this.getSpawnTicks() < MAX_SPAWN_ANIMATION_TICKS && this.isAlive()) {
            this.setSpawnTicks(this.getSpawnTicks() + 1.0F);
            this.navigation.stop();
            this.setYBodyRot(0);
            this.setYHeadRot(0);
            this.getEyePosition(90);
            this.setXRot(0);
            this.setYRot(0);
            this.setTarget(null);
        }
    }

    public void updateAnimations() {
        var isMovingOnGround = moveAnalysis.isMovingHorizontally() && onGround();

        if (this.isDeadOrDying()) {
            animationDispatcher.clientDeath();
            return;
        }

        if (this.getSpawnTicks() < MAX_SPAWN_ANIMATION_TICKS) {
            animationDispatcher.clientSpawn();
            return;
        }

        if (isMovingOnGround) {
            if (this.isAggressive() && !this.swinging) {
                animationDispatcher.clientRun();
            } else {
                animationDispatcher.clientWalk();
            }
            return;
        }

        if (!this.isAggressive()) {
            animationDispatcher.clientIdle();
        }
    }

    public void runAttackAnimations() {
        animationDispatcher.serverMelee();
    }

    protected void setSpawnTicks(float spawnTicks) {
        this.entityData.set(SPAWN_TICKS, spawnTicks);
    }

    public float getSpawnTicks() {
        return this.entityData.get(SPAWN_TICKS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPAWN_TICKS, 0.0F);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SpawnTicks", this.getSpawnTicks());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSpawnTicks(compound.getFloat("SpawnTicks"));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.3F));
        this.goalSelector.addGoal(2, new DelayedAttackGoal(this, 0.6F, true, 5, this::runAttackAnimations));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) { /* DISABLES VANILLA WALK SOUND */}
}
