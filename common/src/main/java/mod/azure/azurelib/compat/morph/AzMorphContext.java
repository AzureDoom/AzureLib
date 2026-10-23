package mod.azure.azurelib.compat.morph;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class AzMorphContext {

    private static final double MOVING_EPSILON_SQUARED = 1.0E-4;

    final LivingEntity shape;

    Player player;

    int lastPlayerTick = Integer.MIN_VALUE;

    int morphTicks;

    double deltaX, deltaY, deltaZ;

    boolean swinging, wasSwinging;

    private Map<Object, Object> state;

    AzMorphContext(LivingEntity shape) {
        this.shape = shape;
    }

    /** The player wearing the morph. */
    public Player player() {
        return player;
    }

    /** The client-side copy Walkers draws on the player. Never a real, level-ticked mob. */
    public LivingEntity shape() {
        return shape;
    }

    /** Player ticks this copy has been driven for; 1 on the first tick. */
    public int morphTicks() {
        return morphTicks;
    }

    public double deltaX() {
        return deltaX;
    }

    public double deltaY() {
        return deltaY;
    }

    public double deltaZ() {
        return deltaZ;
    }

    /** True when the player moved more than 0.01 blocks horizontally this tick. */
    public boolean isMovingHorizontally() {
        return deltaX * deltaX + deltaZ * deltaZ > MOVING_EPSILON_SQUARED;
    }

    public boolean isSprinting() {
        return player.isSprinting();
    }

    public boolean isOnGround() {
        return player.onGround();
    }

    /** True only on the tick the player's arm swing began - the natural trigger for an attack clip. */
    public boolean startedSwing() {
        return swinging && !wasSwinging;
    }

    /**
     * Per-copy scratch state for a handler (attack cycling, cooldowns...). Created on first use, dropped with the copy.
     * Use a private static final key object so two mods never collide.
     */
    @SuppressWarnings("unchecked")
    public <S> S state(Object key, Supplier<S> factory) {
        if (state == null) {
            state = new IdentityHashMap<>(2);
        }

        return (S) state.computeIfAbsent(key, ignored -> factory.get());
    }
}
