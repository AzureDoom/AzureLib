package mod.azure.azurelib.common.api.common.helper;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.api.common.items.BaseGunItem;
import mod.azure.azurelib.common.internal.common.blocks.TickingLightEntity;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.common.platform.Services;
import mod.azure.azurelib.common.platform.services.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record CommonUtils() {

    /**
     * Summons an Area of Effect Cloud with the set particle, y offset, radius, duration, and effect options.
     *
     * @param entity     The Entity summoning the AoE
     * @param particle   Sets the Particle
     * @param yOffset    Set the yOffset if wanted
     * @param duration   Sets the duration of the AoE
     * @param radius     Sets the radius of the AoE
     * @param hasEffect  Should this have an effect?
     * @param effect     If it should effect, what effect?
     * @param effectTime How long the effect should be applied for?
     */
    public static void summonAoE(LivingEntity entity, ParticleOptions particle, int yOffset, int duration, float radius, boolean hasEffect, @Nullable MobEffect effect, int effectTime) {
        var areaEffectCloudEntity = new AreaEffectCloud(entity.level(), entity.getX(), entity.getY() + yOffset, entity.getZ());
        areaEffectCloudEntity.setRadius(radius);
        areaEffectCloudEntity.setDuration(duration);
        areaEffectCloudEntity.setParticle(particle);
        areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / areaEffectCloudEntity.getDuration());
        if (hasEffect && effect != null && !entity.hasEffect(effect))
            areaEffectCloudEntity.addEffect(new MobEffectInstance(effect, effectTime, 0));
        entity.level().addFreshEntity(areaEffectCloudEntity);
    }

    /**
     * Call wherever you are firing weapon to place the half tick light-block, making sure do so only on the server.
     *
     * @param entity         Usually the player or mob that is using the weapon
     * @param isInWaterBlock Checks if it's in a water block to refresh faster.
     */
    public static void spawnLightSource(Entity entity, boolean isInWaterBlock) {
        BlockPos lightBlockPos = null;
        if (lightBlockPos == null) {
            lightBlockPos = AzureLibUtil.findFreeSpace(entity.level(), entity.blockPosition(), 2);
            if (lightBlockPos == null) return;
            entity.level().setBlockAndUpdate(lightBlockPos, Services.PLATFORM.getTickingLightBlock().defaultBlockState());
        } else if (AzureLibUtil.checkDistance(lightBlockPos, entity.blockPosition(), 2) && entity.level().getBlockEntity(lightBlockPos) instanceof TickingLightEntity tickingLightEntity) {
            tickingLightEntity.refresh(isInWaterBlock ? 20 : 0);
        }
    }

    /**
     * Hitscan between the player and the target. Useful for doing damage
     * TODO: Fix why it doesn't work if going about shoulder level on zombie sized mobs
     *
     * @param livingEntity The Shooter Entity.
     * @param range        The block distance it can fire.
     * @param ticks        The amount of ticks to take, usually will be 1.0f
     * @return returns a EntityHitResult
     */
    public static EntityHitResult hitscanTrace(LivingEntity livingEntity, double range, float ticks) {
        var look = livingEntity.getViewVector(ticks);
        var start = livingEntity.getEyePosition(ticks);
        var end = new Vec3(livingEntity.getX() + look.x * range, livingEntity.getEyeY() + look.y * range, livingEntity.getZ() + look.z * range);
        var traceDistance = livingEntity.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity)).getLocation().distanceToSqr(end);
        for (var possible : livingEntity.level().getEntities(livingEntity, livingEntity.getBoundingBox().expandTowards(look.scale(traceDistance)).expandTowards(3.0D, 3.0D, 3.0D), (entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity))) {
            var clip = possible.getBoundingBox().inflate(0.3D).clip(start, end);
            if (clip.isPresent() && start.distanceToSqr(clip.get()) < traceDistance)
                return ProjectileUtil.getEntityHitResult(livingEntity.level(), livingEntity, start, end, livingEntity.getBoundingBox().expandTowards(look.scale(traceDistance)).inflate(3.0D, 3.0D, 3.0D), target -> !target.isSpectator() && livingEntity.isAttackable() && livingEntity.hasLineOfSight(target));
        }
        return null;
    }

    /**
     * Handles the item reloading.
     *
     * @param user Player who's reloading
     * @param hand Currently only sets the {@link InteractionHand#MAIN_HAND}
     */
    public static void reload(Player user, InteractionHand hand) {
        if (user.getItemInHand(hand).getItem() instanceof BaseGunItem gunItem) {
            while (!user.isCreative() && user.getItemInHand(hand).getDamageValue() != 0 && user.getInventory().countItem(gunItem.gunBuilder.getAmmoItem()) > 0) {
                AzureLibUtil.removeAmmo(gunItem.gunBuilder.getAmmoItem(), user);
                user.getItemInHand(hand).hurtAndBreak(-gunItem.gunBuilder.getReloadAmount(), user, s -> user.broadcastBreakEvent(hand));
                user.getItemInHand(hand).setPopTime(gunItem.gunBuilder.getReloadCooldown());
                if (gunItem.gunBuilder.getReloadSound() != null)
                    user.level().playSound((Player) null, user.getX(), user.getY(), user.getZ(), gunItem.gunBuilder.getReloadSound(), SoundSource.PLAYERS, 1.00F, 1.0F);
            }
        }
    }

    /**
     * Handles calling the reload packet when the player is holding the item and presses {@link ClientUtils#RELOAD} keymap
     * Should be used in {@link Item#inventoryTick(ItemStack, Level, Entity, int, boolean)}
     *
     * @param stack    The item being used
     * @param level    The Level of the entity, should be clientside
     * @param entity   The entity calling the reload packet, should be a Player
     * @param selected The check to see if the player is holding the entity.
     */
    public static void sendReloadPacket(ItemStack stack, Level level, Entity entity, boolean selected, int slot) {
        if (level.isClientSide && entity instanceof Player player && player.getItemInHand(player.getUsedItemHand()).getItem() instanceof BaseGunItem)
            if (ClientUtils.RELOAD.isDown() && selected && !player.getCooldowns().isOnCooldown(stack.getItem())) {
                Services.NETWORK.reloadGun(slot);
            }
    }

    /**
     * Handles setting fire to targets when using the {@link IPlatformHelper#getIncendairyenchament()}
     *
     * @param projectile The Projectile being used
     */
    public static void setOnFire(Projectile projectile) {
        if (projectile.isOnFire())
            projectile.level().getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(2)).forEach(e -> {
                if (e.isAlive() && !(e instanceof Player)) e.setRemainingFireTicks(90);
            });
    }
}
