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
import mod.azure.azurelib.render.item.AzItemDisplayContext;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Mth;

/**
 * Binds the Bedrock entity queries that take no arguments. The suppliers are created once and read the entity from
 * {@link AzMolangQueryContext}, so binding them every frame allocates nothing.
 * <p>
 * Every entity query lives here (rather than in the animator) and reads the context's current entity, so they all
 * follow the arrow operator: {@code t.pig->q.health} reads the pig's health. {@link AzMolangQueryContext} re-binds them
 * whenever its entity changes, which also means items and blocks never see a value memoized for the last entity.
 * </p>
 * <p>
 * Minecraft is reached through {@link Mc} and {@link McClient}. Queries for things Beta 1.7.3 (Ornithe) doesn't have
 * (sprinting, invisibility, silent entities, spectators, gravity toggles, gliding, leashes, babies, item use, blocking,
 * experience) still bind, to a constant: 0, or 1 for {@code has_gravity}.
 * </p>
 */
public final class AzEntityMolangQueries {

    private static final AzMolangQueryContext CONTEXT = AzMolangQueryContext.INSTANCE;

    /** Squared distance per tick below which an entity counts as standing still. */
    private static final double MOVING_THRESHOLD_SQR = 1.0E-6;

    private static final double TICKS_PER_SECOND = 20;

    /**
     * Beta's horizontal facing index (0 south, 1 west, 2 north, 3 east) to the 3D direction index Bedrock and later
     * versions use (2 north, 3 south, 4 west, 5 east).
     */
    private static final int[] FACING_TO_3D = { 3, 4, 2, 5 };

