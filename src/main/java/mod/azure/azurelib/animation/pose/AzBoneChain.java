package mod.azure.azurelib.animation.pose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.util.math.Mth;
import mod.azure.azurelib.util.math.Vector3f;

/**
 * A chain of bones, each a descendant of the one before it, that a pose modifier can solve as a skeleton: read where
 * its joints are in model space, turn bones so a point lands somewhere in model space, then write the result back. IK
 * solvers, look-at and spring chains are built on it, and custom solvers can use it directly.
 * <p>
 * The chain works on its own copy of the chain bones' rotations, so a solver can iterate as often as it likes; nothing
 * reaches the pose until {@link #apply()}, which writes every chain bone once through {@link AzPoseBone#setRotation},
 * scaled by the modifier's weight. Everything else about the skeleton (ancestors, positions, scales) is read from the
 * current pose.
 * </p>
 * <p>
 * Model space is the model's own space in blocks, before the entity's rotation and scale: the space bone pivots are
 * authored in, with X mirrored the same way AzureLib mirrors it at load. Offsets passed to {@link #pointOnBone} use
 * Blockbench pixels relative to the bone's pivot, the same convention as hitbox offsets.
 * </p>
 * <p>
 * Rotations are solved assuming ancestors have uniform, positive scale. Non-uniform or mirrored scale on an ancestor
 * makes solved rotations approximate.
 * </p>
 * <p>
 * Obtain one per animatable with {@link #resolve} and keep it in {@link AzPoseContext#state}; it is tied to the model
 * it was resolved against, and the pose layer drops modifier state when the model changes.
 * </p>
 */
public final class AzBoneChain {

    private static final float PIXEL = 1 / 16f;

    private static final float TWO_PI = (float) (Math.PI * 2);

    private static final float EPSILON = 1e-7f;

    private final AzPoseBone[] path;

    private final int[] chainToPath;

    private final int[] pathToChain;

    private final float[] euler;

    private final float[] jointPos;

    private final float[] linear;

    private final float[] parentRot;

    private boolean fkDirty = true;

    private final float[] a = new float[9], b = new float[9], c = new float[9];

    private final float[] cand = new float[6];

    private final Vector3f scratchRotation = new Vector3f();

    private AzBoneChain(AzPoseBone[] path, int[] chainToPath) {
        this.path = path;
        this.chainToPath = chainToPath;
        this.pathToChain = new int[path.length];
        Arrays.fill(pathToChain, -1);

        for (int i = 0; i < chainToPath.length; i++) {
            pathToChain[chainToPath[i]] = i;
        }

        this.euler = new float[chainToPath.length * 3];
        this.jointPos = new float[path.length * 3];
        this.linear = new float[path.length * 9];
        this.parentRot = new float[path.length * 9];
    }

    /**
     * Resolves a chain against the model of the running modifier's animatable.
     *
     * @param boneNames the chain bones, root-most first; each must be a descendant of the one before it
     * @return the chain, or {@code null} if a bone is missing or the bones are not one descending chain
     */
    public static AzBoneChain resolve(AzPoseContext<?> pose, String... boneNames) {
        if (boneNames.length == 0) {
            return null;
        }

        AzPoseBone last = pose.bone(boneNames[boneNames.length - 1]);

        if (!last.exists()) {
            return null;
        }

        ArrayList<AzBone> bones = new ArrayList<>();

        for (AzBone bone = last.bone(); bone != null; bone = bone.getParent()) {
            bones.add(bone);
        }

        Collections.reverse(bones);

        AzPoseBone[] path = new AzPoseBone[bones.size()];

        for (int i = 0; i < path.length; i++) {
            path[i] = pose.bone(bones.get(i).getName());
        }

        int[] chainToPath = new int[boneNames.length];
        int searchFrom = 0;

        for (int i = 0; i < boneNames.length; i++) {
            int index = -1;

            for (int k = searchFrom; k < path.length; k++) {
                if (path[k].bone().getName().equals(boneNames[i])) {
                    index = k;
                    break;
                }
            }

            if (index < 0) {
                return null;
            }

            chainToPath[i] = index;
            searchFrom = index + 1;
        }

        AzBoneChain chain = new AzBoneChain(path, chainToPath);
        chain.reset();
        return chain;
    }

    public int size() {
        return chainToPath.length;
    }

    public AzPoseBone bone(int index) {
        return path[chainToPath[index]];
    }

