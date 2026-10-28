package mod.azure.azurelib.platform.mc;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.animal.WolfEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.mixins.accessor.EntityRegistryAccessor;
import mod.azure.azurelib.util.AzEquipmentHolder;
import mod.azure.azurelib.util.AzItemStackDataHolder;

/**
 * The single place where AzureLib touches Minecraft's entity, world, item and NBT members.
 * <p>
 * Each member access is annotated with its MCP (b1.7.3) name. When the Feather mappings for a member differ from the
 * name used here, look the MCP name up in the decompiled sources ({@code genSources}) and fix it here only.
 */
@SuppressWarnings("unused")
public final class Mc {

    public static final int DEFAULT_MAX_HEALTH = 20;

    private Mc() {
        throw new UnsupportedOperationException();
    }

    public static UUID entityUuid(Entity entity) {
        return new UUID(0xA2_0000_0000L, entityId(entity));
    }

    public static int entityId(Entity entity) {
        return entity.networkId;
    }

    public static World world(Entity entity) {
        return entity.world;
    }

    public static double x(Entity e) {
        return e.x;
    }

    public static double y(Entity e) {
        return e.y;
    }

    public static double z(Entity e) {
        return e.z;
    }

    public static double prevX(Entity e) {
        return e.prevX;
    }

    public static double prevY(Entity e) {
        return e.prevY;
    }

    public static double prevZ(Entity e) {
        return e.prevZ;
    }

    public static double lastTickX(Entity e) {
        return e.lastX;
    }

    public static double lastTickY(Entity e) {
        return e.lastY;
    }

    public static double lastTickZ(Entity e) {
        return e.lastZ;
    }

    public static double velocityX(Entity e) {
        return e.velocityX;
    }

    public static double velocityY(Entity e) {
        return e.velocityY;
    }

    public static double velocityZ(Entity e) {
        return e.velocityZ;
    }

    public static float yaw(Entity e) {
        return e.yaw;
    }

    public static float prevYaw(Entity e) {
        return e.lastYaw;
    }

    public static float pitch(Entity e) {
        return e.pitch;
    }

    public static float prevPitch(Entity e) {
        return e.lastPitch;
    }

    public static int age(Entity e) {
        return e.ticks;
    }

    public static float width(Entity e) {
        return e.width;
    }

    public static float height(Entity e) {
        return e.height;
    }

    public static float eyeHeight(Entity e) {
        return e.getEyeHeight();
    }

    public static boolean onGround(Entity e) {
        return e.onGround;
    }

    public static boolean isRemoved(Entity e) {
        return e.removed;
    }

    public static boolean isAlive(Entity e) {
        return !e.removed && (!(e instanceof MobEntity) || health((MobEntity) e) > 0);
    }

    public static boolean isSneaking(Entity e) {
        return e.isSneaking();
    }

    public static boolean isOnFire(Entity e) {
        return e.isOnFire();
    }

    public static boolean isInWater(Entity e) {
        return e.isInWater();
    }

    public static boolean isTouchingWater(Entity e) {
        return e.isInWater();
    }

    public static boolean isInLava(Entity e) {
        return e.isInLava();
    }

    public static boolean noClip(Entity e) {
        return e.noClip;
    }

    public static boolean isSleeping(MobEntity e) {
        return e.isSleeping();
    }

    public static boolean isTamed(Entity e) {
        return e instanceof WolfEntity && ((WolfEntity) e).isTame();
    }

    public static boolean isSitting(Entity e) {
        return e instanceof WolfEntity && ((WolfEntity) e).isSitting();
    }

    public static boolean isWet(Entity e) {
        return e.isInWaterOrRain();
    }

    public static boolean isRiding(Entity e) {
        return e.vehicle != null;
    }

    @Nullable
    public static Entity vehicle(Entity e) {
        return e.vehicle;
    }

    @Nullable
    public static Entity passenger(Entity e) {
        return e.rider;
    }

    public static boolean hasPassenger(Entity e) {
        return e.rider != null;
    }

    public static float fallDistance(Entity e) {
        return e.fallDistance;
    }

    public static float brightness(Entity e, float partialTick) {
        return e.getBrightness(partialTick);
    }

    public static boolean isLiving(Entity e) {
        return e instanceof MobEntity;
    }

    public static int health(MobEntity e) {
        return e.health;
    }

    public static int maxHealth(MobEntity e) {
        return DEFAULT_MAX_HEALTH;
    }

    public static int hurtTime(MobEntity e) {
        return e.damagedTimer;
    }

    public static int deathTime(MobEntity e) {
        return e.deathTicks;
    }

    public static float bodyYaw(MobEntity e) {
        return e.bodyYaw;
    }

