package mod.azure.azurelib.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.network.packet.*;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (AzureLib.hasKeyBindsInitialized) {
            ClientUtils.RELOAD = new KeyMapping(
                "key.azurelib.reload",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.azurelib.binds"
            );
            KeyBindingHelper.registerKeyBinding(ClientUtils.RELOAD);
            ClientUtils.SCOPE = new KeyMapping(
                "key.azurelib.scope",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category.azurelib.binds"
            );
            KeyBindingHelper.registerKeyBinding(ClientUtils.SCOPE);
            ClientUtils.FIRE_WEAPON = new KeyMapping(
                "key.azurelib.fire",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.azurelib.binds"
            );
            KeyBindingHelper.registerKeyBinding(ClientUtils.FIRE_WEAPON);
        }
        ClientPlayNetworking.registerGlobalReceiver(
            BlockEntityAnimTriggerPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            BlockEntityAnimDataSyncPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(EntityAnimTriggerPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(
            AzEntityDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AzItemStackDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AzBlockEntityDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            EntityAnimDataSyncPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(AnimTriggerPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(AnimDataSyncPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(SendConfigDataPacket.TYPE, (packet, context) -> packet.handle());

    }
}
