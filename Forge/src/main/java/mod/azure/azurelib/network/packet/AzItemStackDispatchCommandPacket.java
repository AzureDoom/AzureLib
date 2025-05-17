package mod.azure.azurelib.network.packet;

import net.minecraft.network.PacketBuffer;

import java.util.UUID;

import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.rewrite.animation.impl.AzItemAnimator;

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
    public void encode(PacketBuffer buf) {
        buf.writeUniqueId(this.itemStackId); // Encode the UUID
        AzCommand.ENCODER.accept(buf, this.dispatchCommand); // Encode AzCommand
    }

    public static AzItemStackDispatchCommandPacket receive(PacketBuffer buf) {
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
