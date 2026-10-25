package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class AzHitboxMath {

    private static final double DEG_TO_RAD = Math.PI / 180.0;

    private static final double PLAYER_EYE_HEIGHT = 1.62;

    private AzHitboxMath() {
        throw new UnsupportedOperationException();
    }

    public static Vec3d vec(double x, double y, double z) {
        return Vec3d.create(x, y, z);
    }

    public static Vec3d add(Vec3d a, Vec3d b) {
        return vec(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vec3d scale(Vec3d v, double factor) {
        return vec(v.x * factor, v.y * factor, v.z * factor);
    }

    public static Vec3d lerp(Vec3d from, Vec3d to, double t) {
        return vec(
            from.x + (to.x - from.x) * t,
            from.y + (to.y - from.y) * t,
            from.z + (to.z - from.z) * t
        );
    }

    public static Vec3d along(Vec3d origin, Vec3d direction, double distance) {
        return vec(
            origin.x + direction.x * distance,
            origin.y + direction.y * distance,
            origin.z + direction.z * distance
        );
    }

    public static Box grow(Box box, double amount) {
        return Box.create(
            box.minX - amount,
            box.minY - amount,
            box.minZ - amount,
            box.maxX + amount,
            box.maxY + amount,
            box.maxZ + amount
        );
    }

    public static Box union(Box a, Box b) {
        return Box.create(
            Math.min(a.minX, b.minX),
            Math.min(a.minY, b.minY),
            Math.min(a.minZ, b.minZ),
            Math.max(a.maxX, b.maxX),
            Math.max(a.maxY, b.maxY),
            Math.max(a.maxZ, b.maxZ)
        );
    }

    public static Vec3d center(Box box) {
        return vec((box.minX + box.maxX) * 0.5, (box.minY + box.maxY) * 0.5, (box.minZ + box.maxZ) * 0.5);
    }

    public static double distanceToSqr(Box box, Vec3d point) {
        double dx = Math.max(Math.max(box.minX - point.x, 0), point.x - box.maxX);
        double dy = Math.max(Math.max(box.minY - point.y, 0), point.y - box.maxY);
        double dz = Math.max(Math.max(box.minZ - point.z, 0), point.z - box.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Beta's local player stores {@code y} at eye level (yOffset 1.62) even in single player, where the world is not
     * remote, while the server's player stores it at the feet. The bounding box bottom is the feet on both, so the eye
     * is derived from it instead of from {@code y}.
     */
    public static Vec3d eyePosition(PlayerEntity player) {
        return vec(player.x, player.boundingBox.minY + PLAYER_EYE_HEIGHT, player.z);
    }

    public static Vec3d look(Entity entity) {
        double yaw = -entity.yaw * DEG_TO_RAD - Math.PI;
        double pitch = -entity.pitch * DEG_TO_RAD;
        double horizontal = -Math.cos(pitch);
        return vec(Math.sin(yaw) * horizontal, Math.sin(pitch), Math.cos(yaw) * horizontal);
    }
}
