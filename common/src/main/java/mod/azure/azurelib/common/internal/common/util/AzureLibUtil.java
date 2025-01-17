/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mod.azure.azurelib.common.internal.common.constant.DataTickets;
import mod.azure.azurelib.common.internal.common.loading.object.BakedModelFactory;
import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import mod.azure.azurelib.common.internal.common.registry.AzureBlocksRegistry;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.InstancedAnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.EasingType;
import mod.azure.azurelib.core.object.DataTicket;

/**
 * Helper class for various AzureLib-specific functions.
 */
public record AzureLibUtil() {

    public static <T> T self(Object object) {
        return (T) object;
    }

    /**
     * Creates a new AnimatableInstanceCache for the given animatable object
     *
     * @param animatable The animatable object
     */
    public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable) {
        AnimatableInstanceCache cache = animatable.animatableCacheOverride();

        return cache != null
            ? cache
            : createInstanceCache(
                animatable,
                !(animatable instanceof Entity) && !(animatable instanceof BlockEntity)
            );
    }

    /**
     * Creates a new AnimatableInstanceCache for the given animatable object. <br>
     * Recommended to use {@link AzureLibUtil#createInstanceCache(GeoAnimatable)} unless you know what you're doing.
     *
     * @param animatable      The animatable object
     * @param singletonObject Whether the object is a singleton/flyweight object, and uses ints to differentiate
     *                        animatable instances
     */
    public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable, boolean singletonObject) {
        AnimatableInstanceCache cache = animatable.animatableCacheOverride();

        if (cache != null)
            return cache;

        return singletonObject
            ? new SingletonAnimatableInstanceCache(
                animatable
            )
            : new InstancedAnimatableInstanceCache(animatable);
    }

    /**
     * Register a custom {@link Animation.LoopType} with AzureLib, allowing for dynamic handling of post-animation
     * looping.<br>
     * <b><u>MUST be called during mod construct</u></b><br>
     *
     * @param name     The name of the {@code LoopType} handler
     * @param loopType The {@code LoopType} implementation to use for the given name
     */
    public static synchronized Animation.LoopType addCustomLoopType(String name, Animation.LoopType loopType) {
        return Animation.LoopType.register(name, loopType);
    }

    /**
     * Register a custom {@link EasingType} with AzureLib, allowing for dynamic handling of animation transitions and
     * curves.<br>
     * <b><u>MUST be called during mod construct</u></b><br>
     *
     * @param name       The name of the {@code EasingType} handler
     * @param easingType The {@code EasingType} implementation to use for the given name
     */
    public static synchronized EasingType addCustomEasingType(String name, EasingType easingType) {
        return EasingType.register(name, easingType);
    }

    /**
     * Register a custom {@link BakedModelFactory} with AzureLib, allowing for dynamic handling of geo model
     * loading.<br>
     * <b><u>MUST be called during mod construct</u></b><br>
     *
     * @param namespace The namespace (modid) to register the factory for
     * @param factory   The factory responsible for model loading under the given namespace
     */
    public static synchronized void addCustomBakedModelFactory(String namespace, BakedModelFactory factory) {
        BakedModelFactory.register(namespace, factory);
    }

    /**
     * Register a custom {@link SerializableDataTicket} with AzureLib for handling custom data transmission.<br>
     * NOTE: You do not need to register non-serializable {@link DataTicket DataTickets}.
     *
     * @param dataTicket The SerializableDataTicket to register
     * @return The dataTicket you passed in
     */
    public static synchronized <D> SerializableDataTicket<D> addDataTicket(SerializableDataTicket<D> dataTicket) {
        return DataTickets.registerSerializable(dataTicket);
    }

    public static boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB, int distance) {
        return Math.abs(blockPosA.getX() - blockPosB.getX()) <= distance && Math.abs(
            blockPosA.getY() - blockPosB.getY()
        ) <= distance && Math.abs(
            blockPosA.getZ() - blockPosB.getZ()
        ) <= distance;
    }

    public static BlockPos findFreeSpace(Level world, BlockPos blockPos, int maxDistance) {
        if (blockPos == null)
            return null;

        var offsets = new int[maxDistance * 2 + 1];
        offsets[0] = 0;
        for (var i = 2; i <= maxDistance * 2; i += 2) {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (var x : offsets)
            for (var y : offsets)
                for (var z : offsets) {
                    var offsetPos = blockPos.offset(x, y, z);
                    var state = world.getBlockState(offsetPos);
                    if (state.isAir() || state.getBlock().equals(AzureBlocksRegistry.TICKING_LIGHT_BLOCK.get()))
                        return offsetPos;
                }
        return null;
    }
}
