package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzRootSetReverseAction(
    boolean hasReverse
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzRootSetReverseAction> DECODER = buf -> {
        boolean hasReverse = buf.readBoolean(); // Read boolean from the buffer
        return new AzRootSetReverseAction(hasReverse); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootSetReverseAction> ENCODER = (buf, action) -> {
        buf.writeBoolean(action.hasReverse()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource(
        "root/set_reverse_tick_offset"
    );

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withShouldReverse(hasReverse)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetReverseAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzRootSetReverseAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
