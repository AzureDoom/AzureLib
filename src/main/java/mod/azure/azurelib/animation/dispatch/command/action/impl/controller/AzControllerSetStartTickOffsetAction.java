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

public final class AzControllerSetStartTickOffsetAction implements AzAction {

    private final String controllerName;

    private final double startTickOffset;

    public AzControllerSetStartTickOffsetAction(String controllerName, double startTickOffset) {
        this.controllerName = controllerName;
        this.startTickOffset = startTickOffset;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public double startTickOffset() {
        return this.startTickOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerSetStartTickOffsetAction))
            return false;
        AzControllerSetStartTickOffsetAction other = (AzControllerSetStartTickOffsetAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && Double.compare(this.startTickOffset, other.startTickOffset) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + Double.hashCode(this.startTickOffset);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerSetStartTickOffsetAction[controllerName=" + this.controllerName + ", startTickOffset="
            + this.startTickOffset + "]";
    }

    public static final Function<AzByteBuf, AzControllerSetStartTickOffsetAction> DECODER = buf -> {
        String controllerName = buf.readString(32767);
        double startTickOffset = buf.readDouble(); // Read double from the buffer
        return new AzControllerSetStartTickOffsetAction(controllerName, startTickOffset); // Create a new instance
    };

    public static final BiConsumer<AzByteBuf, AzControllerSetStartTickOffsetAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName());
        buf.writeDouble(action.startTickOffset()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_start_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withStartTickOffset(startTickOffset));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetStartTickOffsetAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(AzByteBuf buf, AzControllerSetStartTickOffsetAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
