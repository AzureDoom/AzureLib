package mod.azure.azurelib.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public class AzItemStackDispatchCommandPacket extends AbstractPacket {

    private final UUID itemStackId;

    private final AzCommand dispatchCommand;

    private final Optional<AzItemAnimatorView> view;

    public AzItemStackDispatchCommandPacket(
        UUID itemStackId,
        AzCommand dispatchCommand,
        Optional<AzItemAnimatorView> view
    ) {
        this.itemStackId = itemStackId;
        this.dispatchCommand = dispatchCommand;
        this.view = view;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUniqueId(this.itemStackId);
        AzCommand.ENCODER.accept(buf, this.dispatchCommand);
        AzItemAnimatorView.encode(buf, this.view);
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }

    public static AzItemStackDispatchCommandPacket receive(PacketBuffer buf) {
        UUID itemStackId = buf.readUniqueId();
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf);
        Optional<AzItemAnimatorView> view = AzItemAnimatorView.decode(buf);

        return new AzItemStackDispatchCommandPacket(itemStackId, dispatchCommand, view);
    }

    public void handle() {
        AzIdentifiableItemStackAnimatorCache.getInstance()
            .dispatchOrQueue(
                itemStackId,
                view.orElse(null),
                animator -> dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator))
            );
    }
}
