package mod.azure.azurelib.render.registry;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.McClient;

/**
 * Registers entity renderers with Beta's entity render dispatcher, replacing StationAPI's
 * {@code EntityRendererRegisterEvent}. Works for any renderer, AzureLib's
 * {@link mod.azure.azurelib.render.entity.AzEntityRenderer} or a vanilla one.
 * <p>
 * Client only: call it from your {@code client-init} entrypoint, e.g.
 *
 * <pre>{@code
 * AzEntityRendererRegistry.register(ExampleEntity.class, ExampleEntityRenderer::new);
 * }</pre>
 *
 * Calling it before or after the dispatcher exists both work. The renderer is created once, and Beta looks renderers up
 * by exact class first and then by superclass, so registering a base class also covers its subclasses that have no
 * renderer of their own.
 */
public final class AzEntityRendererRegistry {

    private static final AzRendererInstaller<Entity, EntityRenderer, EntityRenderDispatcher> INSTALLER =
        new AzRendererInstaller<>(
            "entity",
            (dispatcher, entry) -> McClient.installEntityRenderer(dispatcher, entry.getKey(), entry.getValue())
        );

    private AzEntityRendererRegistry() {
        throw new UnsupportedOperationException();
    }

    public static void register(Class<? extends Entity> entityClass, Supplier<? extends EntityRenderer> renderer) {
        INSTALLER.register(entityClass, renderer);
    }

    /** Registers one renderer for several entity classes; the supplier is called once per class. */
    @SafeVarargs
    public static void register(
        Supplier<? extends EntityRenderer> renderer,
        Class<? extends Entity> entityClass,
        Class<? extends Entity>... entityClasses
    ) {
        register(entityClass, renderer);
        for (Class<? extends Entity> other : entityClasses) {
            register(other, renderer);
        }
    }

    /** The renderer AzureLib installed for exactly this class, or null (vanilla renderers aren't tracked). */
    @Nullable
    public static EntityRenderer getOrNull(Class<? extends Entity> entityClass) {
        return INSTALLER.getOrNull(entityClass);
    }

    /** Internal: called by {@code EntityRenderDispatcherMixin_AzRenderers} when the dispatcher is constructed. */
    public static void onDispatcherCreated(EntityRenderDispatcher dispatcher) {
        INSTALLER.onDispatcherCreated(dispatcher);
    }
}
