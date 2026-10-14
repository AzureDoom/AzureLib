package mod.azure.azurelib.util;

import net.minecraft.entity.Entity;

import mod.azure.azurelib.platform.mc.Mc;

/**
 * Tracks the per-tick positional delta of an entity so animators can cheaply ask whether it is moving.
 */
public class MoveAnalysis {

    private final Entity entity;

    private int lastTick;

    private double lastX;

    private double lastY;

    private double lastZ;

    private double deltaX;

    private double deltaY;

    private double deltaZ;

    public MoveAnalysis(Entity entity) {
        this.entity = entity;
        this.lastX = Mc.x(entity);
        this.lastY = Mc.y(entity);
        this.lastZ = Mc.z(entity);
    }

    public void update() {
        if (Mc.age(entity) == lastTick) {
            return;
        }

        this.deltaX = Mc.x(entity) - lastX;
        this.deltaY = Mc.y(entity) - lastY;
        this.deltaZ = Mc.z(entity) - lastZ;

        this.lastX = Mc.x(entity);
        this.lastY = Mc.y(entity);
        this.lastZ = Mc.z(entity);
        this.lastTick = Mc.age(entity);
    }

    public boolean isMovingHorizontally() {
        return deltaX != 0 || deltaZ != 0;
    }

    public boolean isMovingVertically() {
        return deltaY != 0;
    }

    public boolean isMoving() {
        return isMovingHorizontally() || isMovingVertically();
    }
}
