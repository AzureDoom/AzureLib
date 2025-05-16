package mod.azure.azurelib.rewrite.animation;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The {@code AzAnimatorAccessor} interface provides a mechanism to associate and manage an {@link AzAnimator} instance
 * with a target object. This enables retrieval and manipulation of animator instances that are specific to the target
 * object.
 *
 * @param <T> The type of the target object that the animator applies to.
 */
public interface AzAnimatorAccessor<T> {

    @Nullable
    AzAnimator<T> getAnimatorOrNull();

    void setAnimator(AzAnimator<T> animator);

    default Optional<AzAnimator<T>> getAnimator() {
        return Optional.ofNullable(getAnimatorOrNull());
    }

    @SuppressWarnings("unchecked")
    static <T> AzAnimatorAccessor<T> cast(T target) {
        return (AzAnimatorAccessor<T>) target;
    }

    static <T> AzAnimator<T> getOrNull(T target) {
        return cast(target).getAnimatorOrNull();
    }

    static <T> Optional<AzAnimator<T>> get(T target) {
        return Optional.ofNullable(getOrNull(target));
    }
}