    public void reset() {
        for (int i = 0; i < chainToPath.length; i++) {
            Vector3f rot = bone(i).currentRotation();
            euler[i * 3] = rot.x();
            euler[i * 3 + 1] = rot.y();
            euler[i * 3 + 2] = rot.z();
        }

        fkDirty = true;
    }

    public Vector3f rotation(int index, Vector3f dest) {
        return AzVectors.set(dest, euler[index * 3], euler[index * 3 + 1], euler[index * 3 + 2]);
    }

    public void setRotation(int index, float x, float y, float z) {
        euler[index * 3] = x;
        euler[index * 3 + 1] = y;
        euler[index * 3 + 2] = z;
        fkDirty = true;
    }

    public Vector3f joint(int index, Vector3f dest) {
        ensureFk();
        int o = chainToPath[index] * 3;
        return AzVectors.set(dest, jointPos[o], jointPos[o + 1], jointPos[o + 2]);
    }

    public Vector3f pointOnBone(int index, float x, float y, float z, Vector3f dest) {
        ensureFk();
        int k = chainToPath[index];
        float lx = -x * PIXEL;
        float ly = y * PIXEL;
        float lz = z * PIXEL;
        int m = k * 9;
        int o = k * 3;
        return AzVectors.set(
            dest,
            jointPos[o] + linear[m] * lx + linear[m + 1] * ly + linear[m + 2] * lz,
            jointPos[o + 1] + linear[m + 3] * lx + linear[m + 4] * ly + linear[m + 5] * lz,
            jointPos[o + 2] + linear[m + 6] * lx + linear[m + 7] * ly + linear[m + 8] * lz
        );
    }

    public Vector3f directionOnBone(int index, float x, float y, float z, Vector3f dest) {
        ensureFk();
        int m = chainToPath[index] * 9;
        float lx = -x;
        float dx = linear[m] * lx + linear[m + 1] * y + linear[m + 2] * z;
        float dy = linear[m + 3] * lx + linear[m + 4] * y + linear[m + 5] * z;
        float dz = linear[m + 6] * lx + linear[m + 7] * y + linear[m + 8] * z;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return len < EPSILON ? AzVectors.set(dest, 0, 0, 0) : AzVectors.set(dest, dx / len, dy / len, dz / len);
    }

    public boolean aim(int index, Vector3f from, Vector3f to, float fraction) {
        ensureFk();
        int o = chainToPath[index] * 3;
        float ux = from.x() - jointPos[o], uy = from.y() - jointPos[o + 1], uz = from.z() - jointPos[o + 2];
        float vx = to.x() - jointPos[o], vy = to.y() - jointPos[o + 1], vz = to.z() - jointPos[o + 2];
        float ul = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        float vl = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);

        if (ul < 1e-5f || vl < 1e-5f) {
            return false;
        }

        ux /= ul;
        uy /= ul;
        uz /= ul;
        vx /= vl;
        vy /= vl;
        vz /= vl;

        float ax = uy * vz - uz * vy, ay = uz * vx - ux * vz, az = ux * vy - uy * vx;
        float s = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        float cos = ux * vx + uy * vy + uz * vz;

        if (s < 1e-6f) {
            if (cos > 0) {
                return false;
            }

            // Opposite directions: any axis perpendicular to u works.
            if (Math.abs(ux) < 0.9f) {
                ax = 0;
                ay = uz;
                az = -uy;
            } else {
                ax = -uz;
                ay = 0;
                az = ux;
            }

            s = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        }

        float angle = (float) Math.atan2(s, cos) * fraction;

        if (Math.abs(angle) < 1e-6f) {
            return false;
        }

