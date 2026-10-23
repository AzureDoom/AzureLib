package mod.azure.azurelib.hitbox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.hitbox.AzHitbox;
import mod.azure.azurelib.hitbox.AzHitboxClientAccess;
import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.hitbox.AzHitboxInteractions;
import mod.azure.azurelib.hitbox.AzHitboxRole;
import mod.azure.azurelib.hitbox.AzHitboxState;
import mod.azure.azurelib.hitbox.AzHitboxSync;
import mod.azure.azurelib.hitbox.AzHitboxes;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.network.packet.AzHitboxAttackPacket;
import mod.azure.azurelib.network.packet.AzHitboxInteractPacket;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.render.entity.AzEntityRenderer;

public final class AzHitboxClientHooks {

    private static final double REPORT_RANGE = 64;

    private static final Set<String> WARNED_MISSING = new HashSet<>();

    private static final Vector3f SCRATCH_CENTER = new Vector3f();

    private static final Vector3f SCRATCH_SCALE = new Vector3f();

    private static final Quaternionf SCRATCH_ROTATION = new Quaternionf();

    private AzHitboxClientHooks() {
        throw new UnsupportedOperationException();
    }

    public static void afterEntityRender(
        Entity entity,
        AzBakedModel model,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        boolean drawDebug
    ) {
        if (!(entity instanceof AzHitboxHolder)) {
            return;
        }

        var state = AzHitboxes.getOrCreateState(entity);

        if (state == null) {
            return;
        }

        capture(entity, model, state, partialTick);
        AzHitboxClientAccess.markRendered(state, entity.level().getGameTime());

        if (drawDebug) {
            renderDebug(entity, state, poseStack, bufferSource);
        }

        report(entity, state);
    }

    public static void afterOffscreenPose(Entity entity, AzBakedModel model, float partialTick) {
        var state = AzHitboxes.getOrCreateState(entity);

        if (state == null) {
            return;
        }

        capture(entity, model, state, partialTick);
        report(entity, state);
    }

    public static void tickOffscreen() {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var player = minecraft.player;

        if (level == null || player == null || minecraft.getConnection() == null) {
            return;
        }

        var now = level.getGameTime();
        var dispatcher = minecraft.getEntityRenderDispatcher();

        for (var entity : level.entitiesForRendering()) {
            if (!(entity instanceof AzHitboxHolder) || entity.isRemoved()) {
                continue;
            }

            if (player.distanceToSqr(entity) > REPORT_RANGE * REPORT_RANGE) {
                continue;
            }

            var state = AzHitboxes.getOrCreateState(entity);

            if (state == null || AzHitboxClientAccess.wasRenderedSince(state, now - 1)) {
                continue;
            }

            if (!AzHitboxSync.isReportDue(state, now)) {
                continue;
            }

            if (dispatcher.getRenderer(entity) instanceof AzEntityRenderer<?> renderer) {
                poseOffscreen(renderer, entity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Entity> void poseOffscreen(AzEntityRenderer<?> renderer, Entity entity) {
        ((AzEntityRenderer<E>) renderer).poseOffscreen((E) entity, 0f);
    }

    private static void capture(Entity entity, AzBakedModel model, AzHitboxState state, float partialTick) {
        var set = state.set();
        var fits = AzHitboxClientAccess.fitCache(state, model.getModelUUID());

        for (int i = 0; i < set.size(); i++) {
            var hitbox = set.get(i);
            var bone = model.getBoneOrNull(hitbox.bone());

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

            var visibility = visibility(bone);

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

            var box = fits[i];

            if (box == null) {
                AzHitboxClientAccess.setInactive(state, i);
                continue;
            }

            var matrix = bone.getLocalSpaceMatrix();
            var center = matrix.transformPosition(box[0], box[1], box[2], SCRATCH_CENTER);
            var scale = matrix.getScale(SCRATCH_SCALE);
            var rotation = matrix.getNormalizedRotation(SCRATCH_ROTATION);

            AzHitboxClientAccess.setBox(
                state,
                i,
                center.x,
                center.y,
                center.z,
                box[3] * Math.abs(scale.x),
                box[4] * Math.abs(scale.y),
                box[5] * Math.abs(scale.z),
                rotation.x,
                rotation.y,
                rotation.z,
                rotation.w
            );
        }

        AzHitboxClientAccess.commit(state, entity, entity.level().getGameTime(), renderYaw(entity, partialTick));
    }

    private static float[] fitBoneSpaceBox(AzBone bone, AzHitbox hitbox) {
        var pivotX = bone.getPivotX() / 16f;
        var pivotY = bone.getPivotY() / 16f;
        var pivotZ = bone.getPivotZ() / 16f;

        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        var point = new Vector3f();

        for (var cube : bone.getCubes()) {
            var transform = cube.transform();

            for (var quad : cube.quads()) {
                if (quad == null) {
                    continue;
                }

                for (var vertex : quad.vertices()) {
                    point.set(vertex.position());

                    if (!transform.identity()) {
                        transform.pose().transformPosition(point);
                    }

                    minX = Math.min(minX, point.x - pivotX);
                    minY = Math.min(minY, point.y - pivotY);
                    minZ = Math.min(minZ, point.z - pivotZ);
                    maxX = Math.max(maxX, point.x - pivotX);
                    maxY = Math.max(maxY, point.y - pivotY);
                    maxZ = Math.max(maxZ, point.z - pivotZ);
                }
            }
        }

        var hasCubes = minX <= maxX;
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

        var inflate = hitbox.inflate() / 16f;
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

        for (var parent = bone.getParent(); parent != null; parent = parent.getParent()) {
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
        if (entity instanceof LivingEntity living) {
            return Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot);
        }

        return entity.getYRot();
    }

    private static void report(Entity entity, AzHitboxState state) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;

        if (player == null || minecraft.getConnection() == null || entity.level() != player.level()) {
            return;
        }

        if (player.distanceToSqr(entity) > REPORT_RANGE * REPORT_RANGE) {
            return;
        }

        if (!AzHitboxSync.shouldReport(state, entity.level().getGameTime())) {
            return;
        }

        var packet = AzHitboxSync.buildReport(entity, state);

        if (packet != null) {
            Services.NETWORK.sendToServer(packet);
        }
    }

    private static void renderDebug(
        Entity entity,
        AzHitboxState state,
        PoseStack poseStack,
        MultiBufferSource bufferSource
    ) {
        var offsetX = -entity.getX();
        var offsetY = -entity.getY();
        var offsetZ = -entity.getZ();
        var buffer = bufferSource.getBuffer(RenderType.lines());
        var pose = poseStack.last();
        var boxes = state.worldBoxes(entity);
        var set = state.set();

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] == null) {
                continue;
            }

            var hitbox = set.get(i);
            var multiplier = hitbox.damageMultiplier();
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

            var corners = boxes[i].corners();

            for (int a = 0; a < 8; a++) {
                for (int bit = 1; bit < 8; bit <<= 1) {
                    if ((a & bit) == 0) {
                        drawEdge(buffer, pose, corners[a], corners[a | bit], offsetX, offsetY, offsetZ, r, g, b);
                    }
                }
            }
        }
    }

