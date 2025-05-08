package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.network.AbstractPacket;
import net.minecraft.network.FriendlyByteBuf;

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

    public static AzItemStackDispatchCommandPacket receive(FriendlyByteBuf buf) {
        UUID itemStackId = buf.readUUID(); // Decode UUID
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand
        return new AzItemStackDispatchCommandPacket(itemStackId, dispatchCommand); // Create and return the packet instance
    }

    public void handle() {
        AzItemAnimator animator = AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(itemStackId);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }
}
