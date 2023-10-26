package mod.azure.azurelib.network.packet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkHooks;

public class EntityPacket {

	public static Packet<ClientGamePacketListener> createPacket(Entity entity) {
		return NetworkHooks.getEntitySpawningPacket(entity);
	}

}