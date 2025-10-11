package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzControllerSetReverseAction(
    String controllerName,
    boolean hasReverse
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerSetReverseAction> DECODER = buf -> {
        String controllerName = buf.readUtf();
        boolean hasReverse = buf.readBoolean(); // Read boolean from the buffer
        return new AzControllerSetReverseAction(controllerName, hasReverse); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerSetReverseAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName());
        buf.writeBoolean(action.hasReverse()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource(
        "controller/set_reverse_tick_offset"
    );

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(
                controller.animationProperties().withShouldReverse(hasReverse)
            );
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetReverseAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzControllerSetReverseAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