    private static final Binding[] BINDINGS = {
        flag(MolangQueries.IS_ON_FIRE, Mc::isOnFire),
        flag(MolangQueries.IS_ALIVE, Mc::isAlive),
        flag(MolangQueries.IS_INVISIBLE, entity -> false),
        flag(MolangQueries.IS_SNEAKING, Mc::isSneaking),
        flag(MolangQueries.IS_SPRINTING, entity -> false),
        // No swimming pose in Beta, so any entity afloat in water counts as swimming.
        flag(MolangQueries.IS_SWIMMING, AzEntityMolangQueries::isSwimming),
        flag(MolangQueries.IS_RIDING, Mc::isRiding),
        flag(MolangQueries.HAS_RIDER, Mc::hasPassenger),
        flag(MolangQueries.IS_IN_LAVA, Mc::isInLava),
        flag(MolangQueries.IS_SILENT, entity -> false),
        flag(MolangQueries.IS_SPECTATOR, entity -> false),
        flag(MolangQueries.IS_FIRE_IMMUNE, entity -> false),
        flag(MolangQueries.HAS_GRAVITY, entity -> true),
        flag(MolangQueries.HAS_COLLISION, entity -> !Mc.noClip(entity)),
        flag(MolangQueries.HEAD_IS_IN_WATER, Mc::isInWater),
        flag(MolangQueries.IS_LOCAL_PLAYER, entity -> entity == McClient.player()),
        flag(MolangQueries.IN_AIR, entity -> !Mc.onGround(entity)),
        flag(MolangQueries.IS_ON_GROUND, Mc::onGround),
        flag(MolangQueries.IS_IN_WATER, Mc::isInWater),
        flag(MolangQueries.IS_IN_WATER_OR_RAIN, Mc::isWet),
        // Position change since last tick rather than velocity, which is unreliable for other entities in multiplayer.
        flag(
            MolangQueries.IS_MOVING,
            entity -> lengthSqr(
                Mc.x(entity) - Mc.prevX(entity),
                Mc.y(entity) - Mc.prevY(entity),
                Mc.z(entity) - Mc.prevZ(entity)
            ) > MOVING_THRESHOLD_SQR
        ),
        flag(MolangQueries.IS_LEASHED, entity -> false),
        flag(MolangQueries.IS_TAMED, Mc::isTamed),
        flag(MolangQueries.IS_SITTING, Mc::isSitting),
        flag(MolangQueries.HAS_TARGET, entity -> CONTEXT.target() != null),
        value(MolangQueries.TARGET_X_ROTATION, entity -> CONTEXT.targetXRotation()),
        value(MolangQueries.TARGET_Y_ROTATION, entity -> CONTEXT.targetYRotation()),
        // Metres per second, as Bedrock defines them.
        value(MolangQueries.VERTICAL_SPEED, entity -> (Mc.y(entity) - Mc.prevY(entity)) * TICKS_PER_SECOND),
        value(
            MolangQueries.GROUND_SPEED,
            entity -> Math.sqrt(lengthSqr(Mc.x(entity) - Mc.prevX(entity), 0, Mc.z(entity) - Mc.prevZ(entity)))
                * TICKS_PER_SECOND
        ),
        value(MolangQueries.DISTANCE_FROM_CAMERA, entity -> CONTEXT.distanceFromCamera()),
        value(
            MolangQueries.CARDINAL_FACING_2D,
            entity -> FACING_TO_3D[Mth.floor(Mc.yaw(entity) * 4.0F / 360.0F + 0.5D) & 3]
        ),
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
        value(MolangQueries.PLAYER_LEVEL, entity -> 0),
        // Bedrock's is_first_person: for an item, whether it renders in a first-person hand; otherwise whether this
        // is the camera entity with the camera in first person.
        new Binding(new MolangVariableRef(MolangQueries.IS_FIRST_PERSON), true, () -> {
            AzItemDisplayContext display = CONTEXT.itemDisplayContext();

            if (display != null)
                return RenderUtils.booleanToFloat(display.isFirstPerson());

            Entity entity = CONTEXT.entity();
            return RenderUtils.booleanToFloat(entity != null && McClient.isFirstPersonCamera(entity));
        }),

        livingFlag(MolangQueries.IS_SLEEPING, Mc::isSleeping),
        livingFlag(MolangQueries.IS_GLIDING, living -> false),
        livingFlag(MolangQueries.IS_BLOCKING, living -> false),
        livingFlag(MolangQueries.IS_USING_ITEM, living -> false),
        livingFlag(MolangQueries.HAS_HEAD_GEAR, living -> AzEquipmentSlot.HEAD.getStack(living) != null),
        livingValue(MolangQueries.HEALTH, Mc::health),
        livingValue(MolangQueries.MAX_HEALTH, Mc::maxHealth),
        livingValue(MolangQueries.YAW_SPEED, living -> Mc.yaw(living) - Mc.prevYaw(living)),
        livingValue(MolangQueries.MODEL_SCALE, living -> 1),
        // No swim animation progress in Beta, so this is fully in or fully out.
        livingValue(MolangQueries.SWIM_AMOUNT, living -> isSwimming(living) ? 1 : 0),
        livingValue(MolangQueries.DEATH_TICKS, living -> {
            int deathTime = Mc.deathTime(living);
            return deathTime == 0 ? 0 : deathTime + CONTEXT.partialTicks();
        }),
        livingValue(MolangQueries.EQUIPMENT_COUNT, living -> {
            int count = 0;

            for (AzEquipmentSlot slot : AzEquipmentSlot.values()) {
                if (slot.getSlotType() == AzEquipmentSlot.Type.ARMOR && slot.getStack(living) != null)
                    count++;
            }

            return count;
        }),
        livingValue(MolangQueries.ITEM_IN_USE_DURATION, living -> 0),
        livingValue(MolangQueries.ITEM_REMAINING_USE_DURATION, living -> 0),
        livingValue(MolangQueries.ITEM_MAX_USE_DURATION, living -> 0),

        // These change within a tick, so they are evaluated on every read rather than memoized.
        // A Beta living entity's yaw is its head yaw; the body turns separately.
        live(livingValue(MolangQueries.HEAD_YAW, living -> {
            float partialTicks = CONTEXT.partialTicks();
            return Mth.lerp(partialTicks, Mc.prevHeadYaw(living), Mc.headYaw(living))
                - Mth.lerp(partialTicks, Mc.prevBodyYaw(living), Mc.bodyYaw(living));
        })),
        live(
            livingValue(
                MolangQueries.HEAD_PITCH,
                living -> Mth.lerp(CONTEXT.partialTicks(), Mc.prevPitch(living), Mc.pitch(living))
            )
        ),
        live(livingValue(MolangQueries.HURT_TIME, living -> {
            int hurtTime = Mc.hurtTime(living);
            return hurtTime == 0 ? 0 : hurtTime - CONTEXT.partialTicks();
        })),
        live(livingFlag(MolangQueries.IS_BABY, living -> false)),
        live(livingValue(MolangQueries.LIMB_SWING, Mc::limbSwing)),
        live(
            livingValue(
                MolangQueries.LIMB_SWING_AMOUNT,
                living -> Mth.lerp(CONTEXT.partialTicks(), Mc.prevLimbSwingAmount(living), Mc.limbSwingAmount(living))
            )
        )
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
        for (Binding binding : BINDINGS) {
            if (binding.memoized) {
                binding.ref.setMemoized(binding.supplier);
            } else {
                binding.ref.set(binding.supplier);
            }
        }
    }

    private static boolean isSwimming(Entity entity) {
        return Mc.isInWater(entity) && !Mc.onGround(entity);
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

    private static Binding livingFlag(String query, Predicate<MobEntity> condition) {
        return livingValue(query, living -> RenderUtils.booleanToFloat(condition.test(living)));
    }

    /**
     * A query for living entities (including players) only. Reads 0 for anything else.
     */
    private static Binding livingValue(String query, ToDoubleFunction<MobEntity> value) {
        return new Binding(new MolangVariableRef(query), true, () -> {
            MobEntity living = CONTEXT.livingEntity();
            return living == null ? 0 : value.applyAsDouble(living);
        });
    }
}
