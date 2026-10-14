package mod.azure.azurelib.platform.mc;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.util.AzEquipmentHolder;

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
        return entity.id;
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
        return e.lastTickX;
    }

    public static double lastTickY(Entity e) {
        return e.lastTickY;
    }

    public static double lastTickZ(Entity e) {
        return e.lastTickZ;
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
        return e.prevYaw;
    }

    public static float pitch(Entity e) {
        return e.pitch;
    }

    public static float prevPitch(Entity e) {
        return e.prevPitch;
    }

    public static int age(Entity e) {
        return e.age;
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
        return e.dead;
    }

    public static boolean isAlive(Entity e) {
        return !e.dead && (!(e instanceof LivingEntity) || health((LivingEntity) e) > 0);
    }

    public static boolean isSneaking(Entity e) {
        return e.isSneaking();
    }

    public static boolean isOnFire(Entity e) {
        return e.isOnFire();
    }

    public static boolean isInWater(Entity e) {
        return e.isSubmergedInWater();
    }

    public static boolean isTouchingWater(Entity e) {
        return e.isSubmergedInWater();
    }

    public static boolean isInLava(Entity e) {
        return e.isTouchingLava();
    }

    public static boolean noClip(Entity e) {
        return e.noClip;
    }

    public static boolean isSleeping(LivingEntity e) {
        return e.isSleeping();
    }

    public static boolean isTamed(Entity e) {
        return e instanceof WolfEntity && ((WolfEntity) e).isTamed();
    }

    public static boolean isSitting(Entity e) {
        return e instanceof WolfEntity && ((WolfEntity) e).isInSittingPose();
    }

    public static boolean isWet(Entity e) {
        return e.isWet();
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
        return e.passenger;
    }

    public static boolean hasPassenger(Entity e) {
        return e.passenger != null;
    }

    public static float fallDistance(Entity e) {
        return e.fallDistance;
    }

    public static float brightness(Entity e, float partialTick) {
        return e.getBrightnessAtEyes(partialTick);
    }

    public static boolean isLiving(Entity e) {
        return e instanceof LivingEntity;
    }

    public static int health(LivingEntity e) {
        return e.health;
    }

    public static int maxHealth(LivingEntity e) {
        return DEFAULT_MAX_HEALTH;
    }

    public static int hurtTime(LivingEntity e) {
        return e.hurtTime;
    }

    public static int deathTime(LivingEntity e) {
        return e.deathTime;
    }

    public static float bodyYaw(LivingEntity e) {
        return e.bodyYaw;
    }

    public static float prevBodyYaw(LivingEntity e) {
        return e.lastBodyYaw;
    }

    public static float headYaw(LivingEntity e) {
        return e.yaw;
    }

    public static float prevHeadYaw(LivingEntity e) {
        return e.prevYaw;
    }

    public static float limbSwing(LivingEntity e) {
        return e.walkAnimationProgress;
    }

    public static float limbSwingAmount(LivingEntity e) {
        return e.walkAnimationSpeed;
    }

    public static float prevLimbSwingAmount(LivingEntity e) {
        return e.lastWalkAnimationSpeed;
    }

    public static float swingProgress(LivingEntity e, float partialTick) {
        return e.getHandSwingProgress(partialTick);
    }

    public static boolean isSwinging(PlayerEntity e) {
        return e.handSwinging;
    }

    @Nullable
    public static ItemStack heldItem(LivingEntity e) {
        if (e instanceof AzEquipmentHolder) {
            return ((AzEquipmentHolder) e).getAzEquipment(0);
        }
        if (e instanceof PlayerEntity) {
            return ((PlayerEntity) e).inventory.getSelectedItem();
        }
        return e.getHeldItem();
    }

    @Nullable
    public static ItemStack armor(LivingEntity e, int armorIndex) {
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
        return world.isRemote;
    }

    public static long time(World world) {
        return world.getTime();
    }

    @SuppressWarnings("unchecked")
    public static List<Entity> entities(World world) {
        return world.entities == null ? Collections.emptyList() : (List<Entity>) world.entities;
    }

    @SuppressWarnings("unchecked")
    public static List<PlayerEntity> players(World world) {
        return world.players == null ? Collections.emptyList() : (List<PlayerEntity>) world.players;
    }

    @Nullable
    public static Entity entityById(World world, int id) {
        for (Entity entity : entities(world)) {
            if (entity.id == id)
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
        return world.getBlockMeta(x, y, z);
    }

    public static int blockId(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z);
    }

    public static int topBlockY(World world, int x, int z) {
        for (int y = 127; y > 0; y--) {
            if (world.getBlockId(x, y, z) != 0)
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
        return stack.count;
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
        return item instanceof ArmorItem ? ((ArmorItem) item).equipmentSlot : -1;
    }

    public static NbtCompound stackNbt(ItemStack stack) {
        return stack.getStationNbt();
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

    public static int armorTextureIndex(@Nullable Item item) {
        return item instanceof ArmorItem ? ((ArmorItem) item).textureIndex : -1;
    }

    public static int blockId(net.minecraft.block.Block block) {
        return block.id;
    }

    public static boolean isHandheld(@Nullable Item item) {
        return item != null && item.isHandheld();
    }

    public static boolean isHandheldRod(@Nullable Item item) {
        return item != null && item.isHandheldRod();
    }

    public static ItemStack itemEntityStack(ItemEntity entity) {
        return entity.stack;
    }

    public static float itemEntityHoverStart(ItemEntity entity) {
        return entity.initialRotationAngle;
    }
}
