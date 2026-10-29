package mod.azure.azurelib.common.socket;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Where a socket is in the world: a position plus the socket's axes in world space. Directions and points are given in
 * the socket's Blockbench axes, so {@code direction(0, 0, -1)} is the way a part modelled facing north points, and
 * {@code point(0, 0, -4)} is four pixels in front of the socket.
 * <p>
 * Reuse one instance; {@link AzBoneSockets#resolve} overwrites it.
 * </p>
 */
public final class AzSocketTransform {

    private double x, y, z;

    private final Vector3f axisX = new Vector3f(1, 0, 0), axisY = new Vector3f(0, 1, 0), axisZ = new Vector3f(0, 0, 1);

    private float scaleX = 1 / 16f, scaleY = 1 / 16f, scaleZ = 1 / 16f;

    void set(double x, double y, double z, Vector3f axisX, Vector3f axisY, Vector3f axisZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.scaleX = axisX.length() / 16f;
        this.scaleY = axisY.length() / 16f;
        this.scaleZ = axisZ.length() / 16f;
        normalizeInto(this.axisX, axisX);
        normalizeInto(this.axisY, axisY);
        normalizeInto(this.axisZ, axisZ);
    }

    private static void normalizeInto(Vector3f dest, Vector3f source) {
        float length = source.length();

        if (length < 1e-8f) {
            dest.zero();
        } else {
            dest.set(source).div(length);
        }
    }

    public Vec3 position() {
        return new Vec3(x, y, z);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public Vec3 point(float bbX, float bbY, float bbZ) {
        float lx = -bbX * scaleX;
        float ly = bbY * scaleY;
        float lz = bbZ * scaleZ;
        return new Vec3(
            x + axisX.x * lx + axisY.x * ly + axisZ.x * lz,
            y + axisX.y * lx + axisY.y * ly + axisZ.y * lz,
            z + axisX.z * lx + axisY.z * ly + axisZ.z * lz
        );
    }

    public Vec3 direction(float bbX, float bbY, float bbZ) {
        float lx = -bbX;
        float dx = axisX.x * lx + axisY.x * bbY + axisZ.x * bbZ;
        float dy = axisX.y * lx + axisY.y * bbY + axisZ.y * bbZ;
        float dz = axisX.z * lx + axisY.z * bbY + axisZ.z * bbZ;
        return new Vec3(dx, dy, dz).normalize();
    }

    public Vec3 forward() {
        return direction(0, 0, -1);
    }

    public Vec3 up() {
        return direction(0, 1, 0);
    }

    public Quaternionf rotation(Quaternionf dest) {
        return dest.setFromNormalized(
            new Matrix3f(
                axisX.x,
                axisX.y,
                axisX.z,
                axisY.x,
                axisY.y,
                axisY.z,
                axisZ.x,
                axisZ.y,
                axisZ.z
            )
        );
    }
}
