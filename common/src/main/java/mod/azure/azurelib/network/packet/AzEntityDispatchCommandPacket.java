package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.util.ClientUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class AzEntityDispatchCommandPacket extends AbstractPacket {

    // TODO: Updated encode/receive methods for AzCommand.CODEC/dispatchCommand
    public static final StreamCodec<FriendlyByteBuf, AzEntityDispatchCommandPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        AzEntityDispatchCommandPacket::entityId,
        AzCommand.CODEC,
        AzEntityDispatchCommandPacket::dispatchCommand,
        AzEntityDispatchCommandPacket::new
    );

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
        // TODO: Needs fixed
        // AzCommand.CODEC
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }

    public static AzItemStackDispatchCommandPacket receive(FriendlyByteBuf buf) {
        var entityId = buf.readInt();
        // TODO: Needs fixed
        // AzCommand azCommand = buf.readUtf();

        return new AzItemStackDispatchCommandPacket(entityId, azCommand);
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
