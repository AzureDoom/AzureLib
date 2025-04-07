package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class AzItemStackDispatchCommandPacket extends AbstractPacket {

    // TODO: Updated encode/receive methods for AzCommand.CODEC/dispatchCommand
    public static final StreamCodec<FriendlyByteBuf, AzItemStackDispatchCommandPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        AzItemStackDispatchCommandPacket::itemStackId,
        AzCommand.CODEC,
        AzItemStackDispatchCommandPacket::dispatchCommand,
        AzItemStackDispatchCommandPacket::new
    );

    private final UUID itemStackId;
    private final AzCommand dispatchCommand;

    public AzItemStackDispatchCommandPacket(
            UUID itemStackId,
            AzCommand dispatchCommand
    ) {
        this.itemStackId = itemStackId;
        this.dispatchCommand = dispatchCommand;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.itemStackId);
        // TODO: Needs fixed
        // AzCommand.CODEC
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }

    public static AzItemStackDispatchCommandPacket receive(FriendlyByteBuf buf) {
        var readUUID = buf.readUUID();
        // TODO: Needs fixed
        // AzCommand azCommand = buf.readUtf();

        return new AzItemStackDispatchCommandPacket(readUUID, azCommand);
    }

    public void handle() {
        var animator = AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(itemStackId);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }
}
