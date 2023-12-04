package mod.azure.azurelib;

import com.mojang.blaze3d.platform.InputConstants;
import mod.azure.azurelib.client.AzureLibClient;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.util.IncompatibleModsCheck;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = AzureLib.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientListener {

    @SubscribeEvent
    public static void registerKeys(final RegisterKeyMappingsEvent event) {
        Keybindings.RELOAD = new KeyMapping("key.azurelib.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.azurelib.binds");
        event.register(Keybindings.RELOAD);
        Keybindings.SCOPE = new KeyMapping("key.azurelib.scope", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.azurelib.binds");
        event.register(Keybindings.SCOPE);
        if (!AzureLibMod.config.useVanillaUseKey) {
            Keybindings.FIRE_WEAPON = new KeyMapping("key.azurelib.fire", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON, "category.azurelib.binds");
            event.register(Keybindings.FIRE_WEAPON);
        }
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
