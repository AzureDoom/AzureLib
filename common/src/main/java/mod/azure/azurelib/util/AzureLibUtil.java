package mod.azure.azurelib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.blocks.TickingLightEntity;
import mod.azure.azurelib.registry.AzureBlocksRegistry;

/**
 * Helper class for various AzureLib-specific functions.
 */
public final class AzureLibUtil {

    public static <T> T self(Object object) {
        return (T) object;
    }

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
    public static void summonAoE(
        LivingEntity entity,
        ParticleOptions particle,
        int yOffset,
        int duration,
        float radius,
        boolean hasEffect,
        @Nullable MobEffect effect,
        int effectTime
    ) {
        var areaEffectCloudEntity = new AreaEffectCloud(
            entity.level(),
            entity.getX(),
            entity.getY() + yOffset,
            entity.getZ()
        );
        areaEffectCloudEntity.setRadius(radius);
        areaEffectCloudEntity.setDuration(duration);
        areaEffectCloudEntity.setParticle(particle);
        areaEffectCloudEntity.setRadiusPerTick(
            -areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration()
        );
        if (hasEffect == true)
            if (!entity.hasEffect(effect))
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
            lightBlockPos = AzureLibUtil.findFreeSpace(entity.level(), entity.blockPosition());
            if (lightBlockPos == null)
                return;
            entity.level()
                .setBlockAndUpdate(
                    lightBlockPos,
                    AzureBlocksRegistry.TICKING_LIGHT_BLOCK.get().defaultBlockState()
                );
        } else if (
            AzureLibUtil.checkDistance(lightBlockPos, entity.blockPosition()) && entity.level()
                .getBlockEntity(
                    lightBlockPos
                ) instanceof TickingLightEntity tickingLightEntity
        ) {
            tickingLightEntity.refresh(isInWaterBlock ? 20 : 0);
        }
    }

    private static boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB) {
        return Math.abs(blockPosA.getX() - blockPosB.getX()) <= 2 && Math.abs(
            blockPosA.getY() - blockPosB.getY()
        ) <= 2 && Math.abs(blockPosA.getZ() - blockPosB.getZ()) <= 2;
    }

    private static BlockPos findFreeSpace(Level world, BlockPos blockPos) {
        if (blockPos == null)
            return null;

        var offsets = new int[2 * 2 + 1];
        offsets[0] = 0;
        for (var i = 2; i <= 2 * 2; i += 2) {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (var x : offsets)
            for (var y : offsets)
                for (var z : offsets) {
                    var offsetPos = blockPos.offset(x, y, z);
                    var state = world.getBlockState(offsetPos);
                    if (
                        state.isAir() || state.getBlock()
                            .equals(
                                AzureBlocksRegistry.TICKING_LIGHT_BLOCK.get()
                            )
                    )
                        return offsetPos;
                }
        return null;
    }
}
