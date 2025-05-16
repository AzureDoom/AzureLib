package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

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
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.itemStackId); // Encode the UUID
        AzCommand.ENCODER.accept(buf, this.dispatchCommand); // Encode AzCommand
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }

    public static AzItemStackDispatchCommandPacket receive(FriendlyByteBuf buf) {
        UUID itemStackId = buf.readUUID(); // Decode UUID
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand
        return new AzItemStackDispatchCommandPacket(itemStackId, dispatchCommand); // Create and return the packet instance
    }

    public void handle() {
        var animator = AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(itemStackId);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }
}
