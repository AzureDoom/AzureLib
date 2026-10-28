package mod.azure.azurelib.animation.molang;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.core.molang.MolangArray;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.MolangScope;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.item.AzItemDisplayContext;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.math.Mth;

/**
 * AzureLib's {@link MolangQueryContext}: answers query functions for whatever animatable is currently being animated.
 * <p>
 * There is one shared instance, re-bound by each animator right before it evaluates its animations (see
 * {@link AzAnimator#applyMolangQueries}), so binding allocates nothing. Render thread only. Minecraft is reached
 * through {@link Mc} and {@link McClient}, so this class has no mapping-specific names.
 * </p>
 * <p>
 * It also carries the rest of the evaluation context Molang needs:
 * </p>
 * <ul>
 * <li>The arrow operator ({@code t.pig->q.health}) pushes another entity: queries then read that entity, and
 * {@code variable.*} reads that entity's own Molang scope if it has an AzureLib animator.</li>
 * <li>{@code this} is the value the bone channel being sampled currently holds, in keyframe units (degrees offset from
 * the bind rotation, position offset, scale). That is the pose the bone ended the previous frame on, so
 * {@code math.lerp(this, 45, 0.2)} eases toward 45 over successive frames. Outside keyframe sampling it is 0.</li>
 * <li>While an item renders, the entity holding it and its display context. Item animations then answer entity queries
 * for the holder, as Bedrock attachables do, and {@code query.is_first_person} reflects the item's own view.</li>
 * </ul>
 */
public final class AzMolangQueryContext implements MolangQueryContext {

    public static final AzMolangQueryContext INSTANCE = new AzMolangQueryContext();

    /** Upper bound on {@code query.get_nearby_entities} radius, to keep a typo from scanning half the world. */
    public static final double MAX_NEARBY_RADIUS = 64;

    private static final AzEquipmentSlot[] ARMOR_SLOTS = {
        AzEquipmentSlot.HEAD,
        AzEquipmentSlot.CHEST,
        AzEquipmentSlot.LEGS,
        AzEquipmentSlot.FEET
    };

    private static final int ROTATION = 0;

    private static final int POSITION = 1;

    private static final int SCALE = 2;

    /** Used for {@code variable.*} when the arrow targets an entity without an AzureLib animator. */
    private static final MolangScope DETACHED_SCOPE = new MolangScope();

    private Entity entity;

    private MobEntity livingEntity;

    private float partialTicks;

    private Entity[] pushedEntities = new Entity[4];

    private MolangScope[] pushedScopes = new MolangScope[4];

    private int pushDepth;

    private Entity cachedTarget;

    private boolean targetResolved;

    private AzBone channelBone;

    private int channelTransform;

    private int channelAxis;

    private MobEntity[] itemHolders = new MobEntity[4];

    private int itemHolderDepth;

    private AzItemDisplayContext itemDisplayContext;

    private AzMolangQueryContext() {}

    /**
     * Makes this the active query context for the animatable about to be animated, and re-binds the entity queries so
     * none of them keeps a value memoized for a previous animatable.
     *
     * @param entity       the entity being animated, or {@code null} for items, blocks and other non-entities
     * @param partialTicks the current partial tick
     */
    public void bind(Entity entity, float partialTicks) {
        setEntity(entity);
        this.partialTicks = partialTicks;
        this.pushDepth = 0;
        MolangQueryContext.setCurrent(this);
        AzEntityMolangQueries.bind();
    }

    private void setEntity(Entity entity) {
        this.entity = entity;
        this.livingEntity = entity instanceof MobEntity ? (MobEntity) entity : null;
        this.cachedTarget = null;
        this.targetResolved = false;
    }

    /**
     * @return the entity being animated (or the arrow operator's entity), or {@code null} if the animatable isn't an
     *         entity
     */
    public Entity entity() {
        return entity;
    }

    /**
     * @return {@link #entity()} if it is a {@link MobEntity} (Feather's name for every living entity, players
     *         included), else {@code null}
     */
    public MobEntity livingEntity() {
        return livingEntity;
    }

    public float partialTicks() {
        return partialTicks;
    }

    // Arrow operator

