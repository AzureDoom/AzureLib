package mod.azure.azurelib.render;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.cache.AzBakedModelCache;

/**
 * The {@code AzProvider} class serves as a utility for providing animation-related resources, such as baked models and
 * animators for animatable objects of type {@code T}. This class facilitates the dynamic retrieval and caching of
 * resources to enhance performance during runtime and minimize redundant resource generation.
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> The type of the animatable object this provider works with (e.g., an entity, block, or item).
 */
public class AzProvider<K, T> {

    protected final Supplier<AzAnimator<K, T>> animatorSupplier;

    protected final BiFunction<Entity, T, ResourceLocation> modelLocationProvider;

    protected final Function<T, K> UUIDProvider;

    public AzProvider(
        Supplier<AzAnimator<K, T>> animatorSupplier,
        BiFunction<Entity, T, ResourceLocation> modelLocationProvider,
        Function<T, K> UUIDProvider
    ) {
        this.animatorSupplier = animatorSupplier;
        this.modelLocationProvider = modelLocationProvider;
        this.UUIDProvider = UUIDProvider;
    }

    /**
     * Provides a baked model associated with the specified animatable object. This method retrieves the model resource
     * location for the animatable object using the configured model location provider, then fetches the corresponding
     * baked model from the {@link AzBakedModelCache}.
     *
     * @param animatable the animatable object for which the baked model should be retrieved, must not be null
     * @return the baked model associated with the animatable object, or null if no model is found
     */
    public @Nullable AzBakedModel provideBakedModel(@Nullable Entity entity, @NotNull T animatable) {
        var modelLocation = modelLocationProvider.apply(entity, animatable);
        var shared = AzBakedModelCache.getInstance().getNullable(modelLocation);
        if (shared == null)
            return AzBakedModel.getDefault();

        var animator = AzAnimatorAccessor.getOrNull(animatable);
        if (animator == null)
            return shared;
        var ctx = animator.context();

        var cache = ctx.boneCache();
        if (cache == null || cache.isEmpty())
            return shared;

        return cache.getBakedModel();
    }

    /**
     * Provides an {@link AzAnimator} instance associated with the given animatable object. If the animator is not
     * already cached, this method will create a new animator, register its controllers, and cache it for future use.
     *
     * @param animatable the animatable object for which the animator should be provided
     * @return an {@link AzAnimator} instance associated with the animatable object, or null if the animator could not
     *         be created or retrieved
     */
    public @Nullable AzAnimator<K, T> provideAnimator(@Nullable Entity entity, T animatable) {
        var accessor = AzAnimatorAccessor.<K, T>cast(animatable);
        var cachedAnimator = accessor.getAnimatorOrNull();

        var modelLocation = modelLocationProvider.apply(entity, animatable);
        var shared = AzBakedModelCache.getInstance().getNullable(modelLocation);

        if (cachedAnimator == null) {
            cachedAnimator = animatorSupplier.get();
            if (cachedAnimator != null) {
                var ctx = cachedAnimator.getOrCreateContext(UUIDProvider.apply(animatable));

                if (shared != null) {
                    ctx.boneCache().setActiveModel(shared);
                }

                cachedAnimator.registerControllers(cachedAnimator.getAnimationControllerContainer());
                accessor.setAnimator(cachedAnimator);
            }
        } else {
            var ctx = cachedAnimator.context();
            if (ctx != null && shared != null) {
                var baked = ctx.boneCache().getBakedModel();

                if (!baked.getModelUUID().equals(shared.getModelUUID())) {
                    ctx.boneCache().setActiveModel(shared);
                    // Recache the animator on model change
                    cachedAnimator = cacheAnimator(accessor, shared, animatable);
                }
                // TODO: Their might be cases where the animator changes without the model changing
            }
        }

        return cachedAnimator;
    }

    /***
     * Returns an {@link AzAnimator} based of accessor and the current animation supplier
     *
     * @return AzAnimator returns itself again
     */
    private AzAnimator<K, T> cacheAnimator(AzAnimatorAccessor<K, T> accessor, AzBakedModel shared, T animatable) {
        var cachedAnimator = animatorSupplier.get();
        if (cachedAnimator != null) {
            var ctx = cachedAnimator.getOrCreateContext(UUIDProvider.apply(animatable));

            if (shared != null) {
                ctx.boneCache().setActiveModel(shared);
            }

            cachedAnimator.registerControllers(cachedAnimator.getAnimationControllerContainer());
            accessor.setAnimator(cachedAnimator);
        }
        return cachedAnimator;
    }
}
