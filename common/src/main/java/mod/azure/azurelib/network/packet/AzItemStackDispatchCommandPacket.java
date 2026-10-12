package mod.azure.azurelib.network.packet;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

/**
 * Dispatches an {@link AzCommand} to an item stack's animators on the client.
 *
 * @param itemStackId     the stack's AzureLib UUID
 * @param dispatchCommand the command to run
 * @param view            the animator to target, or empty to run on every animator the stack has
 */
public record AzItemStackDispatchCommandPacket(
    UUID itemStackId,
    AzCommand dispatchCommand,
    Optional<AzItemAnimatorView> view
) implements AbstractPacket {

    public static final Type<AzItemStackDispatchCommandPacket> TYPE = new Type<>(
        AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID
    );

    public static final StreamCodec<FriendlyByteBuf, AzItemStackDispatchCommandPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        AzItemStackDispatchCommandPacket::itemStackId,
        AzCommand.CODEC,
        AzItemStackDispatchCommandPacket::dispatchCommand,
        AzItemAnimatorView.OPTIONAL_STREAM_CODEC,
        AzItemStackDispatchCommandPacket::view,
        AzItemStackDispatchCommandPacket::new
    );

    public void handle() {
        AzIdentifiableItemStackAnimatorCache.getInstance()
            .forEach(
                itemStackId,
                view.orElse(null),
                animator -> dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator))
            );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
