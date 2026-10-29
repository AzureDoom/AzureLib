package mod.azure.azurelib.socket;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneLocator;
import mod.azure.azurelib.util.math.Matrix4f;
import mod.azure.azurelib.util.math.Vector4f;

/**
 * Client half of {@link AzBoneSockets}, kept separate so the server never loads the animator classes it touches.
 */
final class AzBoneSocketsClient {

    private static final Vector4f SCRATCH = new Vector4f();

    private static final float[] BASIS = new float[9];

    private AzBoneSocketsClient() {}

    static boolean resolve(Entity entity, AzBoneSocket socket, float partialTick, AzSocketTransform dest) {
        AzAnimator<Object, Entity> animator = AzAnimatorAccessor.getOrNull(entity);

        if (animator == null || animator.context() == null) {
            return false;
        }

        AzBakedModel model = animator.context().boneCache().getBakedModel();
        AzBone bone;
        AzBoneLocator locator = null;

        if (socket.locator() != null) {
            bone = findLocatorBone(model, socket.locator());

            if (bone == null) {
                return false;
            }

            locator = bone.getLocators().get(socket.locator());
        } else {
            bone = model.getBoneOrNull(socket.bone());

            if (bone == null) {
                return false;
            }
        }

        if (!bone.isTrackingMatrices()) {
            bone.setTrackingMatrices(true);
            return false;
        }

        float ox = socket.offsetX() + (locator == null ? 0 : locator.offsetX());
        float oy = socket.offsetY() + (locator == null ? 0 : locator.offsetY());
        float oz = socket.offsetZ() + (locator == null ? 0 : locator.offsetZ());
        Matrix4f matrix = bone.getLocalSpaceMatrix();

        SCRATCH.set(-ox / 16f, oy / 16f, oz / 16f, 1);
        SCRATCH.transform(matrix);
        float px = SCRATCH.x(), py = SCRATCH.y(), pz = SCRATCH.z();

        // The bone's axes in world space, one model unit long (they carry the bone's and model's scale).
        column(matrix, 1, 0, 0, 0);
        column(matrix, 0, 1, 0, 3);
        column(matrix, 0, 0, 1, 6);

        if (locator != null) {
            AzBoneSockets.rotateBasis(BASIS, locator.rotationX(), locator.rotationY(), locator.rotationZ());
        }

        AzBoneSockets.rotateBasis(BASIS, socket.rotationX(), socket.rotationY(), socket.rotationZ());

        Vec3 origin = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
        dest.set(origin.xCoord + px, origin.yCoord + py, origin.zCoord + pz, BASIS);
        return true;
    }

    private static void column(Matrix4f matrix, float x, float y, float z, int offset) {
        SCRATCH.set(x, y, z, 0);
        SCRATCH.transform(matrix);
        BASIS[offset] = SCRATCH.x();
        BASIS[offset + 1] = SCRATCH.y();
        BASIS[offset + 2] = SCRATCH.z();
    }

    static AzBone findLocatorBone(AzBakedModel model, String locator) {
        for (AzBone bone : model.getBonesByName().values()) {
            if (bone.getLocators().containsKey(locator)) {
                return bone;
            }
        }

        return null;
    }
}
