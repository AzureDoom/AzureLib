package mod.azure.azurelib.animation.dispatch.command.action;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.codec.AzActionCodec;

/**
 * The AzAction interface serves as a base contract for defining actions that can be dispatched within the animation
 * system. It provides methods for handling an action and retrieving its unique resource location identifier.
 * Implementations of this interface encapsulate specific animation-related behaviors, allowing for the modification or
 * control of animation states or properties within an {@link AzAnimator}.
 */
public interface AzAction {

    /**
     * Decodes an AzAction from a {@link FriendlyByteBuf}. Delegates to {@link AzActionCodec#decode(FriendlyByteBuf)}
     * for decoding logic.
     */
    static AzAction decode(FriendlyByteBuf byteBuf) {
        return new AzActionCodec().decode(byteBuf);
    }

    /**
     * Encodes this AzAction into a {@link FriendlyByteBuf}. Delegates to
     * {@link AzActionCodec#encode(FriendlyByteBuf, AzAction)} for encoding logic.
     */
    default void encode(FriendlyByteBuf byteBuf) {
        new AzActionCodec().encode(byteBuf, this);
    }

    void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator);

    ResourceLocation getResourceLocation();
}
