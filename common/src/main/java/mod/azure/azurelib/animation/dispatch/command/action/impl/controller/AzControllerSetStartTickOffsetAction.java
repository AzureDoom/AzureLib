package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzControllerSetStartTickOffsetAction(
    String controllerName,
    double startTickOffset
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerSetStartTickOffsetAction> DECODER = buf -> {
        String controllerName = buf.readUtf();
        double startTickOffset = buf.readDouble(); // Read double from the buffer
        return new AzControllerSetStartTickOffsetAction(controllerName, startTickOffset); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerSetStartTickOffsetAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName());
        buf.writeDouble(action.startTickOffset()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_start_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withStartTickOffset(startTickOffset));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetStartTickOffsetAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzControllerSetStartTickOffsetAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
