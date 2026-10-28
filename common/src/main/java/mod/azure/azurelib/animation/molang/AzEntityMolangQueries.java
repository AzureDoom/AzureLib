package mod.azure.azurelib.animation.molang;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Binds the Bedrock entity queries that take no arguments. The suppliers are created once and read the entity from
 * {@link AzMolangQueryContext}, so binding them every frame allocates nothing.
 * <p>
 * Every entity query lives here (rather than in the animator) and reads the context's current entity, so they all
 * follow the arrow operator: {@code t.pig->q.health} reads the pig's health. {@link AzMolangQueryContext} re-binds them
 * whenever its entity changes, which also means items and blocks never see a value memoized for the last entity.
 * </p>
 */
public final class AzEntityMolangQueries {

    private static final AzMolangQueryContext CONTEXT = AzMolangQueryContext.INSTANCE;

    /** Squared distance per tick below which an entity counts as standing still. */
    private static final double MOVING_THRESHOLD_SQR = 1.0E-6;

    private static final double TICKS_PER_SECOND = 20;

    private static final Binding[] BINDINGS = {
        flag(MolangQueries.IS_ON_FIRE, Entity::isOnFire),
        flag(MolangQueries.IS_ALIVE, Entity::isAlive),
        flag(MolangQueries.IS_INVISIBLE, Entity::isInvisible),
        flag(MolangQueries.IS_SNEAKING, Entity::isCrouching),
        flag(MolangQueries.IS_SPRINTING, Entity::isSprinting),
        // Entity#isSwimming is only the player's sprint-swim pose; mobs count as swimming while afloat in water.
        flag(
            MolangQueries.IS_SWIMMING,
            entity -> entity.isSwimming() || (!(entity instanceof Player) && entity.isInWater() && !entity.onGround())
        ),
        flag(MolangQueries.IS_RIDING, Entity::isPassenger),
        flag(MolangQueries.HAS_RIDER, Entity::isVehicle),
        flag(MolangQueries.IS_IN_LAVA, Entity::isInLava),
        flag(MolangQueries.IS_SILENT, Entity::isSilent),
        flag(MolangQueries.IS_SPECTATOR, Entity::isSpectator),
        flag(MolangQueries.IS_FIRE_IMMUNE, Entity::fireImmune),
        flag(MolangQueries.HAS_GRAVITY, entity -> !entity.isNoGravity()),
        flag(MolangQueries.HAS_COLLISION, entity -> !entity.noPhysics),
        flag(MolangQueries.HEAD_IS_IN_WATER, entity -> entity.isEyeInFluid(FluidTags.WATER)),
        flag(MolangQueries.IS_LOCAL_PLAYER, entity -> entity == Minecraft.getInstance().player),
        flag(MolangQueries.IN_AIR, entity -> !entity.onGround()),
        flag(MolangQueries.IS_ON_GROUND, Entity::onGround),
        flag(MolangQueries.IS_IN_WATER, Entity::isInWater),
        flag(MolangQueries.IS_IN_WATER_OR_RAIN, Entity::isInWaterOrRain),
        // Position change since last tick rather than getDeltaMovement(), which is unreliable for other entities on
        // the client.
        flag(
            MolangQueries.IS_MOVING,
            entity -> lengthSqr(
                entity.getX() - entity.xo,
                entity.getY() - entity.yo,
                entity.getZ() - entity.zo
            ) > MOVING_THRESHOLD_SQR
        ),
        flag(MolangQueries.IS_LEASHED, entity -> entity instanceof Mob mob && mob.isLeashed()),
        flag(MolangQueries.IS_TAMED, entity -> entity instanceof TamableAnimal tamable && tamable.isTame()),
        flag(
            MolangQueries.IS_SITTING,
            entity -> entity instanceof TamableAnimal tamable && tamable.isInSittingPose()
        ),
        flag(MolangQueries.HAS_TARGET, entity -> CONTEXT.target() != null),
        value(MolangQueries.TARGET_X_ROTATION, entity -> CONTEXT.targetXRotation()),
        value(MolangQueries.TARGET_Y_ROTATION, entity -> CONTEXT.targetYRotation()),
        // Metres per second, as Bedrock defines them.
        value(MolangQueries.VERTICAL_SPEED, entity -> (entity.getY() - entity.yo) * TICKS_PER_SECOND),
        value(
            MolangQueries.GROUND_SPEED,
            entity -> Math.sqrt(lengthSqr(entity.getX() - entity.xo, 0, entity.getZ() - entity.zo)) * TICKS_PER_SECOND
        ),
        value(
            MolangQueries.DISTANCE_FROM_CAMERA,
            entity -> Minecraft.getInstance().gameRenderer.mainCamera().position().distanceTo(entity.position())
        ),
        value(MolangQueries.CARDINAL_FACING_2D, entity -> entity.getDirection().get3DDataValue()),
        value(MolangQueries.BODY_X_ROTATION, entity -> entity.getViewXRot(CONTEXT.partialTicks())),
        value(MolangQueries.BODY_Y_ROTATION, entity -> {
            if (entity instanceof LivingEntity living)
                return Mth.lerp(CONTEXT.partialTicks(), living.yBodyRotO, living.yBodyRot);

            return entity.getViewYRot(CONTEXT.partialTicks());
        }),
        value(MolangQueries.PLAYER_LEVEL, entity -> entity instanceof Player player ? player.experienceLevel : 0),
        // Bedrock's is_first_person: for an item, whether it renders in a first-person hand; otherwise whether this
        // is the camera entity viewed in first person.
        new Binding(new MolangVariableRef(MolangQueries.IS_FIRST_PERSON), true, () -> {
            ItemDisplayContext display = CONTEXT.itemDisplayContext();

            if (display != null)
                return RenderUtils.booleanToFloat(display.firstPerson());

            Entity entity = CONTEXT.entity();
            Minecraft minecraft = Minecraft.getInstance();

            return RenderUtils.booleanToFloat(
                entity != null && entity == minecraft.getCameraEntity()
                    && minecraft.options.getCameraType().isFirstPerson()
            );
        }),

        livingFlag(MolangQueries.IS_SLEEPING, LivingEntity::isSleeping),
        livingFlag(MolangQueries.IS_GLIDING, LivingEntity::isFallFlying),
        livingFlag(MolangQueries.IS_BLOCKING, LivingEntity::isBlocking),
        livingFlag(MolangQueries.IS_USING_ITEM, LivingEntity::isUsingItem),
        livingFlag(MolangQueries.HAS_HEAD_GEAR, living -> !living.getItemBySlot(EquipmentSlot.HEAD).isEmpty()),
        livingValue(MolangQueries.HEALTH, LivingEntity::getHealth),
        livingValue(MolangQueries.MAX_HEALTH, LivingEntity::getMaxHealth),
        livingValue(MolangQueries.YAW_SPEED, living -> living.getYRot() - living.yRotO),
        livingValue(MolangQueries.MODEL_SCALE, LivingEntity::getScale),
        livingValue(MolangQueries.SWIM_AMOUNT, living -> living.getSwimAmount(CONTEXT.partialTicks())),
        livingValue(MolangQueries.DEATH_TICKS, living -> {
            int deathTime = living.deathTime;
            return deathTime == 0 ? 0 : deathTime + CONTEXT.partialTicks();
        }),
        livingValue(MolangQueries.EQUIPMENT_COUNT, living -> {
            int count = 0;

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !living.getItemBySlot(slot).isEmpty())
                    count++;
            }

            return count;
        }),
        // Seconds, smoothed with the partial tick and capped at the item's use duration, as Bedrock specifies.
        livingValue(MolangQueries.ITEM_IN_USE_DURATION, living -> {
            if (!living.isUsingItem())
                return 0;

            double seconds = (living.getTicksUsingItem() + CONTEXT.partialTicks()) / TICKS_PER_SECOND;
            double max = living.getUseItem().getUseDuration(living) / TICKS_PER_SECOND;

            return max > 0 ? Math.min(seconds, max) : seconds;
        }),
        livingValue(MolangQueries.ITEM_REMAINING_USE_DURATION, living -> living.getUseItemRemainingTicks() / 20D),
        livingValue(MolangQueries.ITEM_MAX_USE_DURATION, living -> {
            ItemStack useItem = living.getUseItem();
            return useItem.isEmpty() ? 0 : useItem.getUseDuration(living) / 20D;
        }),

        // These change within a tick, so they are evaluated on every read rather than memoized.
        live(livingValue(MolangQueries.HEAD_YAW, living -> {
            float partialTicks = CONTEXT.partialTicks();
            return living.getViewYRot(partialTicks) - Mth.lerp(partialTicks, living.yBodyRotO, living.yBodyRot);
        })),
        live(livingValue(MolangQueries.HEAD_PITCH, living -> living.getViewXRot(CONTEXT.partialTicks()))),
        live(livingValue(MolangQueries.HURT_TIME, living -> {
            return living.hurtTime == 0 ? 0 : living.hurtTime - CONTEXT.partialTicks();
        })),
        live(livingFlag(MolangQueries.IS_BABY, LivingEntity::isBaby)),
        live(livingValue(MolangQueries.LIMB_SWING, living -> living.walkAnimation.position())),
        live(livingValue(MolangQueries.LIMB_SWING_AMOUNT, living -> {
            return living.walkAnimation.speed(CONTEXT.partialTicks());
        }))
    };

    private AzEntityMolangQueries() {
        throw new UnsupportedOperationException();
    }

    /**
     * Binds every query in this class to the entity currently bound in {@link AzMolangQueryContext}. Most queries are
     * memoized, so they are calculated at most once per binding, and only if an animation reads them. Called by
     * {@link AzMolangQueryContext} whenever its entity changes.
     */
    public static void bind() {
        for (AzEntityMolangQueries.Binding binding : BINDINGS) {
            if (binding.memoized) {
                binding.ref.setMemoized(binding.supplier);
            } else {
                binding.ref.set(binding.supplier);
            }
        }
    }

    private static double lengthSqr(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    private static final class Binding {

        private final MolangVariableRef ref;

        private final boolean memoized;

        private final DoubleSupplier supplier;

        private Binding(MolangVariableRef ref, boolean memoized, DoubleSupplier supplier) {
            this.ref = ref;
            this.memoized = memoized;
            this.supplier = supplier;
        }

        public MolangVariableRef ref() {
            return ref;
        }

        public boolean memoized() {
            return memoized;
        }

        public DoubleSupplier supplier() {
            return supplier;
        }
    }

    private static Binding live(Binding binding) {
        return new Binding(binding.ref, false, binding.supplier);
    }

    private static Binding flag(String query, Predicate<Entity> condition) {
        return value(query, entity -> RenderUtils.booleanToFloat(condition.test(entity)));
    }

    /**
     * A query for any entity. Reads 0 when the animatable isn't an entity.
     */
    private static Binding value(String query, ToDoubleFunction<Entity> value) {
        return new Binding(new MolangVariableRef(query), true, () -> {
            Entity entity = CONTEXT.entity();
            return entity == null ? 0 : value.applyAsDouble(entity);
        });
    }

    private static Binding livingFlag(String query, Predicate<LivingEntity> condition) {
        return livingValue(query, living -> RenderUtils.booleanToFloat(condition.test(living)));
    }

    /**
     * A query for living entities only. Reads 0 for anything else.
     */
    private static Binding livingValue(String query, ToDoubleFunction<LivingEntity> value) {
        return new Binding(new MolangVariableRef(query), true, () -> {
            LivingEntity living = CONTEXT.livingEntity();
            return living == null ? 0 : value.applyAsDouble(living);
        });
    }
}
