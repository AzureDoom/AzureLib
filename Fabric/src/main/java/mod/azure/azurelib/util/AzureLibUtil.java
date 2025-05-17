package mod.azure.azurelib.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for various AzureLib-specific functions.
 */
public final class AzureLibUtil {

    public static <T> T self(Object object) {
        return (T) object;
    }

    public static boolean isMultipleOf(int p_265754_, int p_265543_) {
        return p_265754_ % p_265543_ == 0;
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
        AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(
            entity.level,
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
        if (hasEffect && !entity.hasEffect(effect))
            areaEffectCloudEntity.addEffect(new MobEffectInstance(effect, effectTime, 0));
        entity.level.addFreshEntity(areaEffectCloudEntity);
    }
}
