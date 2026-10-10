package mod.azure.azurelib.animation.molang;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.util.math.Mth;

/**
 * AzureLib's {@link MolangQueryContext}: answers query functions for whatever animatable is currently being animated.
 * <p>
 * There is one shared instance, re-bound by each animator right before it evaluates its animations (see
 * {@link AzAnimator#applyMolangQueries}), so binding allocates nothing. Render thread only.
 * </p>
 */
public final class AzMolangQueryContext implements MolangQueryContext {

    public static final AzMolangQueryContext INSTANCE = new AzMolangQueryContext();

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = {
        EntityEquipmentSlot.HEAD,
        EntityEquipmentSlot.CHEST,
        EntityEquipmentSlot.LEGS,
        EntityEquipmentSlot.FEET
    };

    @Nullable
    private Entity entity;

    @Nullable
    private EntityLivingBase livingEntity;

    private float partialTicks;

    private AzMolangQueryContext() {}

    /**
     * Makes this the active query context for the animatable about to be animated.
     *
     * @param entity       the entity being animated, or {@code null} for items, blocks and other non-entities
     * @param partialTicks the current partial tick
     */
    public void bind(@Nullable Entity entity, float partialTicks) {
        this.entity = entity;
        this.livingEntity = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
        this.partialTicks = partialTicks;
        MolangQueryContext.setCurrent(this);
    }

    /**
     * @return the entity being animated, or {@code null} if the animatable isn't an entity
     */
    @Nullable
    public Entity entity() {
        return entity;
    }

    /**
     * @return the entity being animated if it is a {@link EntityLivingBase}, else {@code null}
     */
    @Nullable
    public EntityLivingBase livingEntity() {
        return livingEntity;
    }

    public float partialTicks() {
        return partialTicks;
    }

    @Override
    public double position(int axis) {
        if (entity == null)
            return 0;

        return axis(
            Mth.lerp(partialTicks, entity.lastTickPosX, entity.posX),
            Mth.lerp(partialTicks, entity.lastTickPosY, entity.posY),
            Mth.lerp(partialTicks, entity.lastTickPosZ, entity.posZ),
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

    @Override
    public double heightmap(double x, double z) {
        World level = Minecraft.getMinecraft().world;
        return level == null ? 0 : level.getHeight((int) Math.floor(x), (int) Math.floor(z));
    }

    @Override
    public double aboveTopSolid(double x, double z) {
        World level = Minecraft.getMinecraft().world;
        return level == null
            ? 0
            : level.getTopSolidOrLiquidBlock(new BlockPos(Math.floor(x), 0, Math.floor(z))).getY();
    }

    @Override
    public double distanceFromCamera() {
        if (entity == null)
            return 0;

        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null)
            return 0;
        float pt = partialTicks;
        double dx = Mth.lerp(pt, camera.lastTickPosX, camera.posX) - Mth.lerp(pt, entity.lastTickPosX, entity.posX);
        double dy = Mth.lerp(pt, camera.lastTickPosY, camera.posY) + camera.getEyeHeight() - Mth.lerp(
            pt,
            entity.lastTickPosY,
            entity.posY
        );
        double dz = Mth.lerp(pt, camera.lastTickPosZ, camera.posZ) - Mth.lerp(pt, entity.lastTickPosZ, entity.posZ);
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
