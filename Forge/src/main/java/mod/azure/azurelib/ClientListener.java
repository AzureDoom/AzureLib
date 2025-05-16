package mod.azure.azurelib;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import mod.azure.azurelib.client.AzureLibClient;
import mod.azure.azurelib.config.ConfigHolder;

@EventBusSubscriber(modid = AzureLib.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientListener {

    @SubscribeEvent
    public static void registerKeys(final FMLClientSetupEvent event) {
        Keybindings.RELOAD = new KeyBinding("key.azurelib.reload", GLFW.GLFW_KEY_INSERT, "category.azurelib.binds");
        ClientRegistry.registerKeyBinding(Keybindings.RELOAD);
    }

    @SubscribeEvent
    public static void clientInit(final FMLClientSetupEvent event) {
        Map<String, List<ConfigHolder<?>>> groups = ConfigHolder.getConfigGroupingByGroup();
        ModList modList = ModList.get();
        for (Map.Entry<String, List<ConfigHolder<?>>> entry : groups.entrySet()) {
            String modId = entry.getKey();
            Optional<? extends ModContainer> optional = modList.getModContainerById(modId);
            optional.ifPresent(modContainer -> {
                List<ConfigHolder<?>> list = entry.getValue();
                modContainer.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, previousScreen) -> {
                    if (list.size() == 1) {
                        return AzureLibClient.getConfigScreen(list.get(0).getConfigId(), previousScreen);
                    }
                    return AzureLibClient.getConfigScreenByGroup(list, modId, previousScreen);
                });
            });
        }
    }
}
