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

public final class AzControllerSetRepeatTimesAction implements AzAction {

    private final String controllerName;

    private final double repeatXTimes;

    public AzControllerSetRepeatTimesAction(String controllerName, double repeatXTimes) {
        this.controllerName = controllerName;
        this.repeatXTimes = repeatXTimes;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public double repeatXTimes() {
        return this.repeatXTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerSetRepeatTimesAction))
            return false;
        AzControllerSetRepeatTimesAction other = (AzControllerSetRepeatTimesAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && Double.compare(this.repeatXTimes, other.repeatXTimes) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + Double.hashCode(this.repeatXTimes);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerSetRepeatTimesAction[controllerName=" + this.controllerName + ", repeatXTimes="
            + this.repeatXTimes + "]";
    }

    public static final Function<PacketBuffer, AzControllerSetRepeatTimesAction> DECODER = buf -> {
        String controllerName = buf.readString(32767);
        double repeatXTimes = buf.readDouble(); // Read double from the buffer
        return new AzControllerSetRepeatTimesAction(controllerName, repeatXTimes); // Create a new instance
    };

    public static final BiConsumer<PacketBuffer, AzControllerSetRepeatTimesAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName());
        buf.writeDouble(action.repeatXTimes()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource(
        "controller/set_repeat_times_tick_offset"
    );

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(
                controller.animationProperties().withRepeatXTimes(repeatXTimes)
            );
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetRepeatTimesAction decode(PacketBuffer buf) {
        return DECODER.apply(buf);
    }

    public static void encode(PacketBuffer buf, AzControllerSetRepeatTimesAction action) {
        ENCODER.accept(buf, action);
    }
}
