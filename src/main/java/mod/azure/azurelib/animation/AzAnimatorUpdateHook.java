package mod.azure.azurelib.animation;

/**
 * Runs every frame just before an animator's controllers update, after Molang queries are bound. Use it to drive
 * controllers from per-frame values, for example setting their weights from the animatable's speed. Register with
 * {@link AzAnimator#addUpdateHook}.
 *
 * @param <T> the animatable type
 */
@FunctionalInterface
public interface AzAnimatorUpdateHook<T> {

    void beforeControllers(T animatable, float partialTicks);
}
