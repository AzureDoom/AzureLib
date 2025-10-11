package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.easing.AzEasingType;

public record AzRootSetEasingTypeAction(
    AzEasingType easingType
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzRootSetEasingTypeAction> DECODER = buf -> {
        AzEasingType easingType = AzEasingType.DECODER.apply(buf);
        return new AzRootSetEasingTypeAction(easingType);
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootSetEasingTypeAction> ENCODER = (buf, action) -> {
        AzEasingType.ENCODER.accept(buf, action.easingType());
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_easing_type");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withEasingType(easingType)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetEasingTypeAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf);
    }

    public static void encode(FriendlyByteBuf buf, AzRootSetEasingTypeAction action) {
        ENCODER.accept(buf, action);
    }

}
