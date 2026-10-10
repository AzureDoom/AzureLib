package mod.azure.azurelib.util;

import net.minecraft.entity.Entity;

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
        this.lastX = entity.posX;
        this.lastY = entity.posY;
        this.lastZ = entity.posZ;
    }

    public void update() {
        if (entity.ticksExisted == lastTick) {
            return;
        }

        this.deltaX = entity.posX - lastX;
        this.deltaY = entity.posY - lastY;
        this.deltaZ = entity.posZ - lastZ;

        this.lastX = entity.posX;
        this.lastY = entity.posY;
        this.lastZ = entity.posZ;
        this.lastTick = entity.ticksExisted;
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
