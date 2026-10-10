package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public final class AzControllerSetTransitionSpeedAction implements AzAction {

    private final String controllerName;

    private final float transitionSpeed;

    public AzControllerSetTransitionSpeedAction(String controllerName, float transitionSpeed) {
        this.controllerName = controllerName;
        this.transitionSpeed = transitionSpeed;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public float transitionSpeed() {
        return this.transitionSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerSetTransitionSpeedAction))
            return false;
        AzControllerSetTransitionSpeedAction other = (AzControllerSetTransitionSpeedAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && Float.compare(this.transitionSpeed, other.transitionSpeed) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + Float.hashCode(this.transitionSpeed);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerSetTransitionSpeedAction[controllerName=" + this.controllerName + ", transitionSpeed="
            + this.transitionSpeed + "]";
    }

    public static final Function<PacketBuffer, AzControllerSetTransitionSpeedAction> DECODER = buf -> {
        String controllerName = buf.readString(32767);
        float transitionSpeed = buf.readFloat(); // Read float from the buffer
        return new AzControllerSetTransitionSpeedAction(controllerName, transitionSpeed); // Create a new instance
    };

    public static final BiConsumer<PacketBuffer, AzControllerSetTransitionSpeedAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName());
        buf.writeFloat(action.transitionSpeed()); // Write the transition speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_transition_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withTransitionLength(transitionSpeed));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetTransitionSpeedAction decode(PacketBuffer buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(PacketBuffer buf, AzControllerSetTransitionSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
