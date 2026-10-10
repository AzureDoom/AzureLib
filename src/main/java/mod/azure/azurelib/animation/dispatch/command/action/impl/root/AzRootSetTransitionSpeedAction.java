package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.network.AzByteBuf;

/**
 * Represents an action that sets the transition speed of animation controllers within an {@link AzAnimator}. This
 * action is part of the AzureLib animation system and provides a means to modify the transition length property of all
 * animation controllers contained in the target animator. The {@link AzRootSetTransitionSpeedAction} encapsulates a
 * single `transitionSpeed` value, which determines the length of animation transition in seconds when applied during
 * animation state changes.
 */
public final class AzRootSetTransitionSpeedAction implements AzAction {

    private final float transitionSpeed;

    public AzRootSetTransitionSpeedAction(float transitionSpeed) {
        this.transitionSpeed = transitionSpeed;
    }

    public float transitionSpeed() {
        return this.transitionSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzRootSetTransitionSpeedAction))
            return false;
        AzRootSetTransitionSpeedAction other = (AzRootSetTransitionSpeedAction) o;
        return Float.compare(this.transitionSpeed, other.transitionSpeed) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Float.hashCode(this.transitionSpeed);
        return result;
    }

    @Override
    public String toString() {
        return "AzRootSetTransitionSpeedAction[transitionSpeed=" + this.transitionSpeed + "]";
    }

    public static final Function<AzByteBuf, AzRootSetTransitionSpeedAction> DECODER = buf -> {
        float transitionSpeed = buf.readFloat(); // Read float from the buffer
        return new AzRootSetTransitionSpeedAction(transitionSpeed); // Create a new instance
    };

    public static final BiConsumer<AzByteBuf, AzRootSetTransitionSpeedAction> ENCODER = (buf, action) -> {
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

    public static AzRootSetTransitionSpeedAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(AzByteBuf buf, AzRootSetTransitionSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
