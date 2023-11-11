package mod.azure.azurelib;

import com.mojang.blaze3d.platform.InputConstants;
import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Keybindings.RELOAD = new KeyMapping("key.azurelib.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.azurelib.binds");
        KeyBindingHelper.registerKeyBinding(Keybindings.RELOAD);
        Services.NETWORK.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
    }
}