    private static void drawEdge(
        VertexConsumer buffer,
        PoseStack.Pose pose,
        Vec3 from,
        Vec3 to,
        double offsetX,
        double offsetY,
        double offsetZ,
        float r,
        float g,
        float b
    ) {
        var dx = (float) (to.x - from.x);
        var dy = (float) (to.y - from.y);
        var dz = (float) (to.z - from.z);
        var length = Mth.sqrt(dx * dx + dy * dy + dz * dz);

        if (length < 1.0E-6f) {
            return;
        }

        dx /= length;
        dy /= length;
        dz /= length;

        buffer.vertex(
            pose.pose(),
            (float) (from.x + offsetX),
            (float) (from.y + offsetY),
            (float) (from.z + offsetZ)
        )
            .color(r, g, b, 1f)
            .normal(pose.normal(), dx, dy, dz)
            .endVertex();
        buffer.vertex(
            pose.pose(),
            (float) (to.x + offsetX),
            (float) (to.y + offsetY),
            (float) (to.z + offsetZ)
        )
            .color(r, g, b, 1f)
            .normal(pose.normal(), dx, dy, dz)
            .endVertex();
    }

    public static void onAttack(Player player, Entity target) {
        if (!(target instanceof AzHitboxHolder) || Minecraft.getInstance().getConnection() == null) {
            return;
        }

        var eye = player.getEyePosition();
        var reach = Services.PLATFORM.getEntityInteractionRange(player);
        var end = eye.add(player.getViewVector(1f).scale(reach));

        AzHitboxes.raycast(target, eye, end, AzHitbox::isPickable)
            .filter(hit -> hit.hitbox().role() == AzHitboxRole.COMBAT)
            .ifPresent(hit -> Services.NETWORK.sendToServer(new AzHitboxAttackPacket(target.getId(), hit.index())));
    }

    public static void onInteract(Player player, Entity target) {
        if (!(target instanceof AzHitboxHolder) || Minecraft.getInstance().getConnection() == null) {
            return;
        }

        var hit = AzHitboxInteractions.findLookedAt(player, target);

        if (hit != null) {
            Services.NETWORK.sendToServer(new AzHitboxInteractPacket(target.getId(), hit.index()));
        }
    }

    private static void warnMissing(Entity entity, AzHitbox hitbox) {
        var key = entity.getType() + "/" + hitbox.bone();

        if (WARNED_MISSING.add(key)) {
            AzureLib.LOGGER.warn(
                "Hitbox '{}' on {} references bone '{}', which is not in the model",
                hitbox.name(),
                entity.getType(),
                hitbox.bone()
            );
        }
    }
}
