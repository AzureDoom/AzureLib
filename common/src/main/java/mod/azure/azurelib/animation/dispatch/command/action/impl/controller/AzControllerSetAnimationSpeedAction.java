package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzControllerSetAnimationSpeedAction(
    String controllerName,
    double animationSpeed
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerSetAnimationSpeedAction> DECODER = buf -> {
        String controllerName = buf.readUtf(); // Read controller name (UTF string)
        double animationSpeed = buf.readDouble(); // Read double from the buffer
        return new AzControllerSetAnimationSpeedAction(controllerName, animationSpeed); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerSetAnimationSpeedAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName()); // Write controller name (UTF string)
        buf.writeDouble(action.animationSpeed()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_animation_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withAnimationSpeed(animationSpeed));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetAnimationSpeedAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate to the DECODER functional interface
    }

    public static void encode(FriendlyByteBuf buf, AzControllerSetAnimationSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate to the ENCODER functional interface
    }
}
