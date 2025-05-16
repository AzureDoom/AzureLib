package mod.azure.azurelib.rewrite.animation.controller;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Collection;
import java.util.Map;

/**
 * A container class for managing a collection of {@link AzAnimationController} instances. Provides methods to add,
 * retrieve, and access animation controllers by their names.
 *
 * @param <T> the type of the animation data or state managed by {@link AzAnimationController}.
 */
public class AzAnimationControllerContainer<T> {

    private final Map<String, AzAnimationController<T>> animationControllersByName;

    public AzAnimationControllerContainer() {
        this.animationControllersByName = new Object2ObjectArrayMap<>();
    }

    @SafeVarargs
    public final void add(AzAnimationController<T> controller, AzAnimationController<T>... controllers) {
        animationControllersByName.put(controller.name(), controller);

        for (var extraController : controllers) {
            animationControllersByName.put(extraController.name(), extraController);
        }
    }

    public AzAnimationController<T> getOrNull(String controllerName) {
        return animationControllersByName.get(controllerName);
    }

    public Collection<AzAnimationController<T>> getAll() {
        return animationControllersByName.values();
    }
}
