package mod.azure.azurelib.animation.molang;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.AzureLibUtil;
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

    private static final AzEquipmentSlot[] ARMOR_SLOTS = {
        AzEquipmentSlot.HEAD,
        AzEquipmentSlot.CHEST,
        AzEquipmentSlot.LEGS,
        AzEquipmentSlot.FEET
    };

    @Nullable
    private Entity entity;

    @Nullable
    private LivingEntity livingEntity;

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
        this.livingEntity = entity instanceof LivingEntity ? (LivingEntity) entity : null;
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
     * @return the entity being animated if it is a {@link LivingEntity}, else {@code null}
     */
    @Nullable
    public LivingEntity livingEntity() {
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
            Mth.lerp(partialTicks, Mc.lastTickX(entity), Mc.x(entity)),
            Mth.lerp(partialTicks, Mc.lastTickY(entity), Mc.y(entity)),
            Mth.lerp(partialTicks, Mc.lastTickZ(entity), Mc.z(entity)),
            axis
        );
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

        double length = Math.sqrt(
            Mc.velocityX(entity) * Mc.velocityX(entity) + Mc.velocityY(entity) * Mc.velocityY(entity) + Mc.velocityZ(
                entity
            ) * Mc.velocityZ(entity)
        );

        return length < 1.0E-7
            ? 0
            : axis(Mc.velocityX(entity), Mc.velocityY(entity), Mc.velocityZ(entity), axis) / length;
    }

    @Override
    public double hasArmorSlot(int slot) {
        if (livingEntity == null || slot < 0 || slot >= ARMOR_SLOTS.length)
            return 0;

        return AzureLibUtil.isEmpty(ARMOR_SLOTS[slot].getStack(livingEntity)) ? 0 : 1;
    }

    @Override
    public double armorDamageSlot(int slot) {
        if (livingEntity == null || slot < 0 || slot >= ARMOR_SLOTS.length)
            return 0;

        ItemStack stack = ARMOR_SLOTS[slot].getStack(livingEntity);
        return stack == null || !Mc.isDamageable(stack) ? 0 : Mc.maxDamage(stack) - Mc.damage(stack);
    }

    @Override
    public double isItemEquipped(int hand) {
        if (livingEntity == null)
            return 0;

        AzEquipmentSlot slot = hand == 1 ? AzEquipmentSlot.OFFHAND : AzEquipmentSlot.MAINHAND;

        return AzureLibUtil.isEmpty(slot.getStack(livingEntity)) ? 0 : 1;
    }

    @Override
    public double heightmap(double x, double z) {
        World level = McClient.world();
        return level == null ? 0 : Mc.topBlockY(level, (int) Math.floor(x), (int) Math.floor(z));
    }

    @Override
    public double aboveTopSolid(double x, double z) {
        World level = McClient.world();
        return level == null ? 0 : Mc.topBlockY(level, (int) Math.floor(x), (int) Math.floor(z));
    }

    @Override
    public double distanceFromCamera() {
        if (entity == null)
            return 0;

        Entity camera = McClient.cameraEntity();
        if (camera == null)
            return 0;
        float pt = partialTicks;
        double dx = Mth.lerp(pt, Mc.lastTickX(camera), Mc.x(camera)) - Mth.lerp(pt, Mc.lastTickX(entity), Mc.x(entity));
        double dy = Mth.lerp(pt, Mc.lastTickY(camera), Mc.y(camera)) + Mc.eyeHeight(camera) - Mth.lerp(
            pt,
            Mc.lastTickY(entity),
            Mc.y(entity)
        );
        double dz = Mth.lerp(pt, Mc.lastTickZ(camera), Mc.z(camera)) - Mth.lerp(pt, Mc.lastTickZ(entity), Mc.z(entity));
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
