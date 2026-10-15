package mod.azure.azurelib.render.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Shared bookkeeping for AzureLib's renderer registries.
 * <p>
 * Beta's render dispatchers are singletons created during client startup, and mods may register renderers before or
 * after that happens. Registrations made before the dispatcher exists are queued and installed when it is constructed;
 * registrations made afterwards are installed immediately. Renderers are created from their suppliers at install time,
 * on the client thread.
 *
 * @param <K> the class key type (entity or block entity class)
 * @param <R> the vanilla renderer type
 * @param <D> the vanilla dispatcher type
 */
final class AzRendererInstaller<K, R, D> {

    private final String kind;

    private final BiConsumer<D, Map.Entry<Class<? extends K>, R>> installer;

    private final Map<Class<? extends K>, Supplier<? extends R>> pending = new LinkedHashMap<>();

    private final Map<Class<? extends K>, R> installed = new LinkedHashMap<>();

    @Nullable
    private D dispatcher;

    AzRendererInstaller(String kind, BiConsumer<D, Map.Entry<Class<? extends K>, R>> installer) {
        this.kind = kind;
        this.installer = installer;
    }

    synchronized void register(Class<? extends K> type, Supplier<? extends R> rendererSupplier) {
        if (type == null || rendererSupplier == null) {
            throw new IllegalArgumentException("Cannot register a null " + kind + " class or renderer");
        }
        if (this.dispatcher == null) {
            this.pending.put(type, rendererSupplier);
        } else {
            install(this.dispatcher, type, rendererSupplier);
        }
    }

    /** Called from the dispatcher's constructor mixin. Installs everything registered so far. */
    synchronized void onDispatcherCreated(D dispatcher) {
        this.dispatcher = dispatcher;
        for (Map.Entry<Class<? extends K>, Supplier<? extends R>> entry : this.pending.entrySet()) {
            install(dispatcher, entry.getKey(), entry.getValue());
        }
        this.pending.clear();
    }

    @Nullable
    synchronized R getOrNull(Class<? extends K> type) {
        return this.installed.get(type);
    }

    private void install(D dispatcher, Class<? extends K> type, Supplier<? extends R> rendererSupplier) {
        R renderer = rendererSupplier.get();
        if (renderer == null) {
            throw new IllegalStateException("Renderer supplier for " + kind + " " + type.getName() + " returned null");
        }
        this.installed.put(type, renderer);
        this.installer.accept(dispatcher, new java.util.AbstractMap.SimpleImmutableEntry<>(type, renderer));
    }
}
