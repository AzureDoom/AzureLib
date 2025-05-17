package mod.azure.azurelib.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.util.ClientUtils;

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
    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeInt(this.entityId);
        AzCommand.ENCODER.accept(buf, this.dispatchCommand);
        return buf;
    }

    public static AzEntityDispatchCommandPacket receive(FriendlyByteBuf buf) {
        int entityId = buf.readInt(); // Decode integer entity ID
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand
        return new AzEntityDispatchCommandPacket(entityId, dispatchCommand); // Create and return the packet instance
    }

    public static void receive(
        Minecraft client,
        ClientPacketListener handler,
        FriendlyByteBuf buf,
        PacketSender responseSender
    ) {
        int entityId = buf.readInt(); // Decode integer entity ID
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand

        client.execute(() -> runOnThread(entityId, dispatchCommand));
    }

    private static void runOnThread(int entityId, AzCommand dispatchCommand) {
        Entity entity = ClientUtils.getLevel().getEntity(entityId);

        if (entity == null) {
            return;
        }

        AzAnimator<Entity> animator = AzAnimatorAccessor.getOrNull(entity);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }
}
