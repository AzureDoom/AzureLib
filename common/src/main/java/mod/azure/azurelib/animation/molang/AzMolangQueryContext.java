package mod.azure.azurelib.animation.molang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.core.molang.MolangArray;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.MolangScope;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * AzureLib's {@link MolangQueryContext}: answers query functions for whatever animatable is currently being animated.
 * <p>
 * There is one shared instance, re-bound by each animator right before it evaluates its animations (see
 * {@link AzAnimator#applyMolangQueries}), so binding allocates nothing. Render thread only.
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
 * <li>While an item renders, the entity holding it and the display context. Item animations then answer entity queries
 * for the holder, as Bedrock attachables do, and {@code query.is_first_person} reflects the item's own view.</li>
 * </ul>
 */
public final class AzMolangQueryContext implements MolangQueryContext {

    public static final AzMolangQueryContext INSTANCE = new AzMolangQueryContext();

    /** Upper bound on {@code query.get_nearby_entities} radius, to keep a typo from scanning half the level. */
    public static final double MAX_NEARBY_RADIUS = 64;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private static final int ROTATION = 0;

    private static final int POSITION = 1;

    private static final int SCALE = 2;

    /** Used for {@code variable.*} when the arrow targets an entity without an AzureLib animator. */
    private static final MolangScope DETACHED_SCOPE = new MolangScope();

    @Nullable
    private Entity entity;

    @Nullable
    private LivingEntity livingEntity;

    private float partialTicks;

    private Entity[] pushedEntities = new Entity[4];

    private MolangScope[] pushedScopes = new MolangScope[4];

    private int pushDepth;

    @Nullable
    private Entity cachedTarget;

    private boolean targetResolved;

    @Nullable
    private AzBone channelBone;

    private int channelTransform;

    private int channelAxis;

    private LivingEntity[] itemHolders = new LivingEntity[4];

    private int itemHolderDepth;

    @Nullable
    private ItemDisplayContext itemDisplayContext;

    private AzMolangQueryContext() {}

    /**
     * Makes this the active query context for the animatable about to be animated, and re-binds the entity queries so
     * none of them keeps a value memoized for a previous animatable.
     *
     * @param entity       the entity being animated, or {@code null} for items, blocks and other non-entities
     * @param partialTicks the current partial tick
     */
    public void bind(@Nullable Entity entity, float partialTicks) {
        setEntity(entity);
        this.partialTicks = partialTicks;
        this.pushDepth = 0;
        MolangQueryContext.setCurrent(this);
        AzEntityMolangQueries.bind();
    }

    private void setEntity(@Nullable Entity entity) {
        this.entity = entity;
        this.livingEntity = entity instanceof LivingEntity living ? living : null;
        this.cachedTarget = null;
        this.targetResolved = false;
    }

    /**
     * @return the entity being animated (or the arrow operator's entity), or {@code null} if the animatable isn't an
     *         entity
     */
    @Nullable
    public Entity entity() {
        return entity;
    }

    /**
     * @return {@link #entity()} if it is a {@link LivingEntity}, else {@code null}
     */
    @Nullable
    public LivingEntity livingEntity() {
        return livingEntity;
    }

    public float partialTicks() {
        return partialTicks;
    }

    // Arrow operator

    @Override
    public boolean isEntity(Object object) {
        return object instanceof Entity candidate && !candidate.isRemoved();
    }

    @Override
    public boolean pushEntity(Object object) {
        if (!(object instanceof Entity target) || target.isRemoved()) {
            return false;
        }

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
        AzAnimationContext<Entity> context = animator == null ? null : animator.findContext(target.getUUID());

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
    public void bindChannel(@Nullable AzBone bone, int transform, int axis) {
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

        return switch (channelTransform) {
            case ROTATION -> {
                AzBoneSnapshot initial = bone.getInitialAzSnapshot();
                float radians = switch (channelAxis) {
                    case 0 -> bone.getRotX() - initial.getRotX();
                    case 1 -> bone.getRotY() - initial.getRotY();
                    default -> bone.getRotZ() - initial.getRotZ();
                };
                double degrees = Math.toDegrees(radians);
                // Keyframe X and Y rotations are flipped when baked; undo that so this matches what the file wrote.
                yield channelAxis == 2 ? degrees : -degrees;
            }
            case POSITION -> switch (channelAxis) {
                case 0 -> bone.getPosX();
                case 1 -> bone.getPosY();
                default -> bone.getPosZ();
            };
            case SCALE -> switch (channelAxis) {
                case 0 -> bone.getScaleX();
                case 1 -> bone.getScaleY();
                default -> bone.getScaleZ();
            };
            default -> 0;
        };
    }

    // Item rendering

    /**
     * Records the entity holding the item about to render; matched by {@link #popItemHolder()}. Called from
     * {@code ItemRenderer#renderStatic}.
     */
    public void pushItemHolder(@Nullable LivingEntity holder) {
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
    public @Nullable LivingEntity itemHolder() {
        LivingEntity holder = itemHolderDepth > 0 ? itemHolders[itemHolderDepth - 1] : null;

        if (holder == null && itemDisplayContext != null && itemDisplayContext.firstPerson()) {
            return Minecraft.getInstance().getCameraEntity() instanceof LivingEntity camera ? camera : null;
        }

        return holder;
    }

    /**
     * Sets the display context of the item being rendered, or {@code null} once it is done.
     *
     * @return the previous display context, to hand back once rendering finishes
     */
    public @Nullable ItemDisplayContext setItemDisplayContext(@Nullable ItemDisplayContext context) {
        ItemDisplayContext previous = this.itemDisplayContext;
        this.itemDisplayContext = context;
        return previous;
    }

    public @Nullable ItemDisplayContext itemDisplayContext() {
        return itemDisplayContext;
    }

    // Targets

    /**
     * The current entity's attack target: {@link AzMolangTargetProvider#getMolangTarget()} if it implements it,
     * otherwise {@link Mob#getTarget()}. Resolved once per binding.
     */
    public @Nullable Entity target() {
        if (!targetResolved) {
            targetResolved = true;
            cachedTarget = resolveTarget(entity);
        }

        return cachedTarget;
    }

    private static @Nullable Entity resolveTarget(@Nullable Entity entity) {
        Entity target = null;

        if (entity instanceof AzMolangTargetProvider provider) {
            target = provider.getMolangTarget();
        } else if (entity instanceof Mob mob) {
            target = mob.getTarget();
        }

        return target == null || target.isRemoved() || target == entity ? null : target;
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

        Vec3 from = entity.getEyePosition(partialTicks);
        Vec3 to = target.getEyePosition(partialTicks);
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        return Mth.wrapDegrees(-(Mth.atan2(dy, horizontal) * Mth.RAD_TO_DEG));
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

        Vec3 from = entity.getEyePosition(partialTicks);
        Vec3 to = target.getEyePosition(partialTicks);

        return Mth.wrapDegrees(Mth.atan2(to.z - from.z, to.x - from.x) * Mth.RAD_TO_DEG - 90);
    }

    // Collections

    /**
     * Entities within {@code radius} blocks of the current entity, nearest first, as a Molang array.
     *
     * @param filter      an entity type id such as {@code minecraft:pig} (namespace optional), or {@code null} for all
     * @param includeSelf whether the current entity itself may be included
     */
    public MolangArray nearbyEntities(double radius, @Nullable String filter, boolean includeSelf) {
        Entity center = entity;

        if (center == null || !(radius > 0)) {
            return MolangArray.EMPTY;
        }

        double clamped = Math.min(radius, MAX_NEARBY_RADIUS);
        double radiusSqr = clamped * clamped;
        Identifier type = filter == null || filter.isEmpty() ? null : Identifier.tryParse(filter);

        if (filter != null && !filter.isEmpty() && type == null) {
            return MolangArray.EMPTY;
        }

        List<Entity> found = center.level()
            .getEntities(
                includeSelf ? null : center,
                center.getBoundingBox().inflate(clamped),
                candidate -> candidate.isAlive()
                    && candidate.distanceToSqr(center) <= radiusSqr
                    && (type == null || type.equals(BuiltInRegistries.ENTITY_TYPE.getKey(candidate.getType())))
            );

        if (found.isEmpty()) {
            return MolangArray.EMPTY;
        }

        ArrayList<Entity> sorted = new ArrayList<>(found);
        sorted.sort(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center)));
        return MolangArray.of(sorted);
    }

    // Query functions

    @Override
    public double position(int axis) {
        if (entity == null)
            return 0;

        Vec3 position = entity.getPosition(partialTicks);
        return axis(position.x, position.y, position.z, axis);
    }

    @Override
    public double positionDelta(int axis) {
        if (entity == null)
            return 0;

        Vec3 delta = entity.getDeltaMovement();
        return axis(delta.x, delta.y, delta.z, axis);
    }

    @Override
    public double movementDirection(int axis) {
        if (entity == null)
            return 0;

        Vec3 delta = entity.getDeltaMovement();
        double length = delta.length();

        return length < 1.0E-7 ? 0 : axis(delta.x, delta.y, delta.z, axis) / length;
    }

    @Override
    public double hasArmorSlot(int slot) {
        if (livingEntity == null || slot < 0 || slot >= ARMOR_SLOTS.length)
            return 0;

        return livingEntity.getItemBySlot(ARMOR_SLOTS[slot]).isEmpty() ? 0 : 1;
    }

    @Override
    public double armorDamageSlot(int slot) {
        if (livingEntity == null || slot < 0 || slot >= ARMOR_SLOTS.length)
            return 0;

        return livingEntity.getItemBySlot(ARMOR_SLOTS[slot]).getDamageValue();
    }

    @Override
    public double isItemEquipped(int hand) {
        if (livingEntity == null)
            return 0;

        EquipmentSlot slot = hand == 1 ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;

        return livingEntity.getItemBySlot(slot).isEmpty() ? 0 : 1;
    }

    @Override
    public double heightmap(double x, double z) {
        return height(Heightmap.Types.WORLD_SURFACE, x, z);
    }

    @Override
    public double aboveTopSolid(double x, double z) {
        return height(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    @Override
    public double distanceFromCamera() {
        if (entity == null)
            return 0;

        return Minecraft.getInstance().gameRenderer.mainCamera().position().distanceTo(entity.position());
    }

    private static double height(Heightmap.Types type, double x, double z) {
        ClientLevel level = Minecraft.getInstance().level;

        return level == null ? 0 : level.getHeight(type, (int) Math.floor(x), (int) Math.floor(z));
    }

    private static double axis(double x, double y, double z, int axis) {
        return switch (axis) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> 0;
        };
    }
}
