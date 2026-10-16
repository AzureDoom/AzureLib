package mod.azure.azurelib.render.vertex;

import java.util.ArrayDeque;
import java.util.Deque;

import mod.azure.azurelib.util.math.Matrix3f;
import mod.azure.azurelib.util.math.Matrix4f;
import mod.azure.azurelib.util.math.Mth;
import mod.azure.azurelib.util.math.Quaternion;

/**
 * A software matrix stack mirroring the 1.18 {@code com.mojang.blaze3d.vertex.PoseStack} API.
 * <p>
 * Minecraft 1.7.10 renders with OpenGL's fixed-function matrix stack. AzureLib keeps its own software stack (so bone
 * matrices can be captured and re-used, exactly as on modern versions) and transforms vertices on the CPU. A fresh
 * {@link PoseStack} starts at identity, which represents whatever OpenGL model-view matrix is current when AzureLib's
 * renderers are invoked. Everything written through an {@link AzBufferSource} is therefore relative to that GL matrix,
 * and must be flushed ({@link AzBufferSource#endBatch()}) before the GL matrix changes.
 */
public class PoseStack {

    private final Deque<Pose> poseStack = new ArrayDeque<>();

    public PoseStack() {
        Matrix4f pose = new Matrix4f();
        pose.setIdentity();
        Matrix3f normal = new Matrix3f();
        normal.setIdentity();
        this.poseStack.addLast(new Pose(pose, normal));
    }

    public void translate(double x, double y, double z) {
        Pose pose = this.poseStack.getLast();
        pose.pose.multiplyWithTranslation((float) x, (float) y, (float) z);
    }

    public void scale(float x, float y, float z) {
        if (x == 1.0F && y == 1.0F && z == 1.0F) {
            return;
        }
        Pose pose = this.poseStack.getLast();
        pose.pose.multiplyWithScale(x, y, z);
        if (x == 0.0F || y == 0.0F || z == 0.0F) {
            // Collapsed axis: geometry is flat/invisible anyway; don't poison the normal matrix with 1/0.
            return;
        }
        if (x == y && y == z) {
            if (x > 0.0F) {
                return;
            }
            // Uniform negative scale: flip the normals. The cube-root scale below is then identity, so stop here.
            pose.normal.mul(-1.0F);
            return;
        }
        float invX = 1.0F / x;
        float invY = 1.0F / y;
        float invZ = 1.0F / z;
        float cubeRoot = Mth.fastInvCubeRoot(invX * invY * invZ);
        pose.normal.mulScale(cubeRoot * invX, cubeRoot * invY, cubeRoot * invZ);
    }

    public void mulPose(Quaternion quaternion) {
        Pose pose = this.poseStack.getLast();
        pose.pose.multiply(quaternion);
        pose.normal.mul(quaternion);
    }

    /**
     * Applies the rotation Rz * Ry * Rx (angles in radians) in one multiply. Equivalent to
     * {@code mulPose(Z(rz)); mulPose(Y(ry)); mulPose(X(rx));}, which is the bone rotation order, but with a single
     * matrix product for both the pose and the normal matrix and no quaternions. A rotation is orthonormal, so the
     * normal matrix takes the same R.
     */
    public void rotateZYX(float rz, float ry, float rx) {
        if (rz == 0.0F && ry == 0.0F && rx == 0.0F) {
            return;
        }
        float cz = 1.0F, sz = 0.0F, cy = 1.0F, sy = 0.0F, cx = 1.0F, sx = 0.0F;
        if (rz != 0.0F) {
            cz = Mth.cos(rz);
            sz = Mth.sin(rz);
        }
        if (ry != 0.0F) {
            cy = Mth.cos(ry);
            sy = Mth.sin(ry);
        }
        if (rx != 0.0F) {
            cx = Mth.cos(rx);
            sx = Mth.sin(rx);
        }
        float r00 = cz * cy, r01 = cz * sy * sx - sz * cx, r02 = cz * sy * cx + sz * sx;
        float r10 = sz * cy, r11 = sz * sy * sx + cz * cx, r12 = sz * sy * cx - cz * sx;
        float r20 = -sy, r21 = cy * sx, r22 = cy * cx;

        Pose pose = this.poseStack.getLast();
        pose.pose.multiplyRotation(r00, r01, r02, r10, r11, r12, r20, r21, r22);
        pose.normal.mulRotation(r00, r01, r02, r10, r11, r12, r20, r21, r22);
    }

    public void mulPoseMatrix(Matrix4f matrix) {
        this.poseStack.getLast().pose.multiply(matrix);
    }

    public void pushPose() {
        Pose pose = this.poseStack.getLast();
        this.poseStack.addLast(new Pose(pose.pose.copy(), pose.normal.copy()));
    }

    public void popPose() {
        this.poseStack.removeLast();
    }

    public Pose last() {
        return this.poseStack.getLast();
    }

    public boolean clear() {
        return this.poseStack.size() == 1;
    }

    public void setIdentity() {
        Pose pose = this.poseStack.getLast();
        pose.pose.setIdentity();
        pose.normal.setIdentity();
    }

    public static final class Pose {

        final Matrix4f pose;

        final Matrix3f normal;

        Pose(Matrix4f pose, Matrix3f normal) {
            this.pose = pose;
            this.normal = normal;
        }

        public Matrix4f pose() {
            return this.pose;
        }

        public Matrix3f normal() {
            return this.normal;
        }
    }
}
