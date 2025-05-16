package mod.azure.azurelib.rewrite.animation.dispatch.command.action;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.codec.AzActionCodec;

/**
 * The AzAction interface serves as a base contract for defining actions that can be dispatched within the animation
 * system. It provides methods for handling an action and retrieving its unique resource location identifier.
 * Implementations of this interface encapsulate specific animation-related behaviors, allowing for the modification or
 * control of animation states or properties within an {@link AzAnimator}.
 */
public interface AzAction {

    /**
     * Decodes an AzAction from a {@link PacketBuffer}. Delegates to {@link AzActionCodec#decode(PacketBuffer)} for
     * decoding logic.
     */
    static AzAction decode(PacketBuffer byteBuf) {
        return new AzActionCodec().decode(byteBuf);
    }

    /**
     * Encodes this AzAction into a {@link PacketBuffer}. Delegates to
     * {@link AzActionCodec#encode(PacketBuffer, AzAction)} for encoding logic.
     */
    default void encode(PacketBuffer byteBuf) {
        new AzActionCodec().encode(byteBuf, this);
    }

    void handle(AzDispatchSide originSide, AzAnimator<?> animator);

    ResourceLocation getResourceLocation();
}
