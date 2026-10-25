package mod.azure.azurelib.hitbox.client;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.hitbox.*;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.network.packet.AzHitboxAttackPacket;
import mod.azure.azurelib.network.packet.AzHitboxInteractPacket;
import mod.azure.azurelib.network.packet.AzHitboxPosePacket;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.util.math.Matrix4f;
import mod.azure.azurelib.util.math.Mth;
import mod.azure.azurelib.util.math.Vector4f;

public final class AzHitboxClientHooks {

    private static final double REPORT_RANGE = 64;

    private static final Set<String> WARNED_MISSING = new HashSet<>();

    /**
     * Beta single player has no integrated server: the client world is the authoritative world and holds the same
     * entity objects the renderer captures into. Poses never need reporting there and claims are handled in place.
     */
    private static boolean isMultiplayer(World world) {
        return world != null && Mc.isClientSide(world);
    }

    private AzHitboxClientHooks() {
        throw new UnsupportedOperationException();
    }

    public static void afterEntityRender(Entity entity, AzBakedModel model, float partialTick, boolean drawDebug) {
        if (!(entity instanceof AzHitboxHolder)) {
            return;
        }

        AzHitboxState state = AzHitboxes.getOrCreateState(entity);

        if (state == null) {
            return;
        }

        capture(entity, model, state, partialTick);
        AzHitboxClientAccess.markRendered(state, Mc.time(Mc.world(entity)));

        if (drawDebug) {
            renderDebug(entity, state);
        }

        report(entity, state);
    }

    public static void afterOffscreenPose(Entity entity, AzBakedModel model, float partialTick) {
        AzHitboxState state = AzHitboxes.getOrCreateState(entity);

        if (state == null) {
            return;
        }

        capture(entity, model, state, partialTick);
        report(entity, state);
    }

