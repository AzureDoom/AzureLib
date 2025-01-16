package mod.azure.azurelib.rewrite.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class MoveAnalysis {

    private final Entity entity;

    private int lastTick;

    private Vec3 lastPosition;

    private double deltaX;

    private double deltaY;

    private double deltaZ;

    public MoveAnalysis(Entity entity) {
        this.entity = entity;
        this.lastPosition = entity.position();
    }

    public void update() {
        if (entity.tickCount == lastTick) {
            // Only update on tick differences.
            return;
        }

        var prevPos = lastPosition;
        var prevPosX = prevPos.x;
        var prevPosY = prevPos.y;
        var prevPosZ = prevPos.z;

        var pos = entity.position();
        var posX = pos.x;
        var posY = pos.y;
        var posZ = pos.z;

        this.deltaX = posX - prevPosX;
        this.deltaY = posY - prevPosY;
        this.deltaZ = posZ - prevPosZ;

        this.lastPosition = entity.position();
        this.lastTick = entity.tickCount;
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
