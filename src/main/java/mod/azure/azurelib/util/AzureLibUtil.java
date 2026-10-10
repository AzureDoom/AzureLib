package mod.azure.azurelib.util;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;

import javax.annotation.Nullable;

/**
 * Helper class for various methods and functions useful while using AzureLib.
 */
public final class AzureLibUtil {

    private AzureLibUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Cast the given object to the type of the receiver. Primarily used in mixins, where the compiler can't know that
     * the mixin is the target class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T self(Object object) {
        return (T) object;
    }

    public static boolean isMultipleOf(int value, int divisor) {
        return value % divisor == 0;
    }

    /**
     * Returns the stack's NBT compound, creating and attaching an empty one if absent. 1.12.2 has no
     * {@code ItemStack#getOrCreateTag()}.
     */
    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    /**
     * Summons an Area of Effect Cloud with the set particle, y offset, radius, duration, and effect options.
     *
     * @param entity     The Entity summoning the AoE
     * @param particle   The particle the AoE uses
     * @param yOffset    How offset from the entity's Y position the AoE spawns
     * @param duration   How long the AoE lasts in ticks
     * @param radius     The radius of the AoE
     * @param hasEffect  Whether the AoE applies an effect
     * @param effect     The effect to apply, if {@code hasEffect} is set
     * @param effectTime How long the effect lasts in ticks
     */
    public static void summonAoE(
        EntityLivingBase entity,
        EnumParticleTypes particle,
        int yOffset,
        int duration,
        float radius,
        boolean hasEffect,
        @Nullable Potion effect,
        int effectTime
    ) {
        EntityAreaEffectCloud areaEffectCloudEntity = new EntityAreaEffectCloud(
            entity.world,
            entity.posX,
            entity.posY + yOffset,
            entity.posZ
        );
        areaEffectCloudEntity.setRadius(radius);
        areaEffectCloudEntity.setDuration(duration);
        areaEffectCloudEntity.setParticle(particle);
        areaEffectCloudEntity.setRadiusPerTick(
            -areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration()
        );
        if (hasEffect && effect != null && !entity.isPotionActive(effect)) {
            areaEffectCloudEntity.addEffect(new PotionEffect(effect, effectTime, 0));
        }
        entity.world.spawnEntity(areaEffectCloudEntity);
    }
}
