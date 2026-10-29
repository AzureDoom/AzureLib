package mod.azure.azurelib.socket;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import mod.azure.azurelib.hitbox.AzHitboxes;
import mod.azure.azurelib.hitbox.AzOrientedBox;
import mod.azure.azurelib.util.math.Mth;

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

    private AzBoneSockets() {}

    /**
     * Resolves {@code socket} on {@code entity}.
     *
     * @param partialTick the frame's partial tick when called while rendering, otherwise 1
     * @return whether {@code dest} was filled; {@code false} if the socket can't be resolved yet
     */
    public static boolean resolve(Entity entity, AzBoneSocket socket, float partialTick, AzSocketTransform dest) {
        if (entity.worldObj.isRemote && AzBoneSocketsClient.resolve(entity, socket, partialTick, dest)) {
            return true;
        }

        return resolveThroughHitbox(entity, socket, dest);
    }

    /** The socket's world position, or {@code null} if it can't be resolved yet. */
    public static Vec3 position(Entity entity, AzBoneSocket socket, float partialTick) {
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

        Vec3 u = box.axisX();
        Vec3 v = box.axisY();
        Vec3 w = box.axisZ();
        float[] basis = {
            (float) u.xCoord,
            (float) u.yCoord,
            (float) u.zCoord,
            (float) v.xCoord,
            (float) v.yCoord,
            (float) v.zCoord,
            (float) w.xCoord,
            (float) w.yCoord,
            (float) w.zCoord
        };
        rotateBasis(basis, socket.rotationX(), socket.rotationY(), socket.rotationZ());
        dest.set(box.centerX(), box.centerY(), box.centerZ(), basis);
        return true;
    }

    /**
     * Turns a basis (three axis columns, {@code [x0, x1, x2, y0, y1, y2, z0, z1, z2]}) by a Blockbench rotation in
     * degrees, applied like a bone rotation: {@code basis = basis * Rz * Ry * Rx}, with X and Y negated as AzureLib
     * negates them at load.
     */
    static void rotateBasis(float[] basis, float degX, float degY, float degZ) {
        if (degX == 0 && degY == 0 && degZ == 0) {
            return;
        }

        float x = -degX * Mth.DEG_TO_RAD;
        float y = -degY * Mth.DEG_TO_RAD;
        float z = degZ * Mth.DEG_TO_RAD;
        float cx = (float) Math.cos(x), sx = (float) Math.sin(x);
        float cy = (float) Math.cos(y), sy = (float) Math.sin(y);
        float cz = (float) Math.cos(z), sz = (float) Math.sin(z);

        // Rz * Ry * Rx, row-major.
        float r00 = cz * cy, r01 = cz * sy * sx - sz * cx, r02 = cz * sy * cx + sz * sx;
        float r10 = sz * cy, r11 = sz * sy * sx + cz * cx, r12 = sz * sy * cx - cz * sx;
        float r20 = -sy, r21 = cy * sx, r22 = cy * cx;

        // New column j = sum over k of (old column k) * R[k][j].
        float[] old = basis.clone();

        for (int row = 0; row < 3; row++) {
            float c0 = old[row], c1 = old[3 + row], c2 = old[6 + row];
            basis[row] = c0 * r00 + c1 * r10 + c2 * r20;
            basis[3 + row] = c0 * r01 + c1 * r11 + c2 * r21;
            basis[6 + row] = c0 * r02 + c1 * r12 + c2 * r22;
        }
    }
}
