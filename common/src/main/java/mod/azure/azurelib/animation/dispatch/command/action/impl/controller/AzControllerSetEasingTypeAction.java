package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.easing.AzEasingType;

public record AzControllerSetEasingTypeAction(
    String controllerName,
    AzEasingType easingType
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzControllerSetEasingTypeAction> DECODER = buf -> {
        String controllerName = buf.readUtf();
        AzEasingType easingType = AzEasingType.DECODER.apply(buf);
        return new AzControllerSetEasingTypeAction(controllerName, easingType);
    };

    public static final BiConsumer<FriendlyByteBuf, AzControllerSetEasingTypeAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName());
        AzEasingType.ENCODER.accept(buf, action.easingType());
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_easing_type");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withEasingType(easingType));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetEasingTypeAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf);
    }

    public static void encode(FriendlyByteBuf buf, AzControllerSetEasingTypeAction action) {
        ENCODER.accept(buf, action);
    }
}
