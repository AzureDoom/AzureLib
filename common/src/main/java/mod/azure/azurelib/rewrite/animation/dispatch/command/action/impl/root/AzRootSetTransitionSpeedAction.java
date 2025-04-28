package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The {@code AzRootSetTransitionSpeedAction} class implements the {@link AzAction} interface and represents an action
 * that modifies the transition speed for an animator during an animation state. This action is intended for use within
 * the animation system to adjust the transition timing of animations. This class provides a unique resource location
 * identifier for this specific action and handles the logic required to apply the transition speed modification to the
 * target {@link AzAnimator}. It utilizes {@link StreamCodec} for serialization and deserialization of this action.
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
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
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
