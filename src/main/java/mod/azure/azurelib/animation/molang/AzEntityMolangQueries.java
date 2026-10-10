package mod.azure.azurelib.animation.molang;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Mth;

/**
 * Binds the Bedrock entity queries that take no arguments. The suppliers are created once and read the entity from
 * {@link AzMolangQueryContext}, so binding them every frame allocates nothing.
 */
public final class AzEntityMolangQueries {

    private static final AzMolangQueryContext CONTEXT = AzMolangQueryContext.INSTANCE;

    private static final Binding[] BINDINGS = {
        flag(MolangQueries.IS_ON_FIRE, Entity::isBurning),
        flag(MolangQueries.IS_ALIVE, Entity::isEntityAlive),
        flag(MolangQueries.IS_INVISIBLE, Entity::isInvisible),
        flag(MolangQueries.IS_SNEAKING, Entity::isSneaking),
        flag(MolangQueries.IS_SPRINTING, Entity::isSprinting),
        // 1.12.2 has no swimming state; the closest equivalent is sprinting while in water.
        flag(MolangQueries.IS_SWIMMING, entity -> entity.isInWater() && entity.isSprinting()),
        flag(MolangQueries.IS_RIDING, Entity::isRiding),
        flag(MolangQueries.HAS_RIDER, Entity::isBeingRidden),
        flag(MolangQueries.IS_IN_LAVA, Entity::isInLava),
        flag(MolangQueries.IS_SILENT, Entity::isSilent),
        flag(
            MolangQueries.IS_SPECTATOR,
            entity -> entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator()
        ),
        flag(MolangQueries.IS_FIRE_IMMUNE, Entity::isImmuneToFire),
        flag(MolangQueries.HAS_GRAVITY, entity -> !entity.hasNoGravity()),
        flag(MolangQueries.HAS_COLLISION, entity -> !entity.noClip),
        flag(MolangQueries.HEAD_IS_IN_WATER, entity -> entity.isInsideOfMaterial(Material.WATER)),
        flag(MolangQueries.IS_LOCAL_PLAYER, entity -> entity == Minecraft.getMinecraft().player),
        flag(MolangQueries.IS_FIRST_PERSON, entity -> {
            Minecraft minecraft = Minecraft.getMinecraft();
            return entity == minecraft.getRenderViewEntity() && minecraft.gameSettings.thirdPersonView == 0;
        }),
        flag(
            MolangQueries.IS_MOVING,
            entity -> entity.motionX * entity.motionX + entity.motionZ * entity.motionZ > 1.0E-6
        ),
        flag(
            MolangQueries.IS_LEASHED,
            entity -> entity instanceof EntityLiving && ((EntityLiving) entity).getLeashed()
        ),
        flag(
            MolangQueries.IS_TAMED,
            entity -> entity instanceof EntityTameable && ((EntityTameable) entity).isTamed()
        ),
        flag(
            MolangQueries.IS_SITTING,
            entity -> entity instanceof EntityTameable && ((EntityTameable) entity).isSitting()
        ),
        value(MolangQueries.VERTICAL_SPEED, entity -> entity.motionY * 20),
        value(MolangQueries.CARDINAL_FACING_2D, entity -> entity.getHorizontalFacing().getIndex()),
        value(
            MolangQueries.BODY_X_ROTATION,
            entity -> Mth.lerp(CONTEXT.partialTicks(), entity.prevRotationPitch, entity.rotationPitch)
        ),
        value(MolangQueries.BODY_Y_ROTATION, entity -> {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) entity;
                return Mth.lerp(CONTEXT.partialTicks(), living.prevRenderYawOffset, living.renderYawOffset);
            }
            return Mth.lerp(CONTEXT.partialTicks(), entity.prevRotationYaw, entity.rotationYaw);
        }),
        value(
            MolangQueries.PLAYER_LEVEL,
            entity -> entity instanceof EntityPlayer ? ((EntityPlayer) entity).experienceLevel : 0
        ),
        livingFlag(MolangQueries.IS_SLEEPING, EntityLivingBase::isPlayerSleeping),
        livingFlag(MolangQueries.IS_GLIDING, EntityLivingBase::isElytraFlying),
        livingFlag(
            MolangQueries.HAS_HEAD_GEAR,
            living -> !living.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()
        ),
        // 1.18's LivingEntity#getScale is 0.5 for babies and 1 otherwise.
        livingValue(MolangQueries.MODEL_SCALE, living -> living.isChild() ? 0.5 : 1.0),
        // 1.12.2 has no swim animation amount.
        livingValue(MolangQueries.SWIM_AMOUNT, living -> 0),
        livingValue(MolangQueries.DEATH_TICKS, living -> {
            int deathTime = living.deathTime;
            return deathTime == 0 ? 0 : deathTime + CONTEXT.partialTicks();
        }),
        livingValue(MolangQueries.EQUIPMENT_COUNT, living -> {
            int count = 0;

            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (
                    slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !living.getItemStackFromSlot(slot)
                        .isEmpty()
                )
                    count++;
            }

            return count;
        }),
        livingValue(MolangQueries.ITEM_IN_USE_DURATION, living -> living.getItemInUseMaxCount() / 20D),
        livingValue(MolangQueries.ITEM_REMAINING_USE_DURATION, living -> living.getItemInUseCount() / 20D),
        livingValue(MolangQueries.ITEM_MAX_USE_DURATION, living -> {
            ItemStack useItem = living.getActiveItemStack();
            return useItem.isEmpty() ? 0 : useItem.getMaxItemUseDuration() / 20D;
        })
    };

    private AzEntityMolangQueries() {
        throw new UnsupportedOperationException();
    }

    /**
     * Binds every query in this class to the entity currently bound in {@link AzMolangQueryContext}. Each query is
     * memoized, so it is calculated at most once per entity per frame, and only if an animation reads it.
     */
    public static void bind() {
        for (AzEntityMolangQueries.Binding binding : BINDINGS) {
            binding.ref.setMemoized(binding.supplier);
        }
    }

    private static final class Binding {

        private final MolangVariableRef ref;

        private final DoubleSupplier supplier;

        private Binding(MolangVariableRef ref, DoubleSupplier supplier) {
            this.ref = ref;
            this.supplier = supplier;
        }

        public MolangVariableRef ref() {
            return this.ref;
        }

        public DoubleSupplier supplier() {
            return this.supplier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Binding))
                return false;
            Binding other = (Binding) o;
            return java.util.Objects.equals(this.ref, other.ref)
                && java.util.Objects.equals(this.supplier, other.supplier);
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + java.util.Objects.hashCode(this.ref);
            result = 31 * result + java.util.Objects.hashCode(this.supplier);
            return result;
        }

        @Override
        public String toString() {
            return "Binding[ref=" + this.ref + ", supplier=" + this.supplier + "]";
        }
    }

    private static Binding flag(String query, Predicate<Entity> condition) {
        return value(query, entity -> RenderUtils.booleanToFloat(condition.test(entity)));
    }

    /**
     * A query for any entity. Reads 0 when the animatable isn't an entity.
     */
    private static Binding value(String query, ToDoubleFunction<Entity> value) {
        return new Binding(new MolangVariableRef(query), () -> {
            Entity entity = CONTEXT.entity();
            return entity == null ? 0 : value.applyAsDouble(entity);
        });
    }

    private static Binding livingFlag(String query, Predicate<EntityLivingBase> condition) {
        return livingValue(query, living -> RenderUtils.booleanToFloat(condition.test(living)));
    }

    /**
     * A query for living entities only. Reads 0 for anything else.
     */
    private static Binding livingValue(String query, ToDoubleFunction<EntityLivingBase> value) {
        return new Binding(new MolangVariableRef(query), () -> {
            EntityLivingBase living = CONTEXT.livingEntity();
            return living == null ? 0 : value.applyAsDouble(living);
        });
    }
}
