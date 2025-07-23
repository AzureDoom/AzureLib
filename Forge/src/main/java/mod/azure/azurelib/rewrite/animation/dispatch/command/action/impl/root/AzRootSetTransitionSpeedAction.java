package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;

/**
 * Represents an action that sets the transition speed of animation controllers within an {@link AzAnimator}.
 */
public class AzRootSetTransitionSpeedAction implements AzAction {

    private final float transitionSpeed;

    public static final Function<PacketBuffer, AzRootSetTransitionSpeedAction> DECODER = buf -> {
        float transitionSpeed = buf.readFloat(); // Read float from the buffer
        return new AzRootSetTransitionSpeedAction(transitionSpeed); // Create a new instance
    };

    public static final BiConsumer<PacketBuffer, AzRootSetTransitionSpeedAction> ENCODER = (buf, action) -> {
        buf.writeFloat(action.transitionSpeed()); // Write the transition speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_transition_speed");

    public AzRootSetTransitionSpeedAction(float transitionSpeed) {
        this.transitionSpeed = transitionSpeed;
    }

    public float transitionSpeed() {
        return transitionSpeed;
    }

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

    public static AzRootSetTransitionSpeedAction decode(PacketBuffer buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(PacketBuffer buf, AzRootSetTransitionSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }

    @Override
    public String toString() {
        return "AzRootSetTransitionSpeedAction{" +
            "transitionSpeed=" + transitionSpeed +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AzRootSetTransitionSpeedAction that = (AzRootSetTransitionSpeedAction) o;

        return Float.compare(that.transitionSpeed, transitionSpeed) == 0;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(transitionSpeed);
    }
}