    @Override
    public boolean isEntity(Object object) {
        return object instanceof Entity && !Mc.isRemoved((Entity) object);
    }

    @Override
    public boolean pushEntity(Object object) {
        if (!isEntity(object)) {
            return false;
        }

        Entity target = (Entity) object;

        if (pushDepth == pushedEntities.length) {
            pushedEntities = Arrays.copyOf(pushedEntities, pushDepth * 2);
            pushedScopes = Arrays.copyOf(pushedScopes, pushDepth * 2);
        }

        pushedEntities[pushDepth] = entity;
        pushedScopes[pushDepth] = MolangScope.setCurrent(scopeOf(target));
        pushDepth++;

        setEntity(target);
        AzEntityMolangQueries.bind();
        return true;
    }

    @Override
    public void popEntity() {
        if (pushDepth == 0) {
            return;
        }

        pushDepth--;
        Entity previous = pushedEntities[pushDepth];
        MolangScope.setCurrent(pushedScopes[pushDepth]);
        pushedEntities[pushDepth] = null;
        pushedScopes[pushDepth] = null;

        setEntity(previous);
        AzEntityMolangQueries.bind();
    }

    private static MolangScope scopeOf(Entity target) {
        AzAnimator<UUID, Entity> animator = AzAnimatorAccessor.getOrNull(target);
        // Entity animators are keyed by Mc#entityUuid, the same key AzEntityRenderer gives them.
        AzAnimationContext<Entity> context = animator == null ? null : animator.findContext(Mc.entityUuid(target));

        if (context != null) {
            return context.molangScope();
        }

        DETACHED_SCOPE.clear();
        return DETACHED_SCOPE;
    }

    // this

    /**
     * Supplies {@code this} for the keyframe values about to be read: the given bone channel's current value.
     *
     * @param transform 0 = rotation, 1 = position, 2 = scale
     * @param axis      0 = x, 1 = y, 2 = z
     */
    public void bindChannel(AzBone bone, int transform, int axis) {
        this.channelBone = bone;
        this.channelTransform = transform;
        this.channelAxis = axis;
    }

    public void clearChannel() {
        this.channelBone = null;
    }

    @Override
    public double thisValue() {
        AzBone bone = channelBone;

        if (bone == null) {
            return 0;
        }

        switch (channelTransform) {
            case ROTATION: {
                AzBoneSnapshot initial = bone.getInitialAzSnapshot();
                float radians;

                switch (channelAxis) {
                    case 0:
                        radians = bone.getRotX() - initial.getRotX();
                        break;
                    case 1:
                        radians = bone.getRotY() - initial.getRotY();
                        break;
                    default:
                        radians = bone.getRotZ() - initial.getRotZ();
                        break;
                }

                double degrees = Math.toDegrees(radians);
                // Keyframe X and Y rotations are flipped when baked; undo that so this matches what the file wrote.
                return channelAxis == 2 ? degrees : -degrees;
            }
            case POSITION:
                return axis(bone.getPosX(), bone.getPosY(), bone.getPosZ(), channelAxis);
            case SCALE:
                return axis(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ(), channelAxis);
            default:
                return 0;
        }
    }

    // Item rendering

    /**
     * Records the entity holding the item about to render; matched by {@link #popItemHolder()}. Called from
     * {@code AzItemRendererAdapter#renderItem}, which the held-item mixins pass the holder to.
     */
    public void pushItemHolder(MobEntity holder) {
        if (itemHolderDepth == itemHolders.length) {
            itemHolders = Arrays.copyOf(itemHolders, itemHolderDepth * 2);
        }

        itemHolders[itemHolderDepth++] = holder;
    }

    public void popItemHolder() {
        if (itemHolderDepth > 0) {
            itemHolders[--itemHolderDepth] = null;
        }
    }

    /**
     * @return the entity holding the item being rendered, or {@code null}. In first person this falls back to the
     *         camera entity.
     */
    public MobEntity itemHolder() {
        MobEntity holder = itemHolderDepth > 0 ? itemHolders[itemHolderDepth - 1] : null;

        if (holder == null && itemDisplayContext != null && itemDisplayContext.isFirstPerson()) {
            Entity camera = McClient.cameraEntity();
            return camera instanceof MobEntity ? (MobEntity) camera : null;
        }

        return holder;
    }

