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

    private final Supplier<AzAnimator<K, T>> animatorSupplier;

    private final BiFunction<Entity, T, ResourceLocation> modelLocationProvider;

    private final Function<T, K> UUIDProvider;

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
        // Always have a safe fallback
        var modelLocation = modelLocationProvider.apply(entity, animatable);
        var shared = AzBakedModelCache.getInstance().getNullable(modelLocation);

        if (shared == null) {
            return AzBakedModel.getDefault();
        }

        // Try to return the per-instance model if an animator/context already exists
        var animator = AzAnimatorAccessor.getOrNull(animatable);
        if (animator == null) {
            return shared; // <- avoid NPE: animator not created yet
        }

        var ctx = animator.context();
        if (ctx == null) {
            return shared; // <- avoid NPE: context not set yet this frame
        }

        var cache = ctx.boneCache();
        if (cache == null || cache.isEmpty()) {
            return shared; // <- cache isn't initialized yet
        }

        return cache.getBakedModel(); // <- the deep-copied, per-instance model
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
        // TODO: Instead of caching the entire animator itself, we're going to want to cache the relevant data for the
        // entity.
        var accessor = AzAnimatorAccessor.<K, T>cast(animatable);
        var cachedAnimator = accessor.getAnimatorOrNull();

        if (cachedAnimator == null) {
            cachedAnimator = animatorSupplier.get();
            if (cachedAnimator != null) {
                // Create a per-instance context now
                var ctx = cachedAnimator.getOrCreateContext(UUIDProvider.apply(animatable));

                // Install a deep-copied model into the bone cache BEFORE controllers
                var modelLocation = modelLocationProvider.apply(entity, animatable);
                var shared = AzBakedModelCache.getInstance().getNullable(modelLocation);
                if (shared != null) {
                    ctx.boneCache().setActiveModel(shared); // setActiveModel deep-copies internally
                }

                // Controllers see a ready context & model
                cachedAnimator.registerControllers(cachedAnimator.getAnimationControllerContainer());
                accessor.setAnimator(cachedAnimator);
            }
        }

        return cachedAnimator;
    }
}