        rotateAround(index, ax / s, ay / s, az / s, angle);
        return true;
    }

    public void rotateAround(int index, float axisX, float axisY, float axisZ, float angle) {
        ensureFk();
        int k = chainToPath[index];
        int e = index * 3;

        axisAngle(axisX, axisY, axisZ, angle, a);
        // R' = Wp^T * Q * Wp * R
        mulTransposeA(parentRot, k * 9, a, b);
        mul(b, 0, parentRot, k * 9, c);
        eulerZYX(euler[e], euler[e + 1], euler[e + 2], a);
        mul(c, 0, a, 0, b);
        toEulerNearest(b, euler[e], euler[e + 1], euler[e + 2], e);
        fkDirty = true;
    }

    public void constrain(int index, AzBoneConstraint constraint) {
        AzPoseBone bone = bone(index);

        if (bone.bone() == null || !constraint.boneName().equals(bone.bone().getName())) {
            return;
        }

        int e = index * 3;
        Vector3f rot = AzVectors.set(scratchRotation, euler[e], euler[e + 1], euler[e + 2]);
        constraint.constrain(rot, bone.bindRotation());
        setRotation(index, rot.x, rot.y, rot.z);
    }

    public void apply() {
        for (int i = 0; i < chainToPath.length; i++) {
            bone(i).setRotation(euler[i * 3], euler[i * 3 + 1], euler[i * 3 + 2]);
        }
    }

    private void ensureFk() {
        if (!fkDirty) {
            return;
        }

        // Child frame of the previous bone: linear part F and translation (fx, fy, fz). Starts as model space.
        float[] f = c;
        identity(f);
        float fx = 0, fy = 0, fz = 0;
        identity(b);

        for (int k = 0; k < path.length; k++) {
            AzPoseBone poseBone = path[k];
            AzBone bone = poseBone.bone();
            Vector3f pos = poseBone.currentPosition();
            Vector3f scl = poseBone.currentScale();
            int chainIndex = pathToChain[k];
            float rx, ry, rz;

            if (chainIndex >= 0) {
                rx = euler[chainIndex * 3];
                ry = euler[chainIndex * 3 + 1];
                rz = euler[chainIndex * 3 + 2];
            } else {
                Vector3f rot = poseBone.currentRotation();
                rx = rot.x();
                ry = rot.y();
                rz = rot.z();
            }

            float px = bone.getPivotX() * PIXEL;
            float py = bone.getPivotY() * PIXEL;
            float pz = bone.getPivotZ() * PIXEL;
            float tx = -pos.x() * PIXEL + px;
            float ty = pos.y() * PIXEL + py;
            float tz = pos.z() * PIXEL + pz;

            int o = k * 3;
            jointPos[o] = f[0] * tx + f[1] * ty + f[2] * tz + fx;
            jointPos[o + 1] = f[3] * tx + f[4] * ty + f[5] * tz + fy;
            jointPos[o + 2] = f[6] * tx + f[7] * ty + f[8] * tz + fz;

            System.arraycopy(b, 0, parentRot, k * 9, 9);

            eulerZYX(rx, ry, rz, a);
            // W = W * R
            mul(parentRot, k * 9, a, 0, b);

            // A_J = F * R * S
            int m = k * 9;
            mul(f, 0, a, 0, linear, m);
            for (int row = 0; row < 3; row++) {
                linear[m + row * 3] *= scl.x();
                linear[m + row * 3 + 1] *= scl.y();
                linear[m + row * 3 + 2] *= scl.z();
            }

            // Next child frame: F = A_J, translation = joint - A_J * pivot
            System.arraycopy(linear, m, f, 0, 9);
            fx = jointPos[o] - (f[0] * px + f[1] * py + f[2] * pz);
            fy = jointPos[o + 1] - (f[3] * px + f[4] * py + f[5] * pz);
            fz = jointPos[o + 2] - (f[6] * px + f[7] * py + f[8] * pz);
        }

        fkDirty = false;
    }

    private static void identity(float[] m) {
        Arrays.fill(m, 0, 9, 0);
        m[0] = m[4] = m[8] = 1;
    }

    /** Rz(z) * Ry(y) * Rx(x), the order the renderer applies bone rotations in. */
    static void eulerZYX(float x, float y, float z, float[] m) {
        float cx = (float) Math.cos(x), sx = (float) Math.sin(x);
        float cy = (float) Math.cos(y), sy = (float) Math.sin(y);
        float cz = (float) Math.cos(z), sz = (float) Math.sin(z);
        m[0] = cz * cy;
        m[1] = cz * sy * sx - sz * cx;
        m[2] = cz * sy * cx + sz * sx;
        m[3] = sz * cy;
        m[4] = sz * sy * sx + cz * cx;
        m[5] = sz * sy * cx - cz * sx;
        m[6] = -sy;
        m[7] = cy * sx;
        m[8] = cy * cx;
    }

    private static void axisAngle(float x, float y, float z, float angle, float[] m) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle), t = 1 - c;
        m[0] = t * x * x + c;
        m[1] = t * x * y - s * z;
        m[2] = t * x * z + s * y;
        m[3] = t * x * y + s * z;
        m[4] = t * y * y + c;
        m[5] = t * y * z - s * x;
        m[6] = t * x * z - s * y;
        m[7] = t * y * z + s * x;
        m[8] = t * z * z + c;
    }

    private static void mul(float[] l, int lo, float[] r, int ro, float[] dest) {
        mul(l, lo, r, ro, dest, 0);
    }

    private static void mul(float[] l, int lo, float[] r, int ro, float[] dest, int d) {
        float r0 = 0, r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0, r6 = 0, r7 = 0, r8 = 0;
        r0 = l[lo] * r[ro] + l[lo + 1] * r[ro + 3] + l[lo + 2] * r[ro + 6];
        r1 = l[lo] * r[ro + 1] + l[lo + 1] * r[ro + 4] + l[lo + 2] * r[ro + 7];
        r2 = l[lo] * r[ro + 2] + l[lo + 1] * r[ro + 5] + l[lo + 2] * r[ro + 8];
        r3 = l[lo + 3] * r[ro] + l[lo + 4] * r[ro + 3] + l[lo + 5] * r[ro + 6];
        r4 = l[lo + 3] * r[ro + 1] + l[lo + 4] * r[ro + 4] + l[lo + 5] * r[ro + 7];
        r5 = l[lo + 3] * r[ro + 2] + l[lo + 4] * r[ro + 5] + l[lo + 5] * r[ro + 8];
        r6 = l[lo + 6] * r[ro] + l[lo + 7] * r[ro + 3] + l[lo + 8] * r[ro + 6];
        r7 = l[lo + 6] * r[ro + 1] + l[lo + 7] * r[ro + 4] + l[lo + 8] * r[ro + 7];
        r8 = l[lo + 6] * r[ro + 2] + l[lo + 7] * r[ro + 5] + l[lo + 8] * r[ro + 8];
        dest[d] = r0;
        dest[d + 1] = r1;
        dest[d + 2] = r2;
        dest[d + 3] = r3;
        dest[d + 4] = r4;
        dest[d + 5] = r5;
        dest[d + 6] = r6;
        dest[d + 7] = r7;
        dest[d + 8] = r8;
    }

    /** dest = transpose(l) * r */
    private static void mulTransposeA(float[] l, int lo, float[] r, float[] dest) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                dest[i * 3 + j] = l[lo + i] * r[j] + l[lo + 3 + i] * r[3 + j] + l[lo + 6 + i] * r[6 + j];
            }
        }
    }

    /**
     * Decomposes rotation matrix {@code m} into ZYX Euler angles, choosing among the equivalent solutions the one
     * closest to the previous angles so blending and later keyframes never take the long way round.
     */
    private void toEulerNearest(float[] m, float px, float py, float pz, int e) {
        float sy = Mth.clamp(-m[6], -1f, 1f);
        float y1 = (float) Math.asin(sy);
        float cy = (float) Math.cos(y1);
        int count;

        if (cy > 1e-5f) {
            float x1 = (float) Math.atan2(m[7], m[8]);
            float z1 = (float) Math.atan2(m[3], m[0]);
            cand[0] = x1;
            cand[1] = y1;
            cand[2] = z1;
            cand[3] = x1 + (float) Math.PI;
            cand[4] = (float) Math.PI - y1;
            cand[5] = z1 + (float) Math.PI;
            count = 2;
        } else if (sy > 0) {
            // y = +90deg: only x - z is defined; keep z.
            cand[0] = pz + (float) Math.atan2(m[1], m[2]);
            cand[1] = (float) (Math.PI / 2);
            cand[2] = pz;
            count = 1;
        } else {
            cand[0] = (float) Math.atan2(-m[1], -m[2]) - pz;
            cand[1] = (float) (-Math.PI / 2);
            cand[2] = pz;
            count = 1;
        }

        int best = 0;
        float bestDist = Float.MAX_VALUE;

        for (int i = 0; i < count; i++) {
            int o = i * 3;
            cand[o] = nearest(cand[o], px);
            cand[o + 1] = nearest(cand[o + 1], py);
            cand[o + 2] = nearest(cand[o + 2], pz);
            float dx = cand[o] - px;
            float dy = cand[o + 1] - py;
            float dz = cand[o + 2] - pz;
            float dist = dx * dx + dy * dy + dz * dz;

            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }

        euler[e] = cand[best * 3];
        euler[e + 1] = cand[best * 3 + 1];
        euler[e + 2] = cand[best * 3 + 2];
    }

    private static float nearest(float angle, float reference) {
        int turns = Math.round((reference - angle) / TWO_PI);
        return angle + turns * TWO_PI;
    }
}
