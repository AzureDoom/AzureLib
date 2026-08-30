package mod.azure.azurelib.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.server.packs.PackType;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.render.item.AzItemSpecialRenderer;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(AzureLib.modResource("models"), new AzureLibCache());
        SpecialModelRenderers.ID_MAPPER.put(AzureLib.modResource("azurelib"), AzItemSpecialRenderer.Unbaked.MAP_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(
            AzEntityDispatchCommandPacket.TYPE,
            (packet, _) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AzItemStackDispatchCommandPacket.TYPE,
            (packet, _) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AzBlockEntityDispatchCommandPacket.TYPE,
            (packet, _) -> packet.handle()
        );
    }
}
