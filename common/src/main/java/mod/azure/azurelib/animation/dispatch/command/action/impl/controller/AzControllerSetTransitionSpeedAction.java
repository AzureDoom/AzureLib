package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzControllerSetTransitionSpeedAction(
    String controllerName,
    float transitionSpeed
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerSetTransitionSpeedAction> DECODER = buf -> {
        String controllerName = buf.readUtf();
        float transitionSpeed = buf.readFloat(); // Read float from the buffer
        return new AzControllerSetTransitionSpeedAction(controllerName, transitionSpeed); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerSetTransitionSpeedAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName());
        buf.writeFloat(action.transitionSpeed()); // Write the transition speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_transition_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withTransitionLength(transitionSpeed));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetTransitionSpeedAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzControllerSetTransitionSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
