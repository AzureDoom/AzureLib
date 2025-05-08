package mod.azure.azurelib.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface IPacket<P extends IPacket<P>> {

	void encode(PacketBuffer buffer);

	P decode(PacketBuffer buffer);

	void handle(Supplier<NetworkEvent.Context> supplier);
}