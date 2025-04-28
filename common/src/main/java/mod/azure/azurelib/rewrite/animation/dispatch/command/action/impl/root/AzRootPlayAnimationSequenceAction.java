package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.sequence.AzAnimationSequence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record AzRootPlayAnimationSequenceAction(
    String controllerName,
    AzAnimationSequence sequence
) implements AzAction {

    public static final Function<FriendlyByteBuf, AzRootPlayAnimationSequenceAction> DECODER = buf -> {
        String controllerName = buf.readUtf(); // Read controller name (UTF string)
        AzAnimationSequence sequence = AzAnimationSequence.DECODER.apply(buf); // Decode AzAnimationSequence
        return new AzRootPlayAnimationSequenceAction(controllerName, sequence); // Create new instance
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootPlayAnimationSequenceAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName()); // Write controller name (UTF string)
        AzAnimationSequence.ENCODER.accept(buf, action.sequence()); // Encode AzAnimationSequence
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/play_animation_sequence");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.run(originSide, sequence);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootPlayAnimationSequenceAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(FriendlyByteBuf buf, AzRootPlayAnimationSequenceAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
