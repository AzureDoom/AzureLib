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

public final class AzControllerCancelAction implements AzAction {

    private final String controllerName;

    public AzControllerCancelAction(String controllerName) {
        this.controllerName = controllerName;
    }

    public String controllerName() {
        return this.controllerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerCancelAction))
            return false;
        AzControllerCancelAction other = (AzControllerCancelAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerCancelAction[controllerName=" + this.controllerName + "]";
    }

    public static final Function<AzByteBuf, AzControllerCancelAction> DECODER = buf -> {
        String controllerName = buf.readString(32767); // Read UTF-8 string for the controller's name
        return new AzControllerCancelAction(controllerName);
    };

    public static final BiConsumer<AzByteBuf, AzControllerCancelAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName()); // Write UTF-8 string for the controller's name
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/cancel");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            // Clear the queue too, otherwise the next queued stage of a sequence would start playing.
            controller.animationQueue().clear();
            controller.setCurrentAnimation(null);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerCancelAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate to the DECODER functional interface
    }

    public static void encode(AzByteBuf buf, AzControllerCancelAction action) {
        ENCODER.accept(buf, action); // Delegate to the ENCODER functional interface
    }
}
