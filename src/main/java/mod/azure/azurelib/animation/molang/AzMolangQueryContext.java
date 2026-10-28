package mod.azure.azurelib.animation.molang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.core.molang.MolangArray;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.MolangScope;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;
import mod.azure.azurelib.util.math.Mth;

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
 * <li>While an item renders, the entity holding it and its camera transform type. Item animations then answer entity
 * queries for the holder, as Bedrock attachables do, and {@code query.is_first_person} reflects the item's own
 * view.</li>
 * </ul>
 */
public final class AzMolangQueryContext implements MolangQueryContext {

    public static final AzMolangQueryContext INSTANCE = new AzMolangQueryContext();

    /** Upper bound on {@code query.get_nearby_entities} radius, to keep a typo from scanning half the world. */
    public static final double MAX_NEARBY_RADIUS = 64;

    /** Bedrock's id for players, which 1.12.2 doesn't register in {@link EntityList}. */
    private static final ResourceLocation PLAYER_ID = new ResourceLocation("minecraft", "player");

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = {
        EntityEquipmentSlot.HEAD,
        EntityEquipmentSlot.CHEST,
        EntityEquipmentSlot.LEGS,
        EntityEquipmentSlot.FEET
    };

    private static final int ROTATION = 0;

    private static final int POSITION = 1;

    private static final int SCALE = 2;

    /** Used for {@code variable.*} when the arrow targets an entity without an AzureLib animator. */
    private static final MolangScope DETACHED_SCOPE = new MolangScope();

    private Entity entity;

    /**
     * {@link #entity} as an {@link EntityLivingBase}. Not {@link EntityLiving}: in 1.12.2 that is only mobs, and
     * players and armor stands would be left out.
     */
    private EntityLivingBase livingEntity;

    private float partialTicks;

    private Entity[] pushedEntities = new Entity[4];

    private MolangScope[] pushedScopes = new MolangScope[4];

    private int pushDepth;

    private Entity cachedTarget;

    private boolean targetResolved;

    private AzBone channelBone;

    private int channelTransform;

    private int channelAxis;

    private EntityLivingBase[] itemHolders = new EntityLivingBase[4];

    private int itemHolderDepth;

