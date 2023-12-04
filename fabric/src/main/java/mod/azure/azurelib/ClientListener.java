package mod.azure.azurelib;

import com.mojang.blaze3d.platform.InputConstants;
import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.util.IncompatibleModsCheck;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ClientListener implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(IncompatibleModsCheck::warnings);
        Keybindings.RELOAD = new KeyMapping("key.azurelib.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.azurelib.binds");
        KeyBindingHelper.registerKeyBinding(Keybindings.RELOAD);
        Keybindings.SCOPE = new KeyMapping("key.azurelib.scope", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.azurelib.binds");
        KeyBindingHelper.registerKeyBinding(Keybindings.SCOPE);
        if (!AzureLibMod.config.useVanillaUseKey) {
            Keybindings.FIRE_WEAPON = new KeyMapping("key.azurelib.fire", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON, "category.azurelib.binds");
            KeyBindingHelper.registerKeyBinding(Keybindings.FIRE_WEAPON);
        }
        Services.NETWORK.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
    }
}
