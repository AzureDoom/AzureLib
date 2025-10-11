package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzControllerSetRepeatTimesAction(
    String controllerName,
    double repeatXTimes
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerSetRepeatTimesAction> DECODER = buf -> {
        String controllerName = buf.readUtf();
        double repeatXTimes = buf.readDouble(); // Read double from the buffer
        return new AzControllerSetRepeatTimesAction(controllerName, repeatXTimes); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerSetRepeatTimesAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName());
        buf.writeDouble(action.repeatXTimes()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource(
        "controller/set_repeat_times_tick_offset"
    );

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

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

    public static AzControllerSetRepeatTimesAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf);
    }

    public static void encode(FriendlyByteBuf buf, AzControllerSetRepeatTimesAction action) {
        ENCODER.accept(buf, action);
    }
}
