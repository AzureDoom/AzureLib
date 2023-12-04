package mod.azure.azurelib;

import com.mojang.blaze3d.platform.InputConstants;
import mod.azure.azurelib.client.AzureLibClient;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.util.IncompatibleModsCheck;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = AzureLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientListener {

    @SubscribeEvent
    public static void registerKeys(final RegisterKeyMappingsEvent event) {
        Keybindings.RELOAD = new KeyMapping("key.azurelib.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.azurelib.binds");
        event.register(Keybindings.RELOAD);
        Keybindings.SCOPE = new KeyMapping("key.azurelib.scope", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.azurelib.binds");
        event.register(Keybindings.SCOPE);
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
                modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> {
                    if (list.size() == 1) {
                        return AzureLibClient.getConfigScreen(list.get(0).getConfigId(), screen);
                    }
                    return AzureLibClient.getConfigScreenByGroup(list, modId, screen);
                }));
            });
        }
    }

    @SubscribeEvent
    public static void onClientStart(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            IncompatibleModsCheck.warnings(Minecraft.getInstance());
        }
    }
}