    public static float prevBodyYaw(MobEntity e) {
        return e.lastBodyYaw;
    }

    public static float headYaw(MobEntity e) {
        return e.yaw;
    }

    public static float prevHeadYaw(MobEntity e) {
        return e.lastYaw;
    }

    public static float limbSwing(MobEntity e) {
        return e.walkAnimationProgress;
    }

    public static float limbSwingAmount(MobEntity e) {
        return e.walkAnimationSpeed;
    }

    public static float prevLimbSwingAmount(MobEntity e) {
        return e.lastWalkAnimationSpeed;
    }

    public static float swingProgress(MobEntity e, float partialTick) {
        return e.getAttackAnimationProgress(partialTick);
    }

    public static boolean isSwinging(PlayerEntity e) {
        return e.swingArm;
    }

    @Nullable
    public static ItemStack heldItem(MobEntity e) {
        if (e instanceof AzEquipmentHolder) {
            return ((AzEquipmentHolder) e).getAzEquipment(0);
        }
        if (e instanceof PlayerEntity) {
            return ((PlayerEntity) e).inventory.getSelectedItem();
        }
        return e.getDisplayItemInHand();
    }

    @Nullable
    public static ItemStack armor(MobEntity e, int armorIndex) {
        if (e instanceof AzEquipmentHolder) {
            return ((AzEquipmentHolder) e).getAzEquipment(armorIndex + 1);
        }
        if (e instanceof PlayerEntity) {
            ItemStack[] armor = ((PlayerEntity) e).inventory.armor;
            return armorIndex >= 0 && armorIndex < armor.length ? armor[armorIndex] : null;
        }
        return null;
    }

    public static boolean isPlayer(Entity e) {
        return e instanceof PlayerEntity;
    }

    public static boolean isClientSide(World world) {
        return world.isMultiplayer;
    }

    public static long time(World world) {
        return world.getTime();
    }

    public static List<Entity> entities(World world) {
        return world.entities == null ? Collections.emptyList() : world.entities;
    }

    public static List<PlayerEntity> players(World world) {
        return world.players == null ? Collections.emptyList() : world.players;
    }

    @Nullable
    public static Entity entityById(World world, int id) {
        for (Entity entity : entities(world)) {
            if (entityId(entity) == id)
                return entity;
        }
        return null;
    }

    @Nullable
    public static BlockEntity blockEntity(World world, int x, int y, int z) {
        return world.getBlockEntity(x, y, z);
    }

    public static float brightness(World world, int x, int y, int z) {
        return world.getBrightness(x, y, z);
    }

