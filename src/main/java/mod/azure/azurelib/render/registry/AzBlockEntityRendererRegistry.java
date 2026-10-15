package mod.azure.azurelib.render.registry;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.McClient;

/**
 * Registers block entity renderers with Beta's block entity render dispatcher, replacing StationAPI's
 * {@code BlockEntityRendererRegisterEvent}. Works for any renderer, AzureLib's
 * {@link mod.azure.azurelib.render.block.AzBlockEntityRenderer} or a vanilla one.
 * <p>
 * Client only: call it from your {@code client-init} entrypoint, e.g.
 *
 * <pre>{@code
 * AzBlockEntityRendererRegistry.register(ExampleBlockEntity.class, ExampleBlockEntityRenderer::new);
 * }</pre>
 *
 * This only handles rendering. Beta also needs the block entity class itself mapped to a save id (MCP
 * {@code TileEntity.addMapping}), or saving the chunk fails; that is done on both sides, separately.
 */
public final class AzBlockEntityRendererRegistry {

    private static final AzRendererInstaller<BlockEntity, BlockEntityRenderer, BlockEntityRenderDispatcher> INSTALLER =
        new AzRendererInstaller<>(
            "block entity",
            (dispatcher, entry) -> McClient.installBlockEntityRenderer(dispatcher, entry.getKey(), entry.getValue())
        );

    private AzBlockEntityRendererRegistry() {
        throw new UnsupportedOperationException();
    }

    public static void register(
        Class<? extends BlockEntity> blockEntityClass,
        Supplier<? extends BlockEntityRenderer> renderer
    ) {
        INSTALLER.register(blockEntityClass, renderer);
    }

    /** Registers one renderer for several block entity classes; the supplier is called once per class. */
    @SafeVarargs
    public static void register(
        Supplier<? extends BlockEntityRenderer> renderer,
        Class<? extends BlockEntity> blockEntityClass,
        Class<? extends BlockEntity>... blockEntityClasses
    ) {
        register(blockEntityClass, renderer);
        for (Class<? extends BlockEntity> other : blockEntityClasses) {
            register(other, renderer);
        }
    }

    /** The renderer AzureLib installed for exactly this class, or null (vanilla renderers aren't tracked). */
    @Nullable
    public static BlockEntityRenderer getOrNull(Class<? extends BlockEntity> blockEntityClass) {
        return INSTALLER.getOrNull(blockEntityClass);
    }

    /** Internal: called by {@code BlockEntityRenderDispatcherMixin_AzRenderers} when the dispatcher is constructed. */
    public static void onDispatcherCreated(BlockEntityRenderDispatcher dispatcher) {
        INSTALLER.onDispatcherCreated(dispatcher);
    }
}
