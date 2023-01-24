package mod.azure.azurelib.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.EasingType;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.InstancedAnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.loading.object.BakedModelFactory;
import mod.azure.azurelib.network.SerializableDataTicket;

/**
 * Helper class for various AzureLib-specific functions.
 */
public final class AzureLibUtil {
	/**
	 * Creates a new AnimatableInstanceCache for the given animatable object
	 * @param animatable The animatable object
	 */
	public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable) {
		return createInstanceCache(animatable, !(animatable instanceof Entity) && !(animatable instanceof BlockEntity));
	}

	/**
	 * Creates a new AnimatableInstanceCache for the given animatable object. <br>
	 * Recommended to use {@link AzureLibUtil#createInstanceCache(GeoAnimatable)} unless you know what you're doing.
	 * @param animatable The animatable object
	 * @param singletonObject Whether the object is a singleton/flyweight object, and uses ints to differentiate animatable instances
	 */
	public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable, boolean singletonObject) {
		return singletonObject ? new SingletonAnimatableInstanceCache(animatable) : new InstancedAnimatableInstanceCache(animatable);
	}

	/**
	 * Register a custom {@link mod.azure.azurelib.core.animation.Animation.LoopType} with AzureLib,
	 * allowing for dynamic handling of post-animation looping.<br>
	 * <b><u>MUST be called during mod construct</u></b><br>
	 * @param name The name of the {@code LoopType} handler
	 * @param loopType The {@code LoopType} implementation to use for the given name
	 */
	synchronized public static Animation.LoopType addCustomLoopType(String name, Animation.LoopType loopType) {
		return Animation.LoopType.register(name, loopType);
	}

	/**
	 * Register a custom {@link mod.azure.azurelib.core.animation.EasingType} with AzureLib,
	 * allowing for dynamic handling of animation transitions and curves.<br>
	 * <b><u>MUST be called during mod construct</u></b><br>
	 * @param name The name of the {@code EasingType} handler
	 * @param easingType The {@code EasingType} implementation to use for the given name
	 */
	synchronized public static EasingType addCustomEasingType(String name, EasingType easingType) {
		return EasingType.register(name, easingType);
	}

	/**
	 * Register a custom {@link mod.azure.azurelib.loading.object.BakedModelFactory} with AzureLib,
	 * allowing for dynamic handling of geo model loading.<br>
	 * <b><u>MUST be called during mod construct</u></b><br>
	 * @param namespace The namespace (modid) to register the factory for
	 * @param factory The factory responsible for model loading under the given namespace
	 */
	synchronized public static void addCustomBakedModelFactory(String namespace, BakedModelFactory factory) {
		BakedModelFactory.register(namespace, factory);
	}

	/**
	 * Register a custom {@link SerializableDataTicket} with AzureLib for handling custom data transmission.<br>
	 * NOTE: You do not need to register non-serializable {@link mod.azure.azurelib.core.object.DataTicket DataTickets}.
	 * @param dataTicket The SerializableDataTicket to register
	 * @return The dataTicket you passed in
	 */
	synchronized public static <D> SerializableDataTicket<D> addDataTicket(SerializableDataTicket<D> dataTicket) {
		return DataTickets.registerSerializable(dataTicket);
	}
}
