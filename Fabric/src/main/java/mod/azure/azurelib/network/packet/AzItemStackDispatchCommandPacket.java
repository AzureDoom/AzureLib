package mod.azure.azurelib.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzureLibNetwork;

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
    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeUUID(this.itemStackId); // Encode the UUID
        AzCommand.ENCODER.accept(buf, this.dispatchCommand); // Encode AzCommand
        return buf;
    }

    public static void receive(
        Minecraft client,
        ClientPacketListener handler,
        FriendlyByteBuf buf,
        PacketSender responseSender
    ) {
        UUID itemStackId = buf.readUUID(); // Decode UUID
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand

        client.execute(() -> runOnThread(itemStackId, dispatchCommand));
    }

    private static void runOnThread(UUID itemStackId, AzCommand dispatchCommand) {
        AzItemAnimator animator = AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(itemStackId);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }
}
