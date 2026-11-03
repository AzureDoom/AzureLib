
package mod.azure.azurelib.common.compat.bettercombat;

import net.bettercombat.client.collision.OrientedBoundingBox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import mod.azure.azurelib.common.hitbox.AzHitboxHit;
import mod.azure.azurelib.common.hitbox.AzHitboxRole;
import mod.azure.azurelib.common.hitbox.AzHitboxes;
import mod.azure.azurelib.common.hitbox.AzOrientedBox;

public final class AzBetterCombatHitboxHandler {

    private static final double EPSILON = 1.0E-12;

    private AzBetterCombatHitboxHandler() {}

    @Nullable
    public static AzHitboxHit findHit(
        Entity target,
        AzOrientedBox attackVolume
    ) {
        for (AzHitboxHit hit : combatHitboxes(target)) {
            if (attackVolume.intersects(hit.box())) {
                return hit;
            }
        }

        return null;
    }

    @Nullable
    public static AzHitboxHit findHit(
        Entity target,
        OrientedBoundingBox attackVolume
    ) {
        for (AzHitboxHit hit : combatHitboxes(target)) {
            if (intersects(attackVolume, hit.box())) {
                return hit;
            }
        }

        return null;
    }

    private static List<AzHitboxHit> combatHitboxes(
        Entity entity
    ) {
        return AzHitboxes.getActiveHitboxes(
            entity,
            hitbox -> hitbox.role() == AzHitboxRole.COMBAT
        );
    }

    private static boolean intersects(
        OrientedBoundingBox attack,
        AzOrientedBox target
    ) {
        Vec3[] a = attack.vertices;
        Vec3[] b = target.corners();

        if (a == null) {
            return false;
        }

        Vec3[] axesA = {
            attack.axisX,
            attack.axisY,
            attack.axisZ
        };

        Vec3[] axesB = {
            target.axisX(),
            target.axisY(),
            target.axisZ()
        };

        for (Vec3 axis : axesA) {
            if (separated(a, b, axis)) {
                return false;
            }
        }

        for (Vec3 axis : axesB) {
            if (separated(a, b, axis)) {
                return false;
            }
        }

        for (Vec3 axisA : axesA) {
            for (Vec3 axisB : axesB) {
                Vec3 cross = axisA.cross(axisB);

                if (separated(a, b, cross)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean separated(
        Vec3[] a,
        Vec3[] b,
        Vec3 axis
    ) {
        if (axis.lengthSqr() < EPSILON) {
            return false;
        }

        double minA = Double.POSITIVE_INFINITY;
        double maxA = Double.NEGATIVE_INFINITY;

        double minB = Double.POSITIVE_INFINITY;
        double maxB = Double.NEGATIVE_INFINITY;

        for (Vec3 vertex : a) {
            double projection = vertex.dot(axis);

            minA = Math.min(minA, projection);
            maxA = Math.max(maxA, projection);
        }

        for (Vec3 vertex : b) {
            double projection = vertex.dot(axis);

            minB = Math.min(minB, projection);
            maxB = Math.max(maxB, projection);
        }

        return maxA < minB || maxB < minA;
    }
}
