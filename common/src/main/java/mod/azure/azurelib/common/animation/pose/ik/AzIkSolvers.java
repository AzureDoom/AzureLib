package mod.azure.azurelib.common.animation.pose.ik;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

import mod.azure.azurelib.common.animation.pose.AzBoneChain;
import mod.azure.azurelib.common.animation.pose.AzBoneConstraint;

/**
 * The built-in IK solvers.
 */
public final class AzIkSolvers {

    private AzIkSolvers() {}

    /**
     * Analytic two-bone IK for legs and arms: the first two chain bones (upper and lower limb) bend so the effector on
     * the last chain bone reaches the target exactly when it is in reach, and points at it when it is not. The bend
     * keeps the plane the animation put the limb in, so knees and elbows keep facing the way they were animated.
     * Typical chains are {@code (thigh, shin)} with the effector at the ankle, or {@code (thigh, shin, foot)} with the
     * effector at the foot's pivot.
     */
    public static TwoBone twoBone() {
        return new TwoBone();
    }

    /**
     * Cyclic coordinate descent for chains of any length, such as tails, tentacles and spines: each pass turns every
     * chain bone, tip first, to point the effector at the target. Constraints are applied after every turn.
     *
     * @param iterations maximum passes over the chain; 8 to 16 is typical
     * @param tolerance  stop once the effector is this close to the target, in model pixels
     */
    public static AzIkSolver ccd(int iterations, float tolerance) {
        if (iterations < 1) {
            throw new IllegalArgumentException("iterations must be at least 1");
        }

        float toleranceSq = (tolerance / 16f) * (tolerance / 16f);
        Vector3f effectorPos = new Vector3f();

        return (chain, effector, target, constraints) -> {
            for (int iteration = 0; iteration < iterations; iteration++) {
                for (int i = chain.size() - 1; i >= 0; i--) {
                    chain.aim(i, effector.position(chain, effectorPos), target, 1);
                    applyConstraints(chain, i, constraints);
                }

                if (effector.position(chain, effectorPos).distanceSquared(target) <= toleranceSq) {
                    return;
                }
            }
        };
    }

    static void applyConstraints(AzBoneChain chain, int index, List<AzBoneConstraint> constraints) {
        for (int i = 0, n = constraints.size(); i < n; i++) {
            chain.constrain(index, constraints.get(i));
        }
    }

    /** See {@link #twoBone()}. */
    public static final class TwoBone implements AzIkSolver {

        private boolean hasPole;

        private float poleX, poleY, poleZ;

        private final Vector3f a = new Vector3f(), b = new Vector3f(), c = new Vector3f(), t = new Vector3f();

        private TwoBone() {}

        /**
         * Makes the middle joint (knee, elbow) turn towards this model-space point, in Blockbench pixels from the
         * model's origin, instead of keeping the animated bend plane. For legs, a point well in front of the knee.
         */
        public TwoBone pole(float x, float y, float z) {
            this.hasPole = true;
            this.poleX = -x / 16f;
            this.poleY = y / 16f;
            this.poleZ = z / 16f;
            return this;
        }

        @Override
        public void solve(
            AzBoneChain chain,
            AzIkEffector effector,
            Vector3fc target,
            List<AzBoneConstraint> constraints
        ) {
            if (chain.size() < 2) {
                return;
            }

            chain.joint(0, a);
            chain.joint(1, b);
            effector.position(chain, c);

            float upper = a.distance(b);
            float lower = b.distance(c);

            if (upper < 1e-5f || lower < 1e-5f) {
                return;
            }

            // 1. Bend the middle joint so the root-to-effector distance matches the root-to-target distance.
            float reach = clamp(a.distance(target), Math.abs(upper - lower) + 1e-4f, upper + lower - 1e-4f);
            float ux = a.x - b.x, uy = a.y - b.y, uz = a.z - b.z;
            float vx = c.x - b.x, vy = c.y - b.y, vz = c.z - b.z;
            float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
            float nLen = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

            if (nLen < 1e-6f) {
                // Straight limb: bend around the upper bone's X axis; a pole, if any, fixes the side below.
                chain.directionOnBone(0, 1, 0, 0, t);
                nx = t.x;
                ny = t.y;
                nz = t.z;
                nLen = 1;
            }

            float current = (float) Math.acos(clamp((ux * vx + uy * vy + uz * vz) / (upper * lower), -1, 1));
            float desired = (float) Math.acos(
                clamp((upper * upper + lower * lower - reach * reach) / (2 * upper * lower), -1, 1)
            );

            chain.rotateAround(1, nx / nLen, ny / nLen, nz / nLen, desired - current);
            applyConstraints(chain, 1, constraints);

            // 2. Swing the upper bone so the effector points at the target.
            chain.aim(0, effector.position(chain, c), target, 1);
            applyConstraints(chain, 0, constraints);

            // 3. Twist around the root-to-target axis so the middle joint faces the pole.
            if (hasPole) {
                twistTowardsPole(chain, target, constraints);
            }
        }

        private void twistTowardsPole(AzBoneChain chain, Vector3fc target, List<AzBoneConstraint> constraints) {
            chain.joint(0, a);
            chain.joint(1, b);
            float kx = target.x() - a.x, ky = target.y() - a.y, kz = target.z() - a.z;
            float kLen = (float) Math.sqrt(kx * kx + ky * ky + kz * kz);

            if (kLen < 1e-5f) {
                return;
            }

            kx /= kLen;
            ky /= kLen;
            kz /= kLen;

            // Project the middle joint and the pole onto the plane perpendicular to the axis.
            float bx = b.x - a.x, by = b.y - a.y, bz = b.z - a.z;
            float bd = bx * kx + by * ky + bz * kz;
            bx -= kx * bd;
            by -= ky * bd;
            bz -= kz * bd;
            float px = poleX - a.x, py = poleY - a.y, pz = poleZ - a.z;
            float pd = px * kx + py * ky + pz * kz;
            px -= kx * pd;
            py -= ky * pd;
            pz -= kz * pd;

            if (bx * bx + by * by + bz * bz < 1e-10f || px * px + py * py + pz * pz < 1e-10f) {
                return;
            }

            float cx = by * pz - bz * py, cy = bz * px - bx * pz, cz = bx * py - by * px;
            float angle = (float) Math.atan2(cx * kx + cy * ky + cz * kz, bx * px + by * py + bz * pz);
            chain.rotateAround(0, kx, ky, kz, angle);
            applyConstraints(chain, 0, constraints);
        }

        private static float clamp(float value, float min, float max) {
            return value < min ? min : Math.min(value, max);
        }
    }
}
