package mod.azure.azurelib.common.socket;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import mod.azure.azurelib.common.hitbox.AzHitboxes;
import mod.azure.azurelib.common.hitbox.AzOrientedBox;

/**
 * Turns {@link AzBoneSocket}s into world positions and orientations.
 * <p>
 * On the client a socket follows its bone's pose from the last rendered frame (bones start tracking their matrices the
 * first time a socket asks, so the very first call returns {@code false}). Off-screen entities are not posed, so their
 * sockets hold their last on-screen pose unless the entity also has hitboxes, which pose it off-screen. On the server,
 * and on the client before the bone has been posed, a socket resolves through its {@link AzBoneSocket#serverHitbox
 * server hitbox} if it has one.
 * </p>
 */
public final class AzBoneSockets {

    private static final float DEG_TO_RAD = Mth.DEG_TO_RAD;

    private AzBoneSockets() {}

    /**
     * Resolves {@code socket} on {@code entity}.
     *
     * @param partialTick the frame's partial tick when called while rendering, otherwise 1
     * @return whether {@code dest} was filled; {@code false} if the socket can't be resolved yet
     */
    public static boolean resolve(Entity entity, AzBoneSocket socket, float partialTick, AzSocketTransform dest) {
        if (entity.level().isClientSide && AzBoneSocketsClient.resolve(entity, socket, partialTick, dest)) {
            return true;
        }

        return resolveThroughHitbox(entity, socket, dest);
    }

    /** The socket's world position, or {@code null} if it can't be resolved yet. */
    public static @Nullable Vec3 position(Entity entity, AzBoneSocket socket, float partialTick) {
        AzSocketTransform transform = new AzSocketTransform();
        return resolve(entity, socket, partialTick, transform) ? transform.position() : null;
    }

    private static boolean resolveThroughHitbox(Entity entity, AzBoneSocket socket, AzSocketTransform dest) {
        if (socket.serverHitbox() == null) {
            return false;
        }

        AzOrientedBox box = AzHitboxes.getWorldBox(entity, socket.serverHitbox());

        if (box == null) {
            return false;
        }

        Matrix3f basis = new Matrix3f(
            (float) box.axisX().x,
            (float) box.axisX().y,
            (float) box.axisX().z,
            (float) box.axisY().x,
            (float) box.axisY().y,
            (float) box.axisY().z,
            (float) box.axisZ().x,
            (float) box.axisZ().y,
            (float) box.axisZ().z
        );
        basis.mul(socketRotation(socket.rotationX(), socket.rotationY(), socket.rotationZ(), new Matrix3f()));
        setFromBasis(dest, box.centerX(), box.centerY(), box.centerZ(), basis);
        return true;
    }

    /** A Blockbench rotation in degrees as a bone-space rotation matrix (AzureLib negates X and Y at load). */
    static Matrix3f socketRotation(float x, float y, float z, Matrix3f dest) {
        return dest.rotationZYX(z * DEG_TO_RAD, -y * DEG_TO_RAD, -x * DEG_TO_RAD);
    }

    /** Fills {@code dest} from a position and a basis whose columns are the socket's axes, one model unit long. */
    static void setFromBasis(AzSocketTransform dest, double x, double y, double z, Matrix3f basis) {
        Vector3f ax = basis.getColumn(0, new Vector3f());
        Vector3f ay = basis.getColumn(1, new Vector3f());
        Vector3f az = basis.getColumn(2, new Vector3f());
        dest.set(x, y, z, ax, ay, az);
    }
}
