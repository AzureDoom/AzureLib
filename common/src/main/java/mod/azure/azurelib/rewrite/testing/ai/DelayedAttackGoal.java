package mod.azure.azurelib.rewrite.testing.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

import mod.azure.azurelib.rewrite.testing.MarauderEntity;

public class DelayedAttackGoal extends MeleeAttackGoal {

    private final int delayTicksBeforeAttack;

    private final Runnable attackAnimationRunnable;

    private int delayBeforeAttack;

    private boolean triggeredAttackAnimation;

    public DelayedAttackGoal(
        PathfinderMob mob,
        double speedModifier,
        boolean bl,
        int delayTicksBeforeAttack,
        Runnable attackAnimationRunnable
    ) {
        super(mob, speedModifier, bl);
        this.delayTicksBeforeAttack = delayTicksBeforeAttack;
        this.attackAnimationRunnable = attackAnimationRunnable;

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob instanceof MarauderEntity marauderEntity) {
            return marauderEntity.getSpawnTicks() >= marauderEntity.MAX_SPAWN_ANIMATION_TICKS && super.canUse();
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (mob instanceof MarauderEntity marauderEntity) {
            return marauderEntity.getSpawnTicks() >= marauderEntity.MAX_SPAWN_ANIMATION_TICKS
                && super.canContinueToUse();
        }
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.delayBeforeAttack = 0;
        this.triggeredAttackAnimation = false;
    }

    @Override
    protected void checkAndPerformAttack(@NotNull LivingEntity target) {
        if (!this.mob.level().isClientSide()) {
            if (canPerformAttack(target)) {
                if (delayBeforeAttack > 0) {
                    delayBeforeAttack--;
                    this.mob.getNavigation().stop();
                    if (delayBeforeAttack == delayTicksBeforeAttack && !triggeredAttackAnimation) {
                        attackAnimationRunnable.run();
                        this.triggeredAttackAnimation = true;
                    }
                } else {
                    resetAttackCooldown();
                    mob.swing(InteractionHand.MAIN_HAND);
                    mob.doHurtTarget(target);

                    this.triggeredAttackAnimation = false;
                }
            } else {
                this.delayBeforeAttack = adjustedTickDelay(delayTicksBeforeAttack);
                this.triggeredAttackAnimation = false;
            }
        }
    }
}