    /**
     * Sets the display context of the item being rendered, or {@code null} once it is done.
     *
     * @return the previous value, to hand back once rendering finishes
     */
    public AzItemDisplayContext setItemDisplayContext(AzItemDisplayContext context) {
        AzItemDisplayContext previous = this.itemDisplayContext;
        this.itemDisplayContext = context;
        return previous;
    }

    public AzItemDisplayContext itemDisplayContext() {
        return itemDisplayContext;
    }

    // Targets

    /**
     * The current entity's attack target: {@link AzMolangTargetProvider#getMolangTarget()} if it implements it,
     * otherwise {@link Mc#attackTarget}. Resolved once per binding.
     */
    public Entity target() {
        if (!targetResolved) {
            targetResolved = true;
            cachedTarget = resolveTarget(entity);
        }

        return cachedTarget;
    }

    private static Entity resolveTarget(Entity entity) {
        if (entity == null) {
            return null;
        }

        Entity target = entity instanceof AzMolangTargetProvider
            ? ((AzMolangTargetProvider) entity).getMolangTarget()
            : Mc.attackTarget(entity);

        return target == null || Mc.isRemoved(target) || target == entity ? null : target;
    }

    /**
     * Pitch, in degrees, the current entity would need to look at its target's eyes (negative is up), or 0 without a
     * target. Same convention as {@code query.head_x_rotation}.
     */
    public double targetXRotation() {
        Entity target = target();

        if (entity == null || target == null) {
            return 0;
        }

        double dx = lerpX(target) - lerpX(entity);
        double dy = eyeY(target) - eyeY(entity);
        double dz = lerpZ(target) - lerpZ(entity);
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        return Mth.wrapDegrees(-(Math.atan2(dy, horizontal) * Mth.RAD_TO_DEG));
    }

    /**
     * World yaw, in degrees, the current entity would need to face its target, or 0 without a target. Same convention
     * as {@code query.body_y_rotation}; subtract that (with {@code math.min_angle}) for a body-relative angle.
     */
    public double targetYRotation() {
        Entity target = target();

        if (entity == null || target == null) {
            return 0;
        }

        return Mth.wrapDegrees(
            Math.atan2(lerpZ(target) - lerpZ(entity), lerpX(target) - lerpX(entity)) * Mth.RAD_TO_DEG - 90
        );
    }

    double lerpX(Entity entity) {
        return Mth.lerp(partialTicks, Mc.prevX(entity), Mc.x(entity));
    }

    double lerpY(Entity entity) {
        return Mth.lerp(partialTicks, Mc.prevY(entity), Mc.y(entity));
    }

    double lerpZ(Entity entity) {
        return Mth.lerp(partialTicks, Mc.prevZ(entity), Mc.z(entity));
    }

    /** Eye height the way Beta's own look code computes it: {@code y + getEyeHeight()}. */
    private double eyeY(Entity entity) {
        return lerpY(entity) + Mc.eyeHeight(entity);
    }

    // Collections

    /**
     * Entities within {@code radius} blocks of the current entity, nearest first, as a Molang array.
     * <p>
     * Walks the world's entity list rather than a bounding-box query: Beta worlds hold few entities, and it keeps this
     * on {@link Mc}'s existing accessors.
     * </p>
     *
     * @param filter      an entity id such as {@code minecraft:pig}, or {@code null} for all. Beta names entities
     *                    differently ({@code Pig}), so see {@link #matchesEntityId}
     * @param includeSelf whether the current entity itself may be included
     */
    public MolangArray nearbyEntities(double radius, String filter, boolean includeSelf) {
        Entity center = entity;

        if (center == null || !(radius > 0)) {
            return MolangArray.EMPTY;
        }

        World world = Mc.world(center);

        if (world == null) {
            return MolangArray.EMPTY;
        }

        double clamped = Math.min(radius, MAX_NEARBY_RADIUS);
        double radiusSqr = clamped * clamped;
        String wanted = filter == null || filter.isEmpty() ? null : normalizeEntityId(filter);
        double cx = Mc.x(center);
        double cy = Mc.y(center);
        double cz = Mc.z(center);
        List<Entity> found = null;

        for (Entity candidate : Mc.entities(world)) {
            if (
                candidate == null
                    || (!includeSelf && candidate == center)
                    || !Mc.isAlive(candidate)
                    || distanceSqr(candidate, cx, cy, cz) > radiusSqr
                    || (wanted != null && !matchesEntityId(candidate, wanted))
            ) {
                continue;
            }

            if (found == null) {
                found = new ArrayList<>();
            }

            found.add(candidate);
        }

        if (found == null) {
            return MolangArray.EMPTY;
        }

        found.sort(Comparator.comparingDouble(candidate -> distanceSqr(candidate, cx, cy, cz)));
        return MolangArray.of(found);
    }

