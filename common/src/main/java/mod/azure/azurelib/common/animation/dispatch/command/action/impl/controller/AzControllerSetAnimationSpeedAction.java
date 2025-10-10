package mod.azure.azurelib.common.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.AzAnimator;
import mod.azure.azurelib.common.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.common.animation.dispatch.command.action.AzAction;

public record AzControllerSetAnimationSpeedAction(
    String controllerName,
    double animationSpeed
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzControllerSetAnimationSpeedAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        AzControllerSetAnimationSpeedAction::controllerName,
        ByteBufCodecs.DOUBLE,
        AzControllerSetAnimationSpeedAction::animationSpeed,
        AzControllerSetAnimationSpeedAction::new
    );

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
}
