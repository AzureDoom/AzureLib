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
        Pose pose = this.poseStack.getLast();
        pose.pose.multiply(Matrix4f.createScaleMatrix(x, y, z));
        if (x == y && y == z) {
            if (x > 0.0F) {
                return;
            }
            pose.normal.mul(-1.0F);
        }
        float invX = 1.0F / x;
        float invY = 1.0F / y;
        float invZ = 1.0F / z;
        float cubeRoot = Mth.fastInvCubeRoot(invX * invY * invZ);
        pose.normal.mul(Matrix3f.createScaleMatrix(cubeRoot * invX, cubeRoot * invY, cubeRoot * invZ));
    }

    public void mulPose(Quaternion quaternion) {
        Pose pose = this.poseStack.getLast();
        pose.pose.multiply(quaternion);
        pose.normal.mul(quaternion);
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
