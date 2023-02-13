package mod.azure.azurelib.network.packet;

import io.netty.buffer.Unpooled;
import mod.azure.azurelib.network.AzureLibNetwork;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class EntityPacket {

	public static Packet<ClientGamePacketListener> createPacket(Entity entity) {
		FriendlyByteBuf buf = createBuffer();
		buf.writeVarInt(Registry.ENTITY_TYPE.getId(entity.getType()));
		buf.writeUUID(entity.getUUID());
		buf.writeVarInt(entity.getId());
		buf.writeDouble(entity.getX());
		buf.writeDouble(entity.getY());
		buf.writeDouble(entity.getZ());
		buf.writeByte(Mth.floor(entity.getXRot() * 256.0F / 360.0F));
		buf.writeByte(Mth.floor(entity.getYRot() * 256.0F / 360.0F));
		buf.writeFloat(entity.getXRot());
		buf.writeFloat(entity.getYRot());
		return ServerPlayNetworking.createS2CPacket(AzureLibNetwork.CUSTOM_ENTITY_ID, buf);
	}

	private static FriendlyByteBuf createBuffer() {
		return new FriendlyByteBuf(Unpooled.buffer());
	}

}