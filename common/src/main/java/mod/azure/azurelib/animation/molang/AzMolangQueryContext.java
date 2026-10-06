package mod.azure.azurelib.animation.molang;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * AzureLib's {@link MolangQueryContext}: answers query functions for whatever animatable is currently being animated.
 * <p>
 * There is one shared instance, re-bound by each animator right before it evaluates its animations (see
 * {@link AzAnimator#applyMolangQueries}), so binding allocates nothing. Render thread only.
 * </p>
 */
public final class AzMolangQueryContext implements MolangQueryContext {

    public static final AzMolangQueryContext INSTANCE = new AzMolangQueryContext();

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
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
        this.livingEntity = entity instanceof LivingEntity living ? living : null;
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

        var position = entity.getPosition(partialTicks);
        return axis(position.x, position.y, position.z, axis);
    }

    @Override
    public double positionDelta(int axis) {
        if (entity == null)
            return 0;

        var delta = entity.getDeltaMovement();
        return axis(delta.x, delta.y, delta.z, axis);
    }

    @Override
    public double movementDirection(int axis) {
        if (entity == null)
            return 0;

        var delta = entity.getDeltaMovement();
        var length = delta.length();

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

        var slot = hand == 1 ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;

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

        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
    }

    private static double height(Heightmap.Types type, double x, double z) {
        var level = Minecraft.getInstance().level;

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
