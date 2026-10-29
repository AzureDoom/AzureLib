package mod.azure.azurelib.socket;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneLocator;

/**
 * Client half of {@link AzBoneSockets}, kept separate so the server never loads the animator classes it touches.
 */
final class AzBoneSocketsClient {

    private static final Matrix3f BASIS = new Matrix3f();

    private static final Matrix3f ROTATION = new Matrix3f();

    private static final Vector3f POINT = new Vector3f();

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
        matrix.transformPosition(-ox / 16f, oy / 16f, oz / 16f, POINT);

        matrix.get3x3(BASIS);

        if (locator != null) {
            BASIS.mul(
                AzBoneSockets.socketRotation(locator.rotationX(), locator.rotationY(), locator.rotationZ(), ROTATION)
            );
        }

        BASIS.mul(AzBoneSockets.socketRotation(socket.rotationX(), socket.rotationY(), socket.rotationZ(), ROTATION));

        Vec3 origin = entity.getPosition(partialTick);
        AzBoneSockets.setFromBasis(dest, origin.x + POINT.x, origin.y + POINT.y, origin.z + POINT.z, BASIS);
        return true;
    }

    static @Nullable AzBone findLocatorBone(AzBakedModel model, String locator) {
        for (AzBone bone : model.getBonesByName().values()) {
            if (bone.getLocators().containsKey(locator)) {
                return bone;
            }
        }

        return null;
    }
}
