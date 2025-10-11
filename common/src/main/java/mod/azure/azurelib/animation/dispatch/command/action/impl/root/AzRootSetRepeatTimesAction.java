package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzRootSetRepeatTimesAction(
    double repeatXTimes
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzRootSetRepeatTimesAction> DECODER = buf -> {
        double repeatXTimes = buf.readDouble(); // Read double from the buffer
        return new AzRootSetRepeatTimesAction(repeatXTimes); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootSetRepeatTimesAction> ENCODER = (buf, action) -> {
        buf.writeDouble(action.repeatXTimes()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_repeat_times_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withRepeatXTimes(repeatXTimes)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetRepeatTimesAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzRootSetRepeatTimesAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
