package mod.azure.azurelib.network.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public interface IClientPacket<T> extends IPacket<T> {

    @Environment(EnvType.CLIENT)
    void handleClientsidePacket(Minecraft client, ClientPacketListener listener, T packetData, PacketSender dispatcher);
}