    /**
     * Poses hitbox entities near the player that were not rendered this tick (behind the camera, culled), so their
     * hitboxes stay live. In single player this keeps the shared state fresh for projectiles; in multiplayer it also
     * feeds pose reports to the server.
     */
    public static void tickOffscreen() {
        World level = McClient.world();
        PlayerEntity player = McClient.player();

        if (level == null || player == null || McClient.isPaused()) {
            return;
        }

        long now = Mc.time(level);

        for (Entity entity : new ArrayList<>(Mc.entities(level))) {
            if (!(entity instanceof AzHitboxHolder) || !Mc.isAlive(entity)) {
                continue;
            }

            if (Mc.distanceSq(player, entity) > REPORT_RANGE * REPORT_RANGE) {
                continue;
            }

            AzHitboxState state = AzHitboxes.getOrCreateState(entity);

            if (state == null || AzHitboxClientAccess.wasRenderedSince(state, now - 1)) {
                continue;
            }

            if (!AzHitboxSync.isReportDue(state, now)) {
                continue;
            }

            EntityRenderer renderer = McClient.entityRenderer(entity);

            if (renderer instanceof AzEntityRenderer) {
                poseOffscreen((AzEntityRenderer<?>) renderer, entity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Entity> void poseOffscreen(AzEntityRenderer<?> renderer, Entity entity) {
        ((AzEntityRenderer<E>) renderer).poseOffscreen((E) entity, 0f);
    }

    private static void capture(Entity entity, AzBakedModel model, AzHitboxState state, float partialTick) {
        AzHitboxSet set = state.set();
        float[][] fits = AzHitboxClientAccess.fitCache(state, model.getModelUUID());

        for (int i = 0; i < set.size(); i++) {
            AzHitbox hitbox = set.get(i);
            AzBone bone = model.getBoneOrNull(hitbox.bone());

            if (bone == null) {
                warnMissing(entity, hitbox);
                AzHitboxClientAccess.setInactive(state, i);
                continue;
            }

            if (!bone.isTrackingMatrices()) {
                bone.setTrackingMatrices(true);
                AzHitboxClientAccess.setInactive(state, i);
                continue;
            }

            Visibility visibility = visibility(bone);

            if (visibility == Visibility.HIDDEN) {
                AzHitboxClientAccess.setInactive(state, i);
                continue;
            }

            if (visibility == Visibility.STALE) {
                continue;
            }

            if (fits[i] == null) {
                fits[i] = fitBoneSpaceBox(bone, hitbox);
            }

            float[] box = fits[i];

            if (box == null) {
                AzHitboxClientAccess.setInactive(state, i);
                continue;
            }

            Matrix4f matrix = bone.getLocalSpaceMatrix();
            Vector4f center = transform(matrix, box[0], box[1], box[2], 1f);
            Vector4f axisX = transform(matrix, 1, 0, 0, 0f);
            Vector4f axisY = transform(matrix, 0, 1, 0, 0f);
            Vector4f axisZ = transform(matrix, 0, 0, 1, 0f);
            float scaleX = length(axisX);
            float scaleY = length(axisY);
            float scaleZ = length(axisZ);
            float[] rotation = rotationOf(axisX, scaleX, axisY, scaleY, axisZ, scaleZ);

            AzHitboxClientAccess.setBox(
                state,
                i,
                center.x(),
                center.y(),
                center.z(),
                box[3] * scaleX,
                box[4] * scaleY,
                box[5] * scaleZ,
                rotation[0],
                rotation[1],
                rotation[2],
                rotation[3]
            );
        }

        AzHitboxClientAccess.commit(state, entity, Mc.time(Mc.world(entity)), renderYaw(entity, partialTick));
    }

    private static Vector4f transform(Matrix4f matrix, float x, float y, float z, float w) {
        Vector4f vector = new Vector4f(x, y, z, w);
        vector.transform(matrix);
        return vector;
    }

    private static float length(Vector4f vector) {
        return (float) Math.sqrt(vector.x() * vector.x() + vector.y() * vector.y() + vector.z() * vector.z());
    }

    private static float[] rotationOf(
        Vector4f axisX,
        float scaleX,
        Vector4f axisY,
        float scaleY,
        Vector4f axisZ,
        float scaleZ
    ) {
        if (scaleX < 1.0E-6f || scaleY < 1.0E-6f || scaleZ < 1.0E-6f) {
            return new float[] { 0, 0, 0, 1 };
        }

        float m00 = axisX.x() / scaleX, m10 = axisX.y() / scaleX, m20 = axisX.z() / scaleX;
        float m01 = axisY.x() / scaleY, m11 = axisY.y() / scaleY, m21 = axisY.z() / scaleY;
        float m02 = axisZ.x() / scaleZ, m12 = axisZ.y() / scaleZ, m22 = axisZ.z() / scaleZ;
        float trace = m00 + m11 + m22;
        float x, y, z, w;

        if (trace > 0) {
            float s = (float) Math.sqrt(trace + 1f) * 2f;
            w = 0.25f * s;
            x = (m21 - m12) / s;
            y = (m02 - m20) / s;
            z = (m10 - m01) / s;
        } else if (m00 > m11 && m00 > m22) {
            float s = (float) Math.sqrt(1f + m00 - m11 - m22) * 2f;
            w = (m21 - m12) / s;
            x = 0.25f * s;
            y = (m01 + m10) / s;
            z = (m02 + m20) / s;
        } else if (m11 > m22) {
            float s = (float) Math.sqrt(1f + m11 - m00 - m22) * 2f;
            w = (m02 - m20) / s;
            x = (m01 + m10) / s;
            y = 0.25f * s;
            z = (m12 + m21) / s;
        } else {
            float s = (float) Math.sqrt(1f + m22 - m00 - m11) * 2f;
            w = (m10 - m01) / s;
            x = (m02 + m20) / s;
            y = (m12 + m21) / s;
            z = 0.25f * s;
        }

        return new float[] { x, y, z, w };
    }

    private static float[] fitBoneSpaceBox(AzBone bone, AzHitbox hitbox) {
        float pivotX = bone.getPivotX() / 16f;
        float pivotY = bone.getPivotY() / 16f;
        float pivotZ = bone.getPivotZ() / 16f;

        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

        for (GeoCube cube : bone.getCubes()) {
            GeoCube.Transform transform = cube.transform();

            for (GeoQuad quad : cube.quads()) {
                if (quad == null) {
                    continue;
                }

                for (GeoVertex vertex : quad.vertices()) {
                    Vector4f point = new Vector4f(
                        vertex.position().x(),
                        vertex.position().y(),
                        vertex.position().z(),
                        1f
                    );

                    if (!transform.identity()) {
                        point.transform(transform.pose());
                    }

                    minX = Math.min(minX, point.x() - pivotX);
                    minY = Math.min(minY, point.y() - pivotY);
                    minZ = Math.min(minZ, point.z() - pivotZ);
                    maxX = Math.max(maxX, point.x() - pivotX);
                    maxY = Math.max(maxY, point.y() - pivotY);
                    maxZ = Math.max(maxZ, point.z() - pivotZ);
                }
            }
        }

        boolean hasCubes = minX <= maxX;
        float centerX = 0, centerY = 0, centerZ = 0;
        float halfX, halfY, halfZ;

        if (hasCubes && !hitbox.anchorAtPivot()) {
            centerX = (minX + maxX) * 0.5f;
            centerY = (minY + maxY) * 0.5f;
            centerZ = (minZ + maxZ) * 0.5f;
        }

        if (hitbox.fitsCubes()) {
            if (!hasCubes) {
                AzureLib.LOGGER.warn(
                    "Hitbox '{}' fits bone '{}', which has no cubes; give it an explicit size",
                    hitbox.name(),
                    hitbox.bone()
                );
                return null;
            }

            halfX = (maxX - minX) * 0.5f;
            halfY = (maxY - minY) * 0.5f;
            halfZ = (maxZ - minZ) * 0.5f;
        } else {
            halfX = hitbox.sizeX() / 32f;
            halfY = hitbox.sizeY() / 32f;
            halfZ = hitbox.sizeZ() / 32f;
        }

        float inflate = hitbox.inflate() / 16f;
        halfX = Math.max(0, halfX + inflate);
        halfY = Math.max(0, halfY + inflate);
        halfZ = Math.max(0, halfZ + inflate);

        if (halfX == 0 && halfY == 0 && halfZ == 0) {
            return null;
        }

        return new float[] {
            centerX - hitbox.offsetX() / 16f,
            centerY + hitbox.offsetY() / 16f,
            centerZ + hitbox.offsetZ() / 16f,
            halfX,
            halfY,
            halfZ
        };
    }

    private enum Visibility {
        VISIBLE,
        HIDDEN,
        STALE
    }

    private static Visibility visibility(AzBone bone) {
        if (bone.isHidden()) {
            return bone.getLodHidden() ? Visibility.STALE : Visibility.HIDDEN;
        }

        if (isZeroScale(bone)) {
            return Visibility.HIDDEN;
        }

        for (AzBone parent = bone.getParent(); parent != null; parent = parent.getParent()) {
            if ((parent.isHidden() && parent.isHidingChildren()) || isZeroScale(parent)) {
                return parent.getLodHidden() ? Visibility.STALE : Visibility.HIDDEN;
            }
        }

        return Visibility.VISIBLE;
    }

    private static boolean isZeroScale(AzBone bone) {
        return bone.getScaleX() == 0f && bone.getScaleY() == 0f && bone.getScaleZ() == 0f;
    }

    private static float renderYaw(Entity entity, float partialTick) {
        if (entity instanceof MobEntity) {
            MobEntity living = (MobEntity) entity;
            return Mth.rotLerp(partialTick, Mc.prevBodyYaw(living), Mc.bodyYaw(living));
        }

        return Mc.yaw(entity);
    }

    private static void report(Entity entity, AzHitboxState state) {
        PlayerEntity player = McClient.player();

        if (player == null || Mc.world(entity) != Mc.world(player)) {
            return;
        }

        if (Mc.distanceSq(player, entity) > REPORT_RANGE * REPORT_RANGE) {
            return;
        }

        // Also runs in single player so the report interval keeps throttling offscreen posing.
        if (!AzHitboxSync.shouldReport(state, Mc.time(Mc.world(entity))) || !isMultiplayer(Mc.world(entity))) {
            return;
        }

        AzHitboxPosePacket packet = AzHitboxSync.buildReport(entity, state);

        if (packet != null) {
            Services.NETWORK.sendToServer(packet);
        }
    }

    /**
     * Draws the hitboxes as lines. The GL matrix is translated to the entity, so world corners are offset by its
     * position. Uses immediate-mode GL rather than Beta's {@code Tessellator}, like {@code AzBufferSource}.
     */
    private static void renderDebug(Entity entity, AzHitboxState state) {
        AzOrientedBox[] boxes = state.worldBoxes(entity);
        AzHitboxSet set = state.set();
        double offsetX = -Mc.x(entity);
        double offsetY = -Mc.y(entity);
        double offsetZ = -Mc.z(entity);

        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glBegin(GL11.GL_LINES);

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] == null) {
                continue;
            }

            AzHitbox hitbox = set.get(i);
            float multiplier = hitbox.damageMultiplier();
            float r = 0.2f, g = 1f, b = 0.2f;

            if (hitbox.role() == AzHitboxRole.INTERACT) {
                r = 0.2f;
                g = 0.9f;
                b = 0.9f;
            } else if (hitbox.role() == AzHitboxRole.ANCHOR) {
                r = 1f;
                g = 0.85f;
                b = 0.1f;
            } else if (multiplier > 1) {
                r = 1f;
                g = 0.25f;
                b = 0.25f;
            } else if (multiplier < 1) {
                r = 0.3f;
                g = 0.5f;
                b = 1f;
            }

            GL11.glColor4f(r, g, b, 1f);
            Vec3d[] corners = boxes[i].corners();

            for (int a = 0; a < 8; a++) {
                for (int bit = 1; bit < 8; bit <<= 1) {
                    if ((a & bit) == 0) {
                        Vec3d from = corners[a];
                        Vec3d to = corners[a | bit];
                        GL11.glVertex3d(from.x + offsetX, from.y + offsetY, from.z + offsetZ);
                        GL11.glVertex3d(to.x + offsetX, to.y + offsetY, to.z + offsetZ);
                    }
                }
            }
        }

        GL11.glEnd();
        GL11.glColor4f(1f, 1f, 1f, 1f);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
    }

    /**
     * Runs on the client before the attack is sent (multiplayer) or performed (single player), so the server has the
     * claim by the time the vanilla attack arrives.
     */
    public static void onAttack(PlayerEntity player, Entity target) {
        if (!(target instanceof AzHitboxHolder)) {
            return;
        }

        Vec3d eye = AzHitboxMath.eyePosition(player);
        Vec3d end = AzHitboxMath.along(eye, AzHitboxMath.look(player), AzHitboxes.attackReach(player));
        AzHitboxHit hit = AzHitboxes.raycast(target, eye, end, AzHitbox::isPickable).orElse(null);

        if (hit != null && hit.hitbox().role() == AzHitboxRole.COMBAT) {
            Services.NETWORK.sendToServer(new AzHitboxAttackPacket(Mc.entityId(target), hit.index()));
        }
    }

    public static void onInteract(PlayerEntity player, Entity target) {
        if (!(target instanceof AzHitboxHolder)) {
            return;
        }

        AzHitboxHit hit = AzHitboxInteractions.findLookedAt(
            player,
            target,
            AzHitboxMath.eyePosition(player),
            AzHitboxMath.look(player)
        );

        if (hit != null) {
            Services.NETWORK.sendToServer(new AzHitboxInteractPacket(Mc.entityId(target), hit.index()));
        }
    }

    private static void warnMissing(Entity entity, AzHitbox hitbox) {
        String key = entity.getClass().getName() + "/" + hitbox.bone();

        if (WARNED_MISSING.add(key)) {
            AzureLib.LOGGER.warn(
                "Hitbox '{}' on {} references bone '{}', which is not in the model",
                hitbox.name(),
                entity.getClass().getSimpleName(),
                hitbox.bone()
            );
        }
    }
}
