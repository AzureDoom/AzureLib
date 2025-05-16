package mod.azure.azurelib.rewrite.render;

import net.minecraft.util.ResourceLocation;

import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.model.cache.AzBakedModelCache;

/**
 * The {@code AzProvider} class serves as a utility for providing animation-related resources, such as baked models and
 * animators for animatable objects of type {@code T}. This class facilitates the dynamic retrieval and caching of
 * resources to enhance performance during runtime and minimize redundant resource generation.
 *
 * @param <T> The type of the animatable object this provider works with (e.g., an entity, block, or item).
 */
public class AzProvider<T> {

    private final Supplier<AzAnimator<T>> animatorSupplier;

    private final Function<T, ResourceLocation> modelLocationProvider;

    public AzProvider(
        Supplier<AzAnimator<T>> animatorSupplier,
        Function<T, ResourceLocation> modelLocationProvider
    ) {
        this.animatorSupplier = animatorSupplier;
        this.modelLocationProvider = modelLocationProvider;
    }

    /**
     * Provides a baked model associated with the specified animatable object. This method retrieves the model resource
     * location for the animatable object using the configured model location provider, then fetches the corresponding
     * baked model from the {@link AzBakedModelCache}.
     *
     * @param animatable the animatable object for which the baked model should be retrieved, must not be null
     * @return the baked model associated with the animatable object, or null if no model is found
     */
    public AzBakedModel provideBakedModel(T animatable) {
        ResourceLocation modelResourceLocation = modelLocationProvider.apply(animatable);
        return AzBakedModelCache.getInstance().getNullable(modelResourceLocation);
    }

    /**
     * Provides an {@link AzAnimator} instance associated with the given animatable object. If the animator is not
     * already cached, this method will create a new animator, register its controllers, and cache it for future use.
     *
     * @param animatable the animatable object for which the animator should be provided
     * @return an {@link AzAnimator} instance associated with the animatable object, or null if the animator could not
     *         be created or retrieved
     */
    public AzAnimator<T> provideAnimator(T animatable) {
        // TODO: Instead of caching the entire animator itself, we're going to want to cache the relevant data for the
        // entity.
        AzAnimatorAccessor<T> accessor = AzAnimatorAccessor.cast(animatable);

        AzAnimator<T> cachedAnimator = accessor.getAnimatorOrNull();

        if (cachedAnimator == null) {
            // If the cached animator is null, create a new one. We use a separate reference here just for some
            cachedAnimator = animatorSupplier.get();

            if (cachedAnimator != null) {
                // If the new animator we created is not null, then register its controllers.
                cachedAnimator.registerControllers(
                    cachedAnimator.getAnimationControllerContainer()
                );
                // Also cache the animator so that the next time we fetch the animator, it's ready for us.
                accessor.setAnimator(cachedAnimator);
            }
        }

        return cachedAnimator;
    }
}
