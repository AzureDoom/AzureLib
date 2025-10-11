package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

/**
 * Represents an action that sets the transition speed of animation controllers within an {@link AzAnimator}. This
 * action is part of the AzureLib animation system and provides a means to modify the transition length property of all
 * animation controllers contained in the target animator. The {@link AzRootSetTransitionSpeedAction} encapsulates a
 * single `transitionSpeed` value, which determines the length of animation transition in seconds when applied during
 * animation state changes.
 */
public record AzRootSetTransitionSpeedAction(
    float transitionSpeed
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzRootSetTransitionSpeedAction> DECODER = buf -> {
        float transitionSpeed = buf.readFloat(); // Read float from the buffer
        return new AzRootSetTransitionSpeedAction(transitionSpeed); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootSetTransitionSpeedAction> ENCODER = (buf, action) -> {
        buf.writeFloat(action.transitionSpeed()); // Write the transition speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_transition_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withTransitionLength(transitionSpeed)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetTransitionSpeedAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzRootSetTransitionSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
