package mod.azure.azurelib.network.packet;

import net.minecraft.util.ResourceLocation;

import java.util.UUID;

import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public class AzItemStackDispatchCommandPacket extends AbstractPacket {

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
    public void encode(AzByteBuf buf) {
        buf.writeUniqueId(this.itemStackId); // Encode the UUID
        AzCommand.ENCODER.accept(buf, this.dispatchCommand); // Encode AzCommand
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }

    public static AzItemStackDispatchCommandPacket receive(AzByteBuf buf) {
        UUID itemStackId = buf.readUniqueId(); // Decode UUID
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand
        return new AzItemStackDispatchCommandPacket(itemStackId, dispatchCommand); // Create and return the packet
                                                                                   // instance
    }

    public void handle() {
        AzItemAnimator animator = AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(itemStackId);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }
}
