package mod.azure.azurelib.socket;

import mod.azure.azurelib.util.math.Quaternion;
import mod.azure.azurelib.util.math.Vec3;

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

    /**
     * World-space unit axes of the socket's local X, Y and Z (in model axes, where X is mirrored from Blockbench), as
     * columns: {@code [x0, x1, x2, y0, y1, y2, z0, z1, z2]}.
     */
    private final float[] axes = { 1, 0, 0, 0, 1, 0, 0, 0, 1 };

    /** World-space length of one Blockbench pixel along each axis, including model and bone scale. */
    private float scaleX = 1 / 16f, scaleY = 1 / 16f, scaleZ = 1 / 16f;

    /** Sets the position and the axes, given as columns one model unit long. */
    void set(double x, double y, double z, float[] basis) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.scaleX = normalizeColumn(basis, 0) / 16f;
        this.scaleY = normalizeColumn(basis, 3) / 16f;
        this.scaleZ = normalizeColumn(basis, 6) / 16f;
    }

    /** Copies a column of {@code basis} into {@link #axes}, normalized, and returns its original length. */
    private float normalizeColumn(float[] basis, int o) {
        float length = (float) Math.sqrt(
            basis[o] * basis[o] + basis[o + 1] * basis[o + 1] + basis[o + 2] * basis[o + 2]
        );

        if (length < 1e-8f) {
            axes[o] = axes[o + 1] = axes[o + 2] = 0;
        } else {
            axes[o] = basis[o] / length;
            axes[o + 1] = basis[o + 1] / length;
            axes[o + 2] = basis[o + 2] / length;
        }

        return length;
    }

    /** The socket's world position. */
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

    /** A point fixed to the socket, in Blockbench pixels along its axes, as a world position. */
    public Vec3 point(float bbX, float bbY, float bbZ) {
        float lx = -bbX * scaleX;
        float ly = bbY * scaleY;
        float lz = bbZ * scaleZ;
        return new Vec3(
            x + axes[0] * lx + axes[3] * ly + axes[6] * lz,
            y + axes[1] * lx + axes[4] * ly + axes[7] * lz,
            z + axes[2] * lx + axes[5] * ly + axes[8] * lz
        );
    }

    /** A direction fixed to the socket, in its Blockbench axes, as a normalized world direction. */
    public Vec3 direction(float bbX, float bbY, float bbZ) {
        float lx = -bbX;
        double dx = axes[0] * lx + axes[3] * bbY + axes[6] * bbZ;
        double dy = axes[1] * lx + axes[4] * bbY + axes[7] * bbZ;
        double dz = axes[2] * lx + axes[5] * bbY + axes[8] * bbZ;
        return new Vec3(dx, dy, dz).normalize();
    }

    /** The socket's front: {@link #direction direction(0, 0, -1)}, the way a part modelled facing north points. */
    public Vec3 forward() {
        return direction(0, 0, -1);
    }

    /** The socket's up: {@link #direction direction(0, 1, 0)}. */
    public Vec3 up() {
        return direction(0, 1, 0);
    }

    /** The socket's orientation in world space, in model axes (X mirrored from Blockbench). */
    public Quaternion rotation() {
        // Rotation matrix with the axes as columns, converted with Shepperd's method.
        float m00 = axes[0], m10 = axes[1], m20 = axes[2];
        float m01 = axes[3], m11 = axes[4], m21 = axes[5];
        float m02 = axes[6], m12 = axes[7], m22 = axes[8];
        float trace = m00 + m11 + m22;
        float qx, qy, qz, qw;

        if (trace > 0) {
            float s = (float) Math.sqrt(trace + 1) * 2;
            qw = 0.25f * s;
            qx = (m21 - m12) / s;
            qy = (m02 - m20) / s;
            qz = (m10 - m01) / s;
        } else if (m00 > m11 && m00 > m22) {
            float s = (float) Math.sqrt(1 + m00 - m11 - m22) * 2;
            qw = (m21 - m12) / s;
            qx = 0.25f * s;
            qy = (m01 + m10) / s;
            qz = (m02 + m20) / s;
        } else if (m11 > m22) {
            float s = (float) Math.sqrt(1 + m11 - m00 - m22) * 2;
            qw = (m02 - m20) / s;
            qx = (m01 + m10) / s;
            qy = 0.25f * s;
            qz = (m12 + m21) / s;
        } else {
            float s = (float) Math.sqrt(1 + m22 - m00 - m11) * 2;
            qw = (m10 - m01) / s;
            qx = (m02 + m20) / s;
            qy = (m12 + m21) / s;
            qz = 0.25f * s;
        }

        return new Quaternion(qx, qy, qz, qw);
    }
}
