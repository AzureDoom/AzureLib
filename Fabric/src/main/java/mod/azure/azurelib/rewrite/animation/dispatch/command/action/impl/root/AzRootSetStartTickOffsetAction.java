package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;

public class AzRootSetStartTickOffsetAction implements AzAction {

    private double startTickOffset;

    public AzRootSetStartTickOffsetAction(double startTickOffset) {
        this.startTickOffset = startTickOffset;
    }

    public static final Function<FriendlyByteBuf, AzRootSetStartTickOffsetAction> DECODER = buf -> {
        double startTickOffset = buf.readDouble(); // Read double from the buffer
        return new AzRootSetStartTickOffsetAction(startTickOffset); // Create a new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootSetStartTickOffsetAction> ENCODER = (buf, action) -> {
        buf.writeDouble(action.startTickOffset()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_start_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withStartTickOffset(startTickOffset)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public double startTickOffset() {
        return startTickOffset;
    }

    public static AzRootSetStartTickOffsetAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzRootSetStartTickOffsetAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
