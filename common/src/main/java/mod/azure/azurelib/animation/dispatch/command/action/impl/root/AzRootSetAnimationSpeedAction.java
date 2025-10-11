package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzRootSetAnimationSpeedAction(
    double animationSpeed
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzRootSetAnimationSpeedAction> DECODER = buf -> {
        double animationSpeed = buf.readDouble(); // Read double from the buffer
        return new AzRootSetAnimationSpeedAction(animationSpeed); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootSetAnimationSpeedAction> ENCODER = (buf, action) -> {
        buf.writeDouble(action.animationSpeed()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_animation_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withAnimationSpeed(animationSpeed)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetAnimationSpeedAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzRootSetAnimationSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
