package mod.azure.azurelib.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.ConfigHolderRegistry;
import mod.azure.azurelib.config.client.ConfigurationClient;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.render.item.AzItemSpecialRenderer;

@EventBusSubscriber(value = Dist.CLIENT, modid = AzureLib.MOD_ID)
public class ClientListener {

    @SubscribeEvent
    public static void registerConfigScreens(final FMLClientSetupEvent event) {
        var groups = ConfigHolderRegistry.getConfigGroupingByGroup();
        var modList = ModList.get();
        for (Map.Entry<String, List<ConfigHolder<?>>> entry : groups.entrySet()) {
            var modId = entry.getKey();
            var holders = entry.getValue();
            var container = modList.getModContainerById(modId);
            container.ifPresent(
                modContainer -> modContainer.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (Supplier<IConfigScreenFactory>) () -> (mod, parent) -> Objects.requireNonNull(
                        ConfigurationClient.getScreenForGroup(
                            holders,
                            modId,
                            parent
                        )
                    )
                )
            );
        }
    }

    @SubscribeEvent
    public static void registerReloadListeners(final AddClientReloadListenersEvent event) {
        event.addListener(AzureLib.modResource("models"), new AzureLibCache());
    }

    @SubscribeEvent
    public static void registerSpecialModelRenderers(
        RegisterSpecialModelRendererEvent event
    ) {
        event.register(
            AzureLib.modResource("azurelib"),
            AzItemSpecialRenderer.Unbaked.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void registerClientPayloadHandlers(final RegisterClientPayloadHandlersEvent event) {
        event.register(AzEntityDispatchCommandPacket.TYPE, (msg, ctx) -> msg.handle());
        event.register(AzItemStackDispatchCommandPacket.TYPE, (msg, ctx) -> msg.handle());
        event.register(AzBlockEntityDispatchCommandPacket.TYPE, (msg, ctx) -> msg.handle());
    }

}
