package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzControllerCancelAction(
    String controllerName
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerCancelAction> DECODER = buf -> {
        String controllerName = buf.readUtf(); // Read UTF-8 string for the controller's name
        return new AzControllerCancelAction(controllerName);
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerCancelAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName()); // Write UTF-8 string for the controller's name
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/cancel");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setCurrentAnimation(null);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerCancelAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate to the DECODER functional interface
    }

    public static void encode(FriendlyByteBuf buf, AzControllerCancelAction action) {
        ENCODER.accept(buf, action); // Delegate to the ENCODER functional interface
    }
}
