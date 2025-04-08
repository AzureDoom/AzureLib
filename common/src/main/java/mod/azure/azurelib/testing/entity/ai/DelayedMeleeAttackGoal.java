package mod.azure.azurelib.testing.entity.ai;

import mod.azure.azurelib.testing.entity.MarauderEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class DelayedMeleeAttackGoal extends Goal {

    protected final PathfinderMob mob;

    private final double speedModifier;

    private final boolean followingTargetEvenIfNotSeen;

    public Path path;

    private double pathedTargetX;

    private double pathedTargetY;

    private double pathedTargetZ;

    private int ticksUntilNextPathRecalculation;

    private int ticksUntilNextAttack;

    public long lastCanUseCheck;

    private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;

    private int delayCounter;

    public DelayedMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        long i = this.mob.level().getGameTime();
        if (i - this.lastCanUseCheck < 20L) {
            return false;
        } else {
            this.lastCanUseCheck = i;
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(livingentity, 0);
                return this.path != null ? true : this.mob.isWithinMeleeAttackRange(livingentity);
            }
        }
    }

    public boolean canContinueToUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        } else {
            return !this.mob.isWithinRestriction(livingentity.blockPosition())
                ? false
                : !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player) livingentity)
                    .isCreative();
        }
    }

    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
        this.delayCounter = 0;
    }

    public void stop() {
        LivingEntity livingentity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
            this.mob.setTarget((LivingEntity) null);
        }

        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
        this.delayCounter = 0;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null) {
            if (!this.mob.level().isClientSide()) {
                this.delayCounter++;
            }
            this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if (
                (this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity))
                    && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == (double) 0.0F
                        && this.pathedTargetY == (double) 0.0F && this.pathedTargetZ == (double) 0.0F || livingentity
                            .distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= (double) 1.0F
                        || this.mob.getRandom().nextFloat() < 0.05F)
            ) {
                this.pathedTargetX = livingentity.getX();
                this.pathedTargetY = livingentity.getY();
                this.pathedTargetZ = livingentity.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                double d0 = this.mob.distanceToSqr(livingentity);
                if (d0 > (double) 1024.0F) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (d0 > (double) 256.0F) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                if (!this.mob.getNavigation().moveTo(livingentity, this.speedModifier)) {
                    this.ticksUntilNextPathRecalculation += 15;
                }

                this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
            }

            if (
                !this.mob.level().isClientSide() && this.mob.getTarget() != null && this.mob.isWithinMeleeAttackRange(
                    this.mob.getTarget()
                )
            ) {
                ((MarauderEntity) this.mob).animationDispatcher.serverMelee();
            }
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformAttack(livingentity);
        }
    }

    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
        }
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(80);
        this.delayCounter = 0;
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected boolean canPerformAttack(LivingEntity entity) {
        return this.delayCounter > 60 && this.isTimeToAttack() && this.mob.isWithinMeleeAttackRange(entity) && this.mob
            .getSensing()
            .hasLineOfSight(entity);
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected int getAttackInterval() {
        return this.adjustedTickDelay(80);
    }
}
