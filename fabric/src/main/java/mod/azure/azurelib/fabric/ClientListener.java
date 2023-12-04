package mod.azure.azurelib.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.internal.common.util.IncompatibleModsCheck;
import mod.azure.azurelib.fabric.network.Networking;
import mod.azure.azurelib.common.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(IncompatibleModsCheck::warnings);
        ClientUtils.RELOAD = new KeyMapping("key.azurelib.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.azurelib.binds");
        KeyBindingHelper.registerKeyBinding(ClientUtils.RELOAD);
        ClientUtils.SCOPE = new KeyMapping("key.azurelib.scope", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.azurelib.binds");
        KeyBindingHelper.registerKeyBinding(ClientUtils.SCOPE);
        Services.NETWORK.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
    }
}
