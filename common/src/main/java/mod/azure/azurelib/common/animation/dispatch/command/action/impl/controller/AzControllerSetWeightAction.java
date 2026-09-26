package mod.azure.azurelib.common.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.AzAnimator;
import mod.azure.azurelib.common.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.common.animation.dispatch.command.action.AzAction;

/**
 * Sets a controller's blend weight, either immediately ({@code fadeTicks <= 0}) or as a fade over {@code fadeTicks}.
 */
public record AzControllerSetWeightAction(
    String controllerName,
    double weight,
    double fadeTicks
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzControllerSetWeightAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        AzControllerSetWeightAction::controllerName,
        ByteBufCodecs.DOUBLE,
        AzControllerSetWeightAction::weight,
        ByteBufCodecs.DOUBLE,
        AzControllerSetWeightAction::fadeTicks,
        AzControllerSetWeightAction::new
    );

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_weight");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.fadeWeight(weight, fadeTicks);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
