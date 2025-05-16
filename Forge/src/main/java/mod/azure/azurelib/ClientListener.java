package mod.azure.azurelib;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
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
        Keybindings.RELOAD = new KeyMapping("key.azurelib.reload", GLFW.GLFW_KEY_R, "category.azurelib.binds");
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
                modContainer.registerExtensionPoint(
                    ConfigGuiHandler.ConfigGuiFactory.class,
                    () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> {
                        if (list.size() == 1) {
                            return AzureLibClient.getConfigScreen(list.get(0).getConfigId(), screen);
                        }
                        return AzureLibClient.getConfigScreenByGroup(list, modId, screen);
                    })
                );
            });
        }
    }
}
