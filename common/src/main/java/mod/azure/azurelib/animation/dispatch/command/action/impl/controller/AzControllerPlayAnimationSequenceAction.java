package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;

public record AzControllerPlayAnimationSequenceAction(
    String controllerName,
    AzAnimationSequence sequence
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerPlayAnimationSequenceAction> DECODER = buf -> {
        String controllerName = buf.readUtf(); // Read controller name (UTF string)
        AzAnimationSequence sequence = AzAnimationSequence.DECODER.apply(buf); // Decode AzAnimationSequence
        return new AzControllerPlayAnimationSequenceAction(controllerName, sequence); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerPlayAnimationSequenceAction> ENCODER = (
        buf,
        action
    ) -> {
        buf.writeUtf(action.controllerName()); // Write controller name (UTF string)
        AzAnimationSequence.ENCODER.accept(buf, action.sequence()); // Encode AzAnimationSequence
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/play_animation_sequence");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.run(originSide, sequence);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerPlayAnimationSequenceAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate to the DECODER functional interface
    }

    public static void encode(FriendlyByteBuf buf, AzControllerPlayAnimationSequenceAction action) {
        ENCODER.accept(buf, action); // Delegate to the ENCODER functional interface
    }
}
