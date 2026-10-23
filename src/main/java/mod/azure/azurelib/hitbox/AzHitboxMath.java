package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public final class AzHitboxMath {

    private static final double DEG_TO_RAD = Math.PI / 180.0;

    private AzHitboxMath() {
        throw new UnsupportedOperationException();
    }

    public static Vec3 vec(double x, double y, double z) {
        return Vec3.createVectorHelper(x, y, z);
    }

    public static Vec3 add(Vec3 a, Vec3 b) {
        return vec(a.xCoord + b.xCoord, a.yCoord + b.yCoord, a.zCoord + b.zCoord);
    }

    public static Vec3 scale(Vec3 v, double factor) {
        return vec(v.xCoord * factor, v.yCoord * factor, v.zCoord * factor);
    }

    public static Vec3 lerp(Vec3 from, Vec3 to, double t) {
        return vec(
            from.xCoord + (to.xCoord - from.xCoord) * t,
            from.yCoord + (to.yCoord - from.yCoord) * t,
            from.zCoord + (to.zCoord - from.zCoord) * t
        );
    }

    public static Vec3 along(Vec3 origin, Vec3 direction, double distance) {
        return vec(
            origin.xCoord + direction.xCoord * distance,
            origin.yCoord + direction.yCoord * distance,
            origin.zCoord + direction.zCoord * distance
        );
    }

    public static AxisAlignedBB grow(AxisAlignedBB box, double amount) {
        return AxisAlignedBB.getBoundingBox(
            box.minX - amount,
            box.minY - amount,
            box.minZ - amount,
            box.maxX + amount,
            box.maxY + amount,
            box.maxZ + amount
        );
    }

    public static AxisAlignedBB union(AxisAlignedBB a, AxisAlignedBB b) {
        return AxisAlignedBB.getBoundingBox(
            Math.min(a.minX, b.minX),
            Math.min(a.minY, b.minY),
            Math.min(a.minZ, b.minZ),
            Math.max(a.maxX, b.maxX),
            Math.max(a.maxY, b.maxY),
            Math.max(a.maxZ, b.maxZ)
        );
    }

    public static Vec3 center(AxisAlignedBB box) {
        return vec((box.minX + box.maxX) * 0.5, (box.minY + box.maxY) * 0.5, (box.minZ + box.maxZ) * 0.5);
    }

    public static double distanceToSqr(AxisAlignedBB box, Vec3 point) {
        double dx = Math.max(Math.max(box.minX - point.xCoord, 0), point.xCoord - box.maxX);
        double dy = Math.max(Math.max(box.minY - point.yCoord, 0), point.yCoord - box.maxY);
        double dz = Math.max(Math.max(box.minZ - point.zCoord, 0), point.zCoord - box.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    public static Vec3 eyePosition(EntityPlayer player) {
        double eye = player.worldObj.isRemote
            ? player.getEyeHeight() - player.getDefaultEyeHeight()
            : player.getEyeHeight();
        return vec(player.posX, player.posY + eye, player.posZ);
    }

    public static Vec3 look(Entity entity) {
        double yaw = -entity.rotationYaw * DEG_TO_RAD - Math.PI;
        double pitch = -entity.rotationPitch * DEG_TO_RAD;
        double horizontal = -Math.cos(pitch);
        return vec(Math.sin(yaw) * horizontal, Math.sin(pitch), Math.cos(yaw) * horizontal);
    }
}
