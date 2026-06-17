package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzControllerSetStartTickOffsetAction(
    String controllerName,
    double startTickOffset
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzControllerSetStartTickOffsetAction> CODEC = StreamCodec
        .composite(
            ByteBufCodecs.STRING_UTF8,
            AzControllerSetStartTickOffsetAction::controllerName,
            ByteBufCodecs.DOUBLE,
            AzControllerSetStartTickOffsetAction::startTickOffset,
            AzControllerSetStartTickOffsetAction::new
        );

    public static final Identifier RESOURCE_LOCATION = AzureLib.modResource("controller/set_start_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withStartTickOffset(startTickOffset));
        }
    }

    @Override
    public Identifier getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
