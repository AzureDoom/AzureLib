package mod.azure.azurelib.animation.molang;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;

import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Direction;
import mod.azure.azurelib.util.math.Mth;

/**
 * Binds the Bedrock entity queries that take no arguments. The suppliers are created once and read the entity from
 * {@link AzMolangQueryContext}, so binding them every frame allocates nothing.
 */
public final class AzEntityMolangQueries {

    private static final AzMolangQueryContext CONTEXT = AzMolangQueryContext.INSTANCE;

    private static final Binding[] BINDINGS = {
        flag(MolangQueries.IS_ON_FIRE, Mc::isOnFire),
        flag(MolangQueries.IS_ALIVE, Mc::isAlive),
        // Beta has no invisibility, sprinting, swimming, silencing, spectators, no-gravity or elytra.
        flag(MolangQueries.IS_INVISIBLE, entity -> false),
        flag(MolangQueries.IS_SNEAKING, Mc::isSneaking),
        flag(MolangQueries.IS_SPRINTING, entity -> false),
        flag(MolangQueries.IS_SWIMMING, entity -> false),
        flag(MolangQueries.IS_RIDING, Mc::isRiding),
        flag(MolangQueries.HAS_RIDER, Mc::hasPassenger),
        flag(MolangQueries.IS_IN_LAVA, Mc::isInLava),
        flag(MolangQueries.IS_SILENT, entity -> false),
        flag(MolangQueries.IS_SPECTATOR, entity -> false),
        // Beta keeps fire immunity in a protected field that only a handful of vanilla mobs set.
        flag(MolangQueries.IS_FIRE_IMMUNE, entity -> false),
        flag(MolangQueries.HAS_GRAVITY, entity -> true),
        flag(MolangQueries.HAS_COLLISION, entity -> !Mc.noClip(entity)),
        // Beta's "submerged in water" is the eye-in-water check, i.e. 1.7.10's isInsideOfMaterial(water).
        flag(MolangQueries.HEAD_IS_IN_WATER, Mc::isInWater),
        flag(MolangQueries.IS_LOCAL_PLAYER, entity -> entity == McClient.player()),
        flag(MolangQueries.IS_FIRST_PERSON, McClient::isFirstPersonCamera),
        flag(
            MolangQueries.IS_MOVING,
            entity -> Mc.velocityX(entity) * Mc.velocityX(entity) + Mc.velocityZ(entity) * Mc.velocityZ(entity) > 1.0E-6
        ),
        // Beta has no leads.
        flag(MolangQueries.IS_LEASHED, entity -> false),
        flag(MolangQueries.IS_TAMED, Mc::isTamed),
        flag(MolangQueries.IS_SITTING, Mc::isSitting),
        value(MolangQueries.VERTICAL_SPEED, entity -> Mc.velocityY(entity) * 20),
        value(MolangQueries.CARDINAL_FACING_2D, entity -> Direction.fromYaw(Mc.yaw(entity)).ordinal()),
        value(
            MolangQueries.BODY_X_ROTATION,
            entity -> Mth.lerp(CONTEXT.partialTicks(), Mc.prevPitch(entity), Mc.pitch(entity))
        ),
        value(MolangQueries.BODY_Y_ROTATION, entity -> {
            if (entity instanceof MobEntity) {
                MobEntity living = (MobEntity) entity;
                return Mth.lerp(CONTEXT.partialTicks(), Mc.prevBodyYaw(living), Mc.bodyYaw(living));
            }
            return Mth.lerp(CONTEXT.partialTicks(), Mc.prevYaw(entity), Mc.yaw(entity));
        }),
        // Beta has no experience levels.
        value(MolangQueries.PLAYER_LEVEL, entity -> 0),
        livingFlag(MolangQueries.IS_SLEEPING, Mc::isSleeping),
        livingFlag(MolangQueries.IS_GLIDING, living -> false),
        livingFlag(
            MolangQueries.HAS_HEAD_GEAR,
            living -> !AzureLibUtil.isEmpty(AzEquipmentSlot.HEAD.getStack(living))
        ),
        // Beta has no baby mobs.
        livingValue(MolangQueries.MODEL_SCALE, living -> 1.0),
        livingValue(MolangQueries.SWIM_AMOUNT, living -> 0),
        livingValue(MolangQueries.DEATH_TICKS, living -> {
            int deathTime = Mc.deathTime(living);
            return deathTime == 0 ? 0 : deathTime + CONTEXT.partialTicks();
        }),
        livingValue(MolangQueries.EQUIPMENT_COUNT, living -> {
            int count = 0;

            for (AzEquipmentSlot slot : AzEquipmentSlot.values()) {
                if (
                    slot.getSlotType() == AzEquipmentSlot.Type.ARMOR && !AzureLibUtil.isEmpty(slot.getStack(living))
                )
                    count++;
            }

            return count;
        }),
        // Beta items have no "use" duration: bows fire and food is eaten instantly.
        livingValue(MolangQueries.ITEM_IN_USE_DURATION, living -> 0),
        livingValue(MolangQueries.ITEM_REMAINING_USE_DURATION, living -> 0),
        livingValue(MolangQueries.ITEM_MAX_USE_DURATION, living -> 0)
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

    private static Binding flag(String query, Predicate<MobEntity> condition) {
        return value(query, entity -> RenderUtils.booleanToFloat(condition.test((MobEntity) entity)));
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

    private static Binding livingFlag(String query, Predicate<MobEntity> condition) {
        return livingValue(query, living -> RenderUtils.booleanToFloat(condition.test(living)));
    }

    /**
     * A query for living entities only. Reads 0 for anything else.
     */
    private static Binding livingValue(String query, ToDoubleFunction<MobEntity> value) {
        return new Binding(new MolangVariableRef(query), () -> {
            MobEntity living = CONTEXT.livingEntity();
            return living == null ? 0 : value.applyAsDouble(living);
        });
    }
}