    private ItemCameraTransforms.TransformType itemDisplayContext;

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
        this.livingEntity = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
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
     * @return {@link #entity()} if it is an {@link EntityLivingBase}, else {@code null}
     */
    public EntityLivingBase livingEntity() {
        return livingEntity;
    }

    public float partialTicks() {
        return partialTicks;
    }

    // Arrow operator

    @Override
    public boolean isEntity(Object object) {
        return object instanceof Entity && !((Entity) object).isDead;
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
        AzAnimationContext<Entity> context = animator == null ? null : animator.findContext(target.getUniqueID());

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
     * {@code RenderItem#renderItem(ItemStack, EntityLivingBase, TransformType, boolean)}.
     */
    public void pushItemHolder(EntityLivingBase holder) {
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
     *         render view entity.
     */
    public EntityLivingBase itemHolder() {
        EntityLivingBase holder = itemHolderDepth > 0 ? itemHolders[itemHolderDepth - 1] : null;

        if (holder == null && isFirstPerson(itemDisplayContext)) {
            Entity view = Minecraft.getMinecraft().getRenderViewEntity();
            return view instanceof EntityLivingBase ? (EntityLivingBase) view : null;
        }

        return holder;
    }

    /**
     * @return whether {@code transformType} draws the item in a first-person hand; {@code false} for {@code null}
     */
    public static boolean isFirstPerson(ItemCameraTransforms.TransformType transformType) {
        return transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
            || transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND;
    }

    /**
     * Sets the camera transform type of the item being rendered, or {@code null} once it is done.
     *
     * @return the previous value, to hand back once rendering finishes
     */
    public ItemCameraTransforms.TransformType setItemDisplayContext(ItemCameraTransforms.TransformType context) {
        ItemCameraTransforms.TransformType previous = this.itemDisplayContext;
        this.itemDisplayContext = context;
        return previous;
    }

    public ItemCameraTransforms.TransformType itemDisplayContext() {
        return itemDisplayContext;
    }

    // Targets

    /**
     * The current entity's attack target: {@link AzMolangTargetProvider#getMolangTarget()} if it implements it,
     * otherwise {@link EntityLiving#getAttackTarget()}. Resolved once per binding.
     */
    public Entity target() {
        if (!targetResolved) {
            targetResolved = true;
            cachedTarget = resolveTarget(entity);
        }

        return cachedTarget;
    }

    private static Entity resolveTarget(Entity entity) {
        Entity target = null;

        if (entity instanceof AzMolangTargetProvider) {
            target = ((AzMolangTargetProvider) entity).getMolangTarget();
        } else if (entity instanceof EntityLiving) {
            target = ((EntityLiving) entity).getAttackTarget();
        }

        return target == null || target.isDead || target == entity ? null : target;
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

        Vec3d from = entity.getPositionEyes(partialTicks);
        Vec3d to = target.getPositionEyes(partialTicks);
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
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

        Vec3d from = entity.getPositionEyes(partialTicks);
        Vec3d to = target.getPositionEyes(partialTicks);

        return Mth.wrapDegrees(Math.atan2(to.z - from.z, to.x - from.x) * Mth.RAD_TO_DEG - 90);
    }

    // Collections

    /**
     * Entities within {@code radius} blocks of the current entity, nearest first, as a Molang array.
     *
     * @param filter      an entity id such as {@code minecraft:pig} (namespace optional; players are
     *                    {@code minecraft:player}), or {@code null} for all
     * @param includeSelf whether the current entity itself may be included
     */
    public MolangArray nearbyEntities(double radius, String filter, boolean includeSelf) {
        final Entity center = entity;

        if (center == null || !(radius > 0)) {
            return MolangArray.EMPTY;
        }

        double clamped = Math.min(radius, MAX_NEARBY_RADIUS);
        final double radiusSqr = clamped * clamped;
        final ResourceLocation type = filter == null || filter.isEmpty() ? null : new ResourceLocation(filter);

        List<Entity> found = center.world.getEntitiesInAABBexcluding(
            includeSelf ? null : center,
            center.getEntityBoundingBox().grow(clamped),
            candidate -> candidate != null
                && candidate.isEntityAlive()
                && candidate.getDistanceSq(center) <= radiusSqr
                && (type == null || type.equals(idOf(candidate)))
        );

        if (found.isEmpty()) {
            return MolangArray.EMPTY;
        }

        List<Entity> sorted = new ArrayList<>(found);
        sorted.sort(Comparator.comparingDouble(candidate -> candidate.getDistanceSq(center)));
        return MolangArray.of(sorted);
    }

    /**
     * @return the entity's registry id, {@code minecraft:player} for players, or {@code null} if it has none
     */
    public static ResourceLocation idOf(Entity entity) {
        return entity instanceof EntityPlayer ? PLAYER_ID : EntityList.getKey(entity);
    }

    // Query functions

    @Override
    public double position(int axis) {
        if (entity == null)
            return 0;

        return axis(
            Mth.lerp(partialTicks, entity.prevPosX, entity.posX),
            Mth.lerp(partialTicks, entity.prevPosY, entity.posY),
            Mth.lerp(partialTicks, entity.prevPosZ, entity.posZ),
            axis
        );
    }

    @Override
    public double positionDelta(int axis) {
        if (entity == null)
            return 0;

        return axis(entity.motionX, entity.motionY, entity.motionZ, axis);
    }

    @Override
    public double movementDirection(int axis) {
        if (entity == null)
            return 0;

        double length = Math.sqrt(
            entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ
        );

        return length < 1.0E-7 ? 0 : axis(entity.motionX, entity.motionY, entity.motionZ, axis) / length;
    }

    @Override
    public double hasArmorSlot(int slot) {
        if (livingEntity == null || slot < 0 || slot >= ARMOR_SLOTS.length)
            return 0;

        return livingEntity.getItemStackFromSlot(ARMOR_SLOTS[slot]).isEmpty() ? 0 : 1;
    }

    @Override
    public double armorDamageSlot(int slot) {
        if (livingEntity == null || slot < 0 || slot >= ARMOR_SLOTS.length)
            return 0;

        return livingEntity.getItemStackFromSlot(ARMOR_SLOTS[slot]).getItemDamage();
    }

    @Override
    public double isItemEquipped(int hand) {
        if (livingEntity == null)
            return 0;

        EntityEquipmentSlot slot = hand == 1 ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND;

        return livingEntity.getItemStackFromSlot(slot).isEmpty() ? 0 : 1;
    }

    /**
     * 1.12.2 keeps a single height map: the height of the highest block that blocks light.
     */
    @Override
    public double heightmap(double x, double z) {
        WorldClient world = Minecraft.getMinecraft().world;

        return world == null ? 0 : world.getHeight(Mth.floor(x), Mth.floor(z));
    }

    @Override
    public double aboveTopSolid(double x, double z) {
        WorldClient world = Minecraft.getMinecraft().world;

        return world == null ? 0 : world.getTopSolidOrLiquidBlock(new BlockPos(Mth.floor(x), 0, Mth.floor(z))).getY();
    }

    /**
     * Distance from the render view entity's eyes, which is where 1.12.2 places the first-person camera.
     */
    @Override
    public double distanceFromCamera() {
        Entity view = Minecraft.getMinecraft().getRenderViewEntity();

        if (entity == null || view == null)
            return 0;

        Vec3d eyes = view.getPositionEyes(partialTicks);
        double dx = eyes.x - Mth.lerp(partialTicks, entity.prevPosX, entity.posX);
        double dy = eyes.y - Mth.lerp(partialTicks, entity.prevPosY, entity.posY);
        double dz = eyes.z - Mth.lerp(partialTicks, entity.prevPosZ, entity.posZ);

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
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
