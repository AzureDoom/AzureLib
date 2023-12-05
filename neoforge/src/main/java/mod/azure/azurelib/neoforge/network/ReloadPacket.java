package mod.azure.azurelib.neoforge.network;

import mod.azure.azurelib.common.api.common.helper.CommonUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReloadPacket {

    public int slot;

    public ReloadPacket(int slot) {
        this.slot = slot;
    }

    public ReloadPacket(final FriendlyByteBuf packetBuffer) {
        slot = packetBuffer.readInt();
    }

    public void encode(final FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(slot);
    }

    public static void handle(ReloadPacket packet, NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            final NetworkEvent.Context context = ctx;
            final PacketListener handler = context.getNetworkManager().getPacketListener();
            if (handler instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
                final ServerPlayer playerEntity = serverGamePacketListener.player;
                CommonUtils.reload(playerEntity, InteractionHand.MAIN_HAND);
            }
        });
        ctx.setPacketHandled(true);
    }
}