    public static int blockMeta(World world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z);
    }

    public static int blockId(World world, int x, int y, int z) {
        return world.getBlock(x, y, z);
    }

    public static int topBlockY(World world, int x, int z) {
        for (int y = 127; y > 0; y--) {
            if (blockId(world, x, y, z) != 0)
                return y + 1;
        }
        return 0;
    }

    public static World world(BlockEntity be) {
        return be.world;
    }

    public static int x(BlockEntity be) {
        return be.x;
    }

    public static int y(BlockEntity be) {
        return be.y;
    }

    public static int z(BlockEntity be) {
        return be.z;
    }

    @Nullable
    public static Item item(@Nullable ItemStack stack) {
        return stack == null ? null : stack.getItem();
    }

    public static int count(ItemStack stack) {
        return stack.size;
    }

    public static int damage(ItemStack stack) {
        return stack.getDamage();
    }

    public static int maxDamage(ItemStack stack) {
        return stack.getMaxDamage();
    }

    public static boolean isDamageable(ItemStack stack) {
        return stack.isDamageable();
    }

    public static ItemStack copy(ItemStack stack) {
        return stack.copy();
    }

    public static int itemId(Item item) {
        return item.id;
    }

    public static int armorType(@Nullable Item item) {
        return item instanceof ArmorItem ? ((ArmorItem) item).slot : -1;
    }

    /** AzureLib's own per-stack data (Beta stacks have no NBT); created on first use. */
    public static NbtCompound stackNbt(ItemStack stack) {
        return ((AzItemStackDataHolder) (Object) stack).azurelib$getOrCreateData();
    }

    @Nullable
    public static NbtCompound stackNbtOrNull(ItemStack stack) {
        return ((AzItemStackDataHolder) (Object) stack).azurelib$getDataOrNull();
    }

    public static boolean nbtHas(NbtCompound nbt, String key) {
        return nbt.contains(key);
    }

    public static long nbtGetLong(NbtCompound nbt, String key) {
        return nbt.getLong(key);
    }

    public static void nbtPutLong(NbtCompound nbt, String key, long value) {
        nbt.putLong(key, value);
    }

    public static NbtCompound nbtGetCompound(NbtCompound nbt, String key) {
        return nbt.getCompound(key);
    }

    public static void nbtPutCompound(NbtCompound nbt, String key, NbtCompound value) {
        nbt.put(key, value);
    }

    public static int armorTextureIndex(@Nullable Item item) {
        return item instanceof ArmorItem ? ((ArmorItem) item).material : -1;
    }

    public static int blockId(net.minecraft.block.Block block) {
        return block.id;
    }

    public static boolean isHandheld(@Nullable Item item) {
        return item != null && item.isWielded();
    }

    public static boolean isHandheldRod(@Nullable Item item) {
        return item != null && item.isFlippedWhenWielded();
    }

    public static ItemStack itemEntityStack(ItemEntity entity) {
        return entity.item;
    }

    public static float itemEntityHoverStart(ItemEntity entity) {
        return entity.bobOffset;
    }

    // ---- Hitboxes ---------------------------------------------------------------------------------------------------
    // Box/Vec3d constructors are private on Beta and opened by azurelib.accesswidener. Constructing directly (rather
    // than through the static factories) also guarantees fresh instances: Beta's createVector/getBoundingBoxFromPool
    // return pooled objects that are reused every tick, and hitbox claims keep their vectors across ticks.

    /** MCP: {@code Entity#boundingBox}. */
    public static Box boundingBox(Entity e) {
        return e.shape;
    }

    /** MCP: {@code AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)} (private, widened). */
    public static Box box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /** MCP: {@code Vec3D(x, y, z)} (private, widened). */
    public static Vec3d vec(double x, double y, double z) {
        return new Vec3d(x, y, z);
    }

    /**
     * An entity-style hit at {@code pos}. Only the position is read by the projectile and crosshair code AzureLib
     * hooks. MCP: {@code MovingObjectPosition(int, int, int, int, Vec3D)}.
     */
    public static HitResult hitResult(Vec3d pos) {
        return new HitResult(0, 0, 0, 1, pos);
    }

    /** MCP: {@code AxisAlignedBB#func_1169_a(Vec3D, Vec3D)} / {@code calculateIntercept}. */
    @Nullable
    public static HitResult clipBox(Box box, Vec3d from, Vec3d to) {
        return box.clip(from, to);
    }

    /** MCP: {@code AxisAlignedBB#isVecInside(Vec3D)}. */
    public static boolean boxContains(Box box, Vec3d point) {
        return box.contains(point);
    }

    /** Squared distance between entity positions, as {@code Entity#getDistanceSqToEntity} computes it. */
    public static double distanceSq(Entity a, Entity b) {
        double dx = x(a) - x(b);
        double dy = y(a) - y(b);
        double dz = z(a) - z(b);
        return dx * dx + dy * dy + dz * dz;
    }

    // Molang

    /**
     * The entity's registry name as Beta knows it ({@code "Pig"}, {@code "PigZombie"}), or {@code "player"} for
     * players, which aren't registered. {@code null} for unregistered entities. Read from {@code Entities}'
     * class-to-key map, the one {@link EntityRegistryAccessor#azurelib$getNameToClass()} exposes.
     */
    @Nullable
    public static String entityTypeName(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return "player";
        }

        Object key = EntityRegistryAccessor.azurelib$getNameToClass().get(entity.getClass());
        return key == null ? null : key.toString();
    }

    /**
     * The name {@code query.is_name_any} compares against: the player name for players, otherwise the entity's registry
     * name. Beta has no name tags.
     */
    @Nullable
    public static String displayName(Entity entity) {
        return entity instanceof PlayerEntity ? ((PlayerEntity) entity).name : entityTypeName(entity);
    }

    /**
     * Attack target for {@code query.has_target} on entities that don't implement {@code AzMolangTargetProvider}. Beta
     * keeps vanilla targets in protected per-class fields, so none are read here; implement the provider on your
     * entities instead.
     */
    @Nullable
    public static Entity attackTarget(Entity entity) {
        return null;
    }

    /**
     * The item's Beta key: its translation key without the {@code item.} / {@code tile.} prefix ({@code "bow"},
     * {@code "swordDiamond"}), or {@code null} if it has none. Beta has no namespaced item ids.
     */
    @Nullable
    public static String itemKey(@Nullable Item item) {
        if (item == null) {
            return null;
        }

        String key = item.getTranslationKey();

        if (key == null) {
            return null;
        }

        int dot = key.indexOf('.');
        return dot >= 0 && (key.startsWith("item.") || key.startsWith("tile.")) ? key.substring(dot + 1) : key;
    }
}
