package mod.azure.azurelib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.client.ClientUtils;

public class AzEntityDispatchCommandPacket extends AbstractPacket {

    private final int entityId;

    private final AzCommand dispatchCommand;

    public AzEntityDispatchCommandPacket(
        int entityId,
        AzCommand dispatchCommand
    ) {
        this.entityId = entityId;
        this.dispatchCommand = dispatchCommand;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        AzCommand.ENCODER.accept(buf, this.dispatchCommand);
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }

    public static AzEntityDispatchCommandPacket receive(FriendlyByteBuf buf) {
        int entityId = buf.readInt(); // Decode integer entity ID
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand
        return new AzEntityDispatchCommandPacket(entityId, dispatchCommand); // Create and return the packet instance
    }

    public void handle() {
        var entity = ClientUtils.getLevel().getEntity(this.entityId);

        if (entity == null) {
            return;
        }

        var animator = AzAnimatorAccessor.getOrNull(entity);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }
}
