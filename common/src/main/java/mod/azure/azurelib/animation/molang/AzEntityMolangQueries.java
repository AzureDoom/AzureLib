package mod.azure.azurelib.animation.molang;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;

import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Binds the Bedrock entity queries that take no arguments. The suppliers are created once and read the entity from
 * {@link AzMolangQueryContext}, so binding them every frame allocates nothing.
 */
public final class AzEntityMolangQueries {

    private static final AzMolangQueryContext CONTEXT = AzMolangQueryContext.INSTANCE;

    private static final Binding[] BINDINGS = {
        flag(MolangQueries.IS_ON_FIRE, Entity::isOnFire),
        flag(MolangQueries.IS_ALIVE, Entity::isAlive),
        flag(MolangQueries.IS_INVISIBLE, Entity::isInvisible),
        flag(MolangQueries.IS_SNEAKING, Entity::isCrouching),
        flag(MolangQueries.IS_SPRINTING, Entity::isSprinting),
        flag(MolangQueries.IS_SWIMMING, Entity::isSwimming),
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
        flag(MolangQueries.IS_FIRST_PERSON, entity -> {
            var minecraft = Minecraft.getInstance();
            return entity == minecraft.getCameraEntity() && minecraft.options.getCameraType().isFirstPerson();
        }),
        flag(MolangQueries.IS_MOVING, entity -> entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6),
        flag(MolangQueries.IS_LEASHED, entity -> entity instanceof Mob mob && mob.isLeashed()),
        flag(MolangQueries.IS_TAMED, entity -> entity instanceof TamableAnimal tamable && tamable.isTame()),
        flag(
            MolangQueries.IS_SITTING,
            entity -> entity instanceof TamableAnimal tamable && tamable.isInSittingPose()
        ),
        value(MolangQueries.VERTICAL_SPEED, entity -> entity.getDeltaMovement().y * 20),
        value(MolangQueries.CARDINAL_FACING_2D, entity -> entity.getDirection().get3DDataValue()),
        value(MolangQueries.BODY_X_ROTATION, entity -> entity.getViewXRot(CONTEXT.partialTicks())),
        value(MolangQueries.BODY_Y_ROTATION, entity -> {
            if (entity instanceof LivingEntity living)
                return Mth.lerp(CONTEXT.partialTicks(), living.yBodyRotO, living.yBodyRot);

            return entity.getViewYRot(CONTEXT.partialTicks());
        }),
        value(MolangQueries.PLAYER_LEVEL, entity -> entity instanceof Player player ? player.experienceLevel : 0),

        livingFlag(MolangQueries.IS_SLEEPING, LivingEntity::isSleeping),
        livingFlag(MolangQueries.IS_GLIDING, LivingEntity::isFallFlying),
        livingFlag(MolangQueries.HAS_HEAD_GEAR, living -> !living.getItemBySlot(EquipmentSlot.HEAD).isEmpty()),
        livingValue(MolangQueries.MODEL_SCALE, LivingEntity::getScale),
        livingValue(MolangQueries.SWIM_AMOUNT, living -> living.getSwimAmount(CONTEXT.partialTicks())),
        livingValue(MolangQueries.DEATH_TICKS, living -> {
            var deathTime = living.deathTime;
            return deathTime == 0 ? 0 : deathTime + CONTEXT.partialTicks();
        }),
        livingValue(MolangQueries.EQUIPMENT_COUNT, living -> {
            var count = 0;

            for (var slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR && !living.getItemBySlot(slot).isEmpty())
                    count++;
            }

            return count;
        }),
        livingValue(MolangQueries.ITEM_IN_USE_DURATION, living -> living.getTicksUsingItem() / 20D),
        livingValue(MolangQueries.ITEM_REMAINING_USE_DURATION, living -> living.getUseItemRemainingTicks() / 20D),
        livingValue(MolangQueries.ITEM_MAX_USE_DURATION, living -> {
            var useItem = living.getUseItem();
            return useItem.isEmpty() ? 0 : useItem.getUseDuration() / 20D;
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
        for (var binding : BINDINGS) {
            binding.ref.setMemoized(binding.supplier);
        }
    }

    private record Binding(
        MolangVariableRef ref,
        DoubleSupplier supplier
    ) {}

    private static Binding flag(String query, Predicate<Entity> condition) {
        return value(query, entity -> RenderUtils.booleanToFloat(condition.test(entity)));
    }

    /**
     * A query for any entity. Reads 0 when the animatable isn't an entity.
     */
    private static Binding value(String query, ToDoubleFunction<Entity> value) {
        return new Binding(new MolangVariableRef(query), () -> {
            var entity = CONTEXT.entity();
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
        return new Binding(new MolangVariableRef(query), () -> {
            var living = CONTEXT.livingEntity();
            return living == null ? 0 : value.applyAsDouble(living);
        });
    }
}
