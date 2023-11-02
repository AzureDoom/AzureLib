package mod.azure.azurelib.fabric.network.api;

import mod.azure.azurelib.common.internal.common.network.api.IPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public interface IClientPacket<T> extends IPacket<T> {

    @Environment(EnvType.CLIENT)
    void handleClientsidePacket(Minecraft client, ClientPacketListener listener, T packetData, PacketSender dispatcher);
}
