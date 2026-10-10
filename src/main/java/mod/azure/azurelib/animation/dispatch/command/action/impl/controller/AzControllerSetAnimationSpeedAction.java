package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.network.AzByteBuf;

public final class AzControllerSetAnimationSpeedAction implements AzAction {

    private final String controllerName;

    private final double animationSpeed;

    public AzControllerSetAnimationSpeedAction(String controllerName, double animationSpeed) {
        this.controllerName = controllerName;
        this.animationSpeed = animationSpeed;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public double animationSpeed() {
        return this.animationSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerSetAnimationSpeedAction))
            return false;
        AzControllerSetAnimationSpeedAction other = (AzControllerSetAnimationSpeedAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && Double.compare(this.animationSpeed, other.animationSpeed) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + Double.hashCode(this.animationSpeed);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerSetAnimationSpeedAction[controllerName=" + this.controllerName + ", animationSpeed="
            + this.animationSpeed + "]";
    }

    public static final Function<AzByteBuf, AzControllerSetAnimationSpeedAction> DECODER = buf -> {
        String controllerName = buf.readString(32767); // Read controller name (UTF string)
        double animationSpeed = buf.readDouble(); // Read double from the buffer
        return new AzControllerSetAnimationSpeedAction(controllerName, animationSpeed); // Create a new instance
    };

    public static final BiConsumer<AzByteBuf, AzControllerSetAnimationSpeedAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName()); // Write controller name (UTF string)
        buf.writeDouble(action.animationSpeed()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_animation_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withAnimationSpeed(animationSpeed));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetAnimationSpeedAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate to the DECODER functional interface
    }

    public static void encode(AzByteBuf buf, AzControllerSetAnimationSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate to the ENCODER functional interface
    }
}
