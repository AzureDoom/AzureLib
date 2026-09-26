package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

/**
 * Sets a controller's blend weight, either immediately ({@code fadeTicks <= 0}) or as a fade over {@code fadeTicks}.
 */
public record AzControllerSetWeightAction(
    String controllerName,
    double weight,
    double fadeTicks
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerSetWeightAction> DECODER = buf -> {
        String controllerName = buf.readUtf();
        double weight = buf.readDouble();
        double fadeTicks = buf.readDouble();

        return new AzControllerSetWeightAction(
            controllerName,
            weight,
            fadeTicks
        );
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerSetWeightAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName());
        buf.writeDouble(action.weight());
        buf.writeDouble(action.fadeTicks());
    };

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

    public static AzControllerSetWeightAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf);
    }

    public static void encode(FriendlyByteBuf buf, AzControllerSetWeightAction action) {
        ENCODER.accept(buf, action);
    }
}
