package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzRootSetFreezeTickAction(
    double freezeTickOffset
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzRootSetFreezeTickAction> DECODER = buf -> {
        double freezeTickOffset = buf.readDouble(); // Read double from the buffer
        return new AzRootSetFreezeTickAction(freezeTickOffset); // Create new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootSetFreezeTickAction> ENCODER = (buf, action) -> {
        buf.writeDouble(action.freezeTickOffset()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_freeze_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withFreezeTickOffset(freezeTickOffset)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetFreezeTickAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzRootSetFreezeTickAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
