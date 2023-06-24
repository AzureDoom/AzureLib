package mod.azure.azurelib.util;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.InstancedAnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.EasingType;
import mod.azure.azurelib.loading.object.BakedModelFactory;
import mod.azure.azurelib.network.SerializableDataTicket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Helper class for various AzureLib-specific functions.
 */
public final class AzureLibUtil {
	/**
	 * Creates a new AnimatableInstanceCache for the given animatable object
	 * 
	 * @param animatable The animatable object
	 */
	public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable) {
		AnimatableInstanceCache cache = animatable.animatableCacheOverride();

		return cache != null ? cache : createInstanceCache(animatable, !(animatable instanceof Entity) && !(animatable instanceof BlockEntity));
	}

	/**
	 * Creates a new AnimatableInstanceCache for the given animatable object. <br>
	 * Recommended to use {@link AzureLibUtil#createInstanceCache(GeoAnimatable)} unless you know what you're doing.
	 * 
	 * @param animatable      The animatable object
	 * @param singletonObject Whether the object is a singleton/flyweight object, and uses ints to differentiate animatable instances
	 */
	public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable, boolean singletonObject) {
		AnimatableInstanceCache cache = animatable.animatableCacheOverride();

		if (cache != null)
			return cache;

		return singletonObject ? new SingletonAnimatableInstanceCache(animatable) : new InstancedAnimatableInstanceCache(animatable);
	}

	/**
	 * Register a custom {@link mod.azure.azurelib.core.animation.Animation.LoopType} with AzureLib, allowing for dynamic handling of post-animation looping.<br>
	 * <b><u>MUST be called during mod construct</u></b><br>
	 * 
	 * @param name     The name of the {@code LoopType} handler
	 * @param loopType The {@code LoopType} implementation to use for the given name
	 */
	synchronized public static Animation.LoopType addCustomLoopType(String name, Animation.LoopType loopType) {
		return Animation.LoopType.register(name, loopType);
	}

	/**
	 * Register a custom {@link mod.azure.azurelib.core.animation.EasingType} with AzureLib, allowing for dynamic handling of animation transitions and curves.<br>
	 * <b><u>MUST be called during mod construct</u></b><br>
	 * 
	 * @param name       The name of the {@code EasingType} handler
	 * @param easingType The {@code EasingType} implementation to use for the given name
	 */
	synchronized public static EasingType addCustomEasingType(String name, EasingType easingType) {
		return EasingType.register(name, easingType);
	}

	/**
	 * Register a custom {@link mod.azure.azurelib.loading.object.BakedModelFactory} with AzureLib, allowing for dynamic handling of geo model loading.<br>
	 * <b><u>MUST be called during mod construct</u></b><br>
	 * 
	 * @param namespace The namespace (modid) to register the factory for
	 * @param factory   The factory responsible for model loading under the given namespace
	 */
	synchronized public static void addCustomBakedModelFactory(String namespace, BakedModelFactory factory) {
		BakedModelFactory.register(namespace, factory);
	}

	/**
	 * Register a custom {@link SerializableDataTicket} with AzureLib for handling custom data transmission.<br>
	 * NOTE: You do not need to register non-serializable {@link mod.azure.azurelib.core.object.DataTicket DataTickets}.
	 * 
	 * @param dataTicket The SerializableDataTicket to register
	 * @return The dataTicket you passed in
	 */
	synchronized public static <D> SerializableDataTicket<D> addDataTicket(SerializableDataTicket<D> dataTicket) {
		return DataTickets.registerSerializable(dataTicket);
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
	public void summonAoE(LivingEntity entity, ParticleOptions particle, int yOffset, int duration, float radius, boolean hasEffect, @Nullable MobEffect effect, int effectTime) {
		var areaEffectCloudEntity = new AreaEffectCloud(entity.getLevel(), entity.getX(), entity.getY() + yOffset, entity.getZ());
		areaEffectCloudEntity.setRadius(radius);
		areaEffectCloudEntity.setDuration(duration);
		areaEffectCloudEntity.setParticle(particle);
		areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
		if (hasEffect == true)
			if (!entity.hasEffect(effect))
				areaEffectCloudEntity.addEffect(new MobEffectInstance(effect, effectTime, 0));
		entity.getLevel().addFreshEntity(areaEffectCloudEntity);
	}
}
