package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;

public record AzControllerPlayAnimationSequenceAction(
    String controllerName,
    AzAnimationSequence sequence
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzControllerPlayAnimationSequenceAction> CODEC = StreamCodec
        .composite(
            ByteBufCodecs.STRING_UTF8,
            AzControllerPlayAnimationSequenceAction::controllerName,
            AzAnimationSequence.CODEC,
            AzControllerPlayAnimationSequenceAction::sequence,
            AzControllerPlayAnimationSequenceAction::new
        );

    public static final Identifier RESOURCE_LOCATION = AzureLib.modResource("controller/play_animation_sequence");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.run(originSide, sequence);
        }
    }

    @Override
    public Identifier getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
