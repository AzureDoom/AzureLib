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

    public Player player() {
        return player;
    }

    public LivingEntity shape() {
        return shape;
    }

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

    public boolean isMovingHorizontally() {
        return deltaX * deltaX + deltaZ * deltaZ > MOVING_EPSILON_SQUARED;
    }

    public boolean isSprinting() {
        return player.isSprinting();
    }

    public boolean isOnGround() {
        return player.isOnGround();
    }

    public boolean startedSwing() {
        return swinging && !wasSwinging;
    }

    @SuppressWarnings("unchecked")
    public <S> S state(Object key, Supplier<S> factory) {
        if (state == null) {
            state = new IdentityHashMap<>(2);
        }

        return (S) state.computeIfAbsent(key, ignored -> factory.get());
    }
}