    private static double distanceSqr(Entity entity, double x, double y, double z) {
        double dx = Mc.x(entity) - x;
        double dy = Mc.y(entity) - y;
        double dz = Mc.z(entity) - z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Whether {@code entity} has the given id. Beta registers entities by name rather than namespaced id, so both sides
     * are compared case-insensitively with the {@code minecraft:} namespace dropped and any other namespace written as
     * {@code modid.}: {@code 'minecraft:pig'} matches {@code Pig}. Names that differ from Bedrock's ({@code PigZombie})
     * must be written the Beta way. Players match {@code 'minecraft:player'}.
     *
     * @param normalizedId an id passed through {@link #normalizeEntityId}
     */
    public static boolean matchesEntityId(Entity entity, String normalizedId) {
        String name = Mc.entityTypeName(entity);
        return name != null && normalizeEntityId(name).equals(normalizedId);
    }

    public static String normalizeEntityId(String id) {
        String lower = id.toLowerCase(Locale.ROOT);

        if (lower.startsWith("minecraft:")) {
            return lower.substring("minecraft:".length());
        }

        return lower.replace(':', '.');
    }

    // Query functions

    @Override
    public double position(int axis) {
        if (entity == null)
            return 0;

        return axis(lerpX(entity), lerpY(entity), lerpZ(entity), axis);
    }

    @Override
    public double positionDelta(int axis) {
        if (entity == null)
            return 0;

        return axis(Mc.velocityX(entity), Mc.velocityY(entity), Mc.velocityZ(entity), axis);
    }

    @Override
    public double movementDirection(int axis) {
        if (entity == null)
            return 0;

        double vx = Mc.velocityX(entity);
        double vy = Mc.velocityY(entity);
        double vz = Mc.velocityZ(entity);
        double length = Math.sqrt(vx * vx + vy * vy + vz * vz);

        return length < 1.0E-7 ? 0 : axis(vx, vy, vz, axis) / length;
    }

    @Override
    public double hasArmorSlot(int slot) {
        return armorStack(slot) == null ? 0 : 1;
    }

    @Override
    public double armorDamageSlot(int slot) {
        ItemStack stack = armorStack(slot);
        return stack == null ? 0 : Mc.damage(stack);
    }

    private ItemStack armorStack(int slot) {
        if (livingEntity == null || slot < 0 || slot >= ARMOR_SLOTS.length)
            return null;

        return ARMOR_SLOTS[slot].getStack(livingEntity);
    }

    /**
     * Beta has no offhand, so hand 1 is always empty.
     */
    @Override
    public double isItemEquipped(int hand) {
        if (livingEntity == null || hand == 1)
            return 0;

        return AzEquipmentSlot.MAINHAND.getStack(livingEntity) == null ? 0 : 1;
    }

    /**
     * Beta has no height maps worth distinguishing, so both height queries give the block above the highest non-air
     * block.
     */
    @Override
    public double heightmap(double x, double z) {
        World world = McClient.world();
        return world == null ? 0 : Mc.topBlockY(world, Mth.floor(x), Mth.floor(z));
    }

    @Override
    public double aboveTopSolid(double x, double z) {
        return heightmap(x, z);
    }

    @Override
    public double distanceFromCamera() {
        if (entity == null)
            return 0;

        return Math.sqrt(McClient.distanceToCameraSq(lerpX(entity), lerpY(entity), lerpZ(entity)));
    }

    private static double axis(double x, double y, double z, int axis) {
        switch (axis) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
            default:
                return 0;
        }
    }
}
