package mod.azure.azurelib.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.internal.client.AzureLibClient;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.config.ConfigHolder;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = AzureLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModListener {

    @SubscribeEvent
    public static void registerKeys(final RegisterKeyMappingsEvent event) {
        ClientUtils.RELOAD = new KeyMapping("key.azurelib.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.azurelib.binds");
        event.register(ClientUtils.RELOAD);
        ClientUtils.SCOPE = new KeyMapping("key.azurelib.scope", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.azurelib.binds");
        event.register(ClientUtils.SCOPE);
        ClientUtils.FIRE_WEAPON = new KeyMapping("key.azurelib.fire", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.azurelib.binds");
        event.register(ClientUtils.FIRE_WEAPON);
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
}
