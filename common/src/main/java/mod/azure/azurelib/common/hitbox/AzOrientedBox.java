package mod.azure.azurelib.common.hitbox;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class AzOrientedBox {

    private static final double EPSILON = 1.0E-7;

    private final double cx, cy, cz;

    private final double hx, hy, hz;

    private final double ux, uy, uz;

    private final double vx, vy, vz;

    private final double wx, wy, wz;

    private AzOrientedBox(
        double cx,
        double cy,
        double cz,
        double hx,
        double hy,
        double hz,
        double ux,
        double uy,
        double uz,
        double vx,
        double vy,
        double vz,
        double wx,
        double wy,
        double wz
    ) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
        this.hx = Math.max(0, hx);
        this.hy = Math.max(0, hy);
        this.hz = Math.max(0, hz);
        this.ux = ux;
        this.uy = uy;
        this.uz = uz;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.wx = wx;
        this.wy = wy;
        this.wz = wz;
    }

    public static AzOrientedBox of(
        double cx,
        double cy,
        double cz,
        double hx,
        double hy,
        double hz,
        double qx,
        double qy,
        double qz,
        double qw
    ) {
        var len = Math.sqrt(qx * qx + qy * qy + qz * qz + qw * qw);

        if (len < EPSILON || !Double.isFinite(len)) {
            qx = 0;
            qy = 0;
            qz = 0;
            qw = 1;
        } else {
            qx /= len;
            qy /= len;
            qz /= len;
            qw /= len;
        }

        var xx = qx * qx;
        var yy = qy * qy;
        var zz = qz * qz;
        var xy = qx * qy;
        var xz = qx * qz;
        var yz = qy * qz;
        var wx = qw * qx;
        var wy = qw * qy;
        var wz = qw * qz;

        return new AzOrientedBox(
            cx,
            cy,
            cz,
            hx,
            hy,
            hz,
            1 - 2 * (yy + zz),
            2 * (xy + wz),
            2 * (xz - wy),
            2 * (xy - wz),
            1 - 2 * (xx + zz),
            2 * (yz + wx),
            2 * (xz + wy),
            2 * (yz - wx),
            1 - 2 * (xx + yy)
        );
    }

    public static AzOrientedBox of(AABB box) {
        return new AzOrientedBox(
            (box.minX + box.maxX) * 0.5,
            (box.minY + box.maxY) * 0.5,
            (box.minZ + box.maxZ) * 0.5,
            (box.maxX - box.minX) * 0.5,
            (box.maxY - box.minY) * 0.5,
            (box.maxZ - box.minZ) * 0.5,
            1,
            0,
            0,
            0,
            1,
            0,
            0,
            0,
            1
        );
    }

    public Vec3 center() {
        return new Vec3(cx, cy, cz);
    }

    public double centerX() {
        return cx;
    }

    public double centerY() {
        return cy;
    }

    public double centerZ() {
        return cz;
    }

    public double halfExtentX() {
        return hx;
    }

    public double halfExtentY() {
        return hy;
    }

    public double halfExtentZ() {
        return hz;
    }

    public Vec3 axisX() {
        return new Vec3(ux, uy, uz);
    }

    public Vec3 axisY() {
        return new Vec3(vx, vy, vz);
    }

    public Vec3 axisZ() {
        return new Vec3(wx, wy, wz);
    }

    public AzOrientedBox inflate(double amount) {
        if (amount == 0) {
            return this;
        }

        return new AzOrientedBox(
            cx,
            cy,
            cz,
            hx + amount,
            hy + amount,
            hz + amount,
            ux,
            uy,
            uz,
            vx,
            vy,
            vz,
            wx,
            wy,
            wz
        );
    }

    public AABB bounds() {
        var ex = Math.abs(ux) * hx + Math.abs(vx) * hy + Math.abs(wx) * hz;
        var ey = Math.abs(uy) * hx + Math.abs(vy) * hy + Math.abs(wy) * hz;
        var ez = Math.abs(uz) * hx + Math.abs(vz) * hy + Math.abs(wz) * hz;

        return new AABB(cx - ex, cy - ey, cz - ez, cx + ex, cy + ey, cz + ez);
    }

    public Vec3 toLocal(Vec3 point) {
        var dx = point.x - cx;
        var dy = point.y - cy;
        var dz = point.z - cz;

        return new Vec3(
            dx * ux + dy * uy + dz * uz,
            dx * vx + dy * vy + dz * vz,
            dx * wx + dy * wy + dz * wz
        );
    }

    public Vec3 toWorld(double lx, double ly, double lz) {
        return new Vec3(
            cx + ux * lx + vx * ly + wx * lz,
            cy + uy * lx + vy * ly + wy * lz,
            cz + uz * lx + vz * ly + wz * lz
        );
    }

    public boolean contains(Vec3 point) {
        var dx = point.x - cx;
        var dy = point.y - cy;
        var dz = point.z - cz;

        return Math.abs(dx * ux + dy * uy + dz * uz) <= hx
            && Math.abs(dx * vx + dy * vy + dz * vz) <= hy
            && Math.abs(dx * wx + dy * wy + dz * wz) <= hz;
    }

    public double clipFraction(Vec3 from, Vec3 to, boolean hitIfInside) {
        var px = from.x - cx;
        var py = from.y - cy;
        var pz = from.z - cz;
        var dx = to.x - from.x;
        var dy = to.y - from.y;
        var dz = to.z - from.z;

        var tMin = Double.NEGATIVE_INFINITY;
        var tMax = Double.POSITIVE_INFINITY;

        for (int axis = 0; axis < 3; axis++) {
            double ax, ay, az, half;

            switch (axis) {
                case 0 -> {
                    ax = ux;
                    ay = uy;
                    az = uz;
                    half = hx;
                }
                case 1 -> {
                    ax = vx;
                    ay = vy;
                    az = vz;
                    half = hy;
                }
                default -> {
                    ax = wx;
                    ay = wy;
                    az = wz;
                    half = hz;
                }
            }

            var e = px * ax + py * ay + pz * az;
            var f = dx * ax + dy * ay + dz * az;

            if (Math.abs(f) < EPSILON) {
                if (e < -half || e > half) {
                    return -1;
                }

                continue;
            }

            var t1 = (-half - e) / f;
            var t2 = (half - e) / f;

            if (t1 > t2) {
                var swap = t1;
                t1 = t2;
                t2 = swap;
            }

            if (t1 > tMin) {
                tMin = t1;
            }

            if (t2 < tMax) {
                tMax = t2;
            }

            if (tMin > tMax) {
                return -1;
            }
        }

        if (tMax < 0 || tMin > 1) {
            return -1;
        }

        if (tMin < 0) {
            return hitIfInside ? 0 : -1;
        }

        return tMin;
    }

    public Optional<Vec3> clip(Vec3 from, Vec3 to) {
        var t = clipFraction(from, to, false);

        if (t < 0) {
            return Optional.empty();
        }

        return Optional.of(from.add(to.subtract(from).scale(t)));
    }

    public Vec3 closestPoint(Vec3 point) {
        var local = toLocal(point);

        return toWorld(
            clamp(local.x, -hx, hx),
            clamp(local.y, -hy, hy),
            clamp(local.z, -hz, hz)
        );
    }

    public double distanceToSqr(Vec3 point) {
        var local = toLocal(point);
        var dx = Math.max(0, Math.abs(local.x) - hx);
        var dy = Math.max(0, Math.abs(local.y) - hy);
        var dz = Math.max(0, Math.abs(local.z) - hz);

        return dx * dx + dy * dy + dz * dz;
    }

    public boolean intersects(AABB box) {
        return intersects(of(box));
    }

    public boolean intersects(AzOrientedBox o) {
        double[] ea = { hx, hy, hz };
        double[] eb = { o.hx, o.hy, o.hz };
        double[][] a = { { ux, uy, uz }, { vx, vy, vz }, { wx, wy, wz } };
        double[][] b = { { o.ux, o.uy, o.uz }, { o.vx, o.vy, o.vz }, { o.wx, o.wy, o.wz } };
        var r = new double[3][3];
        var absR = new double[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = dot(a[i], b[j]);
                absR[i][j] = Math.abs(r[i][j]) + EPSILON;
            }
        }

        double[] d = { o.cx - cx, o.cy - cy, o.cz - cz };
        double[] t = { dot(d, a[0]), dot(d, a[1]), dot(d, a[2]) };
        double ra, rb;

        for (int i = 0; i < 3; i++) {
            ra = ea[i];
            rb = eb[0] * absR[i][0] + eb[1] * absR[i][1] + eb[2] * absR[i][2];

            if (Math.abs(t[i]) > ra + rb) {
                return false;
            }
        }

        for (int j = 0; j < 3; j++) {
            ra = ea[0] * absR[0][j] + ea[1] * absR[1][j] + ea[2] * absR[2][j];
            rb = eb[j];

            if (Math.abs(t[0] * r[0][j] + t[1] * r[1][j] + t[2] * r[2][j]) > ra + rb) {
                return false;
            }
        }

        ra = ea[1] * absR[2][0] + ea[2] * absR[1][0];
        rb = eb[1] * absR[0][2] + eb[2] * absR[0][1];
        if (Math.abs(t[2] * r[1][0] - t[1] * r[2][0]) > ra + rb)
            return false;

        ra = ea[1] * absR[2][1] + ea[2] * absR[1][1];
        rb = eb[0] * absR[0][2] + eb[2] * absR[0][0];
        if (Math.abs(t[2] * r[1][1] - t[1] * r[2][1]) > ra + rb)
            return false;

        ra = ea[1] * absR[2][2] + ea[2] * absR[1][2];
        rb = eb[0] * absR[0][1] + eb[1] * absR[0][0];
        if (Math.abs(t[2] * r[1][2] - t[1] * r[2][2]) > ra + rb)
            return false;

        ra = ea[0] * absR[2][0] + ea[2] * absR[0][0];
        rb = eb[1] * absR[1][2] + eb[2] * absR[1][1];
        if (Math.abs(t[0] * r[2][0] - t[2] * r[0][0]) > ra + rb)
            return false;

        ra = ea[0] * absR[2][1] + ea[2] * absR[0][1];
        rb = eb[0] * absR[1][2] + eb[2] * absR[1][0];
        if (Math.abs(t[0] * r[2][1] - t[2] * r[0][1]) > ra + rb)
            return false;

        ra = ea[0] * absR[2][2] + ea[2] * absR[0][2];
        rb = eb[0] * absR[1][1] + eb[1] * absR[1][0];
        if (Math.abs(t[0] * r[2][2] - t[2] * r[0][2]) > ra + rb)
            return false;

        ra = ea[0] * absR[1][0] + ea[1] * absR[0][0];
        rb = eb[1] * absR[2][2] + eb[2] * absR[2][1];
        if (Math.abs(t[1] * r[0][0] - t[0] * r[1][0]) > ra + rb)
            return false;

        ra = ea[0] * absR[1][1] + ea[1] * absR[0][1];
        rb = eb[0] * absR[2][2] + eb[2] * absR[2][0];
        if (Math.abs(t[1] * r[0][1] - t[0] * r[1][1]) > ra + rb)
            return false;

        ra = ea[0] * absR[1][2] + ea[1] * absR[0][2];
        rb = eb[0] * absR[2][1] + eb[1] * absR[2][0];

        return !(Math.abs(t[1] * r[0][2] - t[0] * r[1][2]) > ra + rb);
    }

    public Vec3[] corners() {
        var corners = new Vec3[8];
        var i = 0;

        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    corners[i++] = toWorld(sx * hx, sy * hy, sz * hz);
                }
            }
        }

        return corners;
    }

    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    @Override
    public String toString() {
        return "AzOrientedBox[center=(" + cx + ", " + cy + ", " + cz + "), half=(" + hx + ", " + hy + ", " + hz + ")]";
    }
}
