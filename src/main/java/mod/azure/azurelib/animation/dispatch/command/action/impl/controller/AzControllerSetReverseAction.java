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

public final class AzControllerSetReverseAction implements AzAction {

    private final String controllerName;

    private final boolean hasReverse;

    public AzControllerSetReverseAction(String controllerName, boolean hasReverse) {
        this.controllerName = controllerName;
        this.hasReverse = hasReverse;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public boolean hasReverse() {
        return this.hasReverse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerSetReverseAction))
            return false;
        AzControllerSetReverseAction other = (AzControllerSetReverseAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && this.hasReverse == other.hasReverse;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + Boolean.hashCode(this.hasReverse);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerSetReverseAction[controllerName=" + this.controllerName + ", hasReverse=" + this.hasReverse
            + "]";
    }

    public static final Function<AzByteBuf, AzControllerSetReverseAction> DECODER = buf -> {
        String controllerName = buf.readString(32767);
        boolean hasReverse = buf.readBoolean(); // Read boolean from the buffer
        return new AzControllerSetReverseAction(controllerName, hasReverse); // Create a new instance
    };

    public static final BiConsumer<AzByteBuf, AzControllerSetReverseAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName());
        buf.writeBoolean(action.hasReverse()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource(
        "controller/set_reverse_tick_offset"
    );

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setReversing(hasReverse);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetReverseAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(AzByteBuf buf, AzControllerSetReverseAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
