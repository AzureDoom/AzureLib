package mod.azure.azurelib.animation.molang;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
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
 * Queries for things 1.7.10 doesn't have (silent entities, spectators, gravity toggles, elytra, the offhand) read 0, or
 * 1 for {@code has_gravity}, rather than being left unbound.
 * </p>
 */
public final class AzEntityMolangQueries {

    private static final AzMolangQueryContext CONTEXT = AzMolangQueryContext.INSTANCE;

    /** Squared distance per tick below which an entity counts as standing still. */
    private static final double MOVING_THRESHOLD_SQR = 1.0E-6;

    private static final double TICKS_PER_SECOND = 20;

    /**
     * 1.7.10's horizontal facing index (0 south, 1 west, 2 north, 3 east) to the 3D direction index Bedrock and later
     * versions use (2 north, 3 south, 4 west, 5 east).
     */
    private static final int[] FACING_TO_3D = { 3, 4, 2, 5 };

    private static final Binding[] BINDINGS = {
        flag(MolangQueries.IS_ON_FIRE, Entity::isBurning),
        flag(MolangQueries.IS_ALIVE, Entity::isEntityAlive),
        flag(MolangQueries.IS_INVISIBLE, Entity::isInvisible),
        flag(MolangQueries.IS_SNEAKING, Entity::isSneaking),
        flag(MolangQueries.IS_SPRINTING, Entity::isSprinting),
        // 1.7.10 has no swimming pose, so any entity afloat in water counts as swimming.
        flag(MolangQueries.IS_SWIMMING, AzEntityMolangQueries::isSwimming),
        flag(MolangQueries.IS_RIDING, Entity::isRiding),
        flag(MolangQueries.HAS_RIDER, entity -> entity.riddenByEntity != null),
        flag(MolangQueries.IS_IN_LAVA, Entity::handleLavaMovement),
        flag(MolangQueries.IS_SILENT, entity -> false),
        flag(MolangQueries.IS_SPECTATOR, entity -> false),
        flag(MolangQueries.IS_FIRE_IMMUNE, Entity::isImmuneToFire),
        flag(MolangQueries.HAS_GRAVITY, entity -> true),
        flag(MolangQueries.HAS_COLLISION, entity -> !entity.noClip),
        flag(MolangQueries.HEAD_IS_IN_WATER, entity -> entity.isInsideOfMaterial(Material.water)),
        flag(MolangQueries.IS_LOCAL_PLAYER, entity -> entity == Minecraft.getMinecraft().thePlayer),
        flag(MolangQueries.IN_AIR, entity -> !entity.onGround),
        flag(MolangQueries.IS_ON_GROUND, entity -> entity.onGround),
        flag(MolangQueries.IS_IN_WATER, Entity::isInWater),
        flag(MolangQueries.IS_IN_WATER_OR_RAIN, Entity::isWet),
        // Position change since last tick rather than motionX/Y/Z, which is unreliable for other entities on the
        // client.
        flag(
            MolangQueries.IS_MOVING,
            entity -> lengthSqr(
                entity.posX - entity.prevPosX,
                entity.posY - entity.prevPosY,
                entity.posZ - entity.prevPosZ
            ) > MOVING_THRESHOLD_SQR
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
        flag(MolangQueries.HAS_TARGET, entity -> CONTEXT.target() != null),
        value(MolangQueries.TARGET_X_ROTATION, entity -> CONTEXT.targetXRotation()),
        value(MolangQueries.TARGET_Y_ROTATION, entity -> CONTEXT.targetYRotation()),
        // Metres per second, as Bedrock defines them.
        value(MolangQueries.VERTICAL_SPEED, entity -> (entity.posY - entity.prevPosY) * TICKS_PER_SECOND),
        value(
            MolangQueries.GROUND_SPEED,
            entity -> Math.sqrt(lengthSqr(entity.posX - entity.prevPosX, 0, entity.posZ - entity.prevPosZ))
                * TICKS_PER_SECOND
        ),
        value(MolangQueries.DISTANCE_FROM_CAMERA, entity -> CONTEXT.distanceFromCamera()),
        value(
            MolangQueries.CARDINAL_FACING_2D,
            entity -> FACING_TO_3D[MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3]
        ),
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
        // Bedrock's is_first_person: for an item, whether it renders in a first-person hand; otherwise whether this
        // is the render view entity with the camera in first person.
        new Binding(new MolangVariableRef(MolangQueries.IS_FIRST_PERSON), true, () -> {
            AzItemDisplayContext display = CONTEXT.itemDisplayContext();

            if (display != null)
                return RenderUtils.booleanToFloat(display.isFirstPerson());

            Entity entity = CONTEXT.entity();
            Minecraft minecraft = Minecraft.getMinecraft();

            return RenderUtils.booleanToFloat(
                entity != null && entity == minecraft.renderViewEntity && minecraft.gameSettings.thirdPersonView == 0
            );
        }),

        livingFlag(MolangQueries.IS_SLEEPING, EntityLivingBase::isPlayerSleeping),
        livingFlag(MolangQueries.IS_GLIDING, living -> false),
        // Sword blocking; players only in 1.7.10.
        livingFlag(
            MolangQueries.IS_BLOCKING,
            living -> living instanceof EntityPlayer && ((EntityPlayer) living).isBlocking()
        ),
        livingFlag(MolangQueries.IS_USING_ITEM, AzEntityMolangQueries::isUsingItem),
        livingFlag(MolangQueries.HAS_HEAD_GEAR, living -> AzEquipmentSlot.HEAD.getStack(living) != null),
        livingValue(MolangQueries.HEALTH, EntityLivingBase::getHealth),
        livingValue(MolangQueries.MAX_HEALTH, EntityLivingBase::getMaxHealth),
        livingValue(MolangQueries.YAW_SPEED, living -> living.rotationYaw - living.prevRotationYaw),
        // 1.7.10 has no scale attribute; babies render at half size, as in later versions.
        livingValue(MolangQueries.MODEL_SCALE, living -> living.isChild() ? 0.5 : 1),
        // No swim animation progress in 1.7.10, so this is fully in or fully out.
        livingValue(MolangQueries.SWIM_AMOUNT, living -> isSwimming(living) ? 1 : 0),
        livingValue(MolangQueries.DEATH_TICKS, living -> {
            int deathTime = living.deathTime;
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
        // Seconds, smoothed with the partial tick and capped at the item's use duration, as Bedrock specifies. Only
        // players use items in 1.7.10.
        livingValue(MolangQueries.ITEM_IN_USE_DURATION, living -> {
            if (!isUsingItem(living))
                return 0;

            EntityPlayer player = (EntityPlayer) living;
            double seconds = (player.getItemInUseDuration() + CONTEXT.partialTicks()) / TICKS_PER_SECOND;
            double max = player.getItemInUse().getMaxItemUseDuration() / TICKS_PER_SECOND;

            return max > 0 ? Math.min(seconds, max) : seconds;
        }),
        livingValue(
            MolangQueries.ITEM_REMAINING_USE_DURATION,
            living -> isUsingItem(living) ? ((EntityPlayer) living).getItemInUseCount() / TICKS_PER_SECOND : 0
        ),
        livingValue(MolangQueries.ITEM_MAX_USE_DURATION, living -> {
            if (!isUsingItem(living))
                return 0;

            ItemStack useItem = ((EntityPlayer) living).getItemInUse();
            return useItem == null ? 0 : useItem.getMaxItemUseDuration() / TICKS_PER_SECOND;
        }),

        // These change within a tick, so they are evaluated on every read rather than memoized.
        live(livingValue(MolangQueries.HEAD_YAW, living -> {
            float partialTicks = CONTEXT.partialTicks();
            return Mth.lerp(partialTicks, living.prevRotationYawHead, living.rotationYawHead)
                - Mth.lerp(partialTicks, living.prevRenderYawOffset, living.renderYawOffset);
        })),
        live(
            livingValue(
                MolangQueries.HEAD_PITCH,
                living -> Mth.lerp(CONTEXT.partialTicks(), living.prevRotationPitch, living.rotationPitch)
            )
        ),
        live(
            livingValue(
                MolangQueries.HURT_TIME,
                living -> living.hurtTime == 0 ? 0 : living.hurtTime - CONTEXT.partialTicks()
            )
        ),
        live(livingFlag(MolangQueries.IS_BABY, EntityLivingBase::isChild)),
        live(livingValue(MolangQueries.LIMB_SWING, living -> living.limbSwing)),
        live(
            livingValue(
                MolangQueries.LIMB_SWING_AMOUNT,
                living -> Mth.lerp(CONTEXT.partialTicks(), living.prevLimbSwingAmount, living.limbSwingAmount)
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
        return entity.isInWater() && !entity.onGround;
    }

    private static boolean isUsingItem(EntityLivingBase living) {
        return living instanceof EntityPlayer && ((EntityPlayer) living).isUsingItem();
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

    private static Binding livingFlag(String query, Predicate<EntityLivingBase> condition) {
        return livingValue(query, living -> RenderUtils.booleanToFloat(condition.test(living)));
    }

    /**
     * A query for living entities (including players) only. Reads 0 for anything else.
     */
    private static Binding livingValue(String query, ToDoubleFunction<EntityLivingBase> value) {
        return new Binding(new MolangVariableRef(query), true, () -> {
            EntityLivingBase living = CONTEXT.livingEntity();
            return living == null ? 0 : value.applyAsDouble(living);
        });
    }
}
