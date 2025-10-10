package mod.azure.azurelib.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.AzureLibClient;
import mod.azure.azurelib.common.config.ConfigHolder;
import mod.azure.azurelib.common.config.ConfigHolderRegistry;

@EventBusSubscriber(modid = AzureLib.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModListener {

    @SubscribeEvent
    public static void clientInit(final FMLClientSetupEvent event) {
        Map<String, List<ConfigHolder<?>>> groups = ConfigHolderRegistry.getConfigGroupingByGroup();
        ModList modList = ModList.get();
        for (Map.Entry<String, List<ConfigHolder<?>>> entry : groups.entrySet()) {
            String modId = entry.getKey();
            Optional<? extends ModContainer> optional = modList.getModContainerById(modId);
            optional.ifPresent(modContainer -> {
                List<ConfigHolder<?>> list = entry.getValue();
                modContainer.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (Supplier<IConfigScreenFactory>) () -> (container, screen) -> {
                        if (list.size() == 1) {
                            return AzureLibClient.getConfigScreen(list.get(0).getConfigId(), screen);
                        }
                        return AzureLibClient.getConfigScreenByGroup(list, modId, screen);
                    }
                );
            });
        }
    }
}
