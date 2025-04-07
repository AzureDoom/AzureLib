package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.sequence.AzAnimationSequence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record AzRootPlayAnimationSequenceAction(
    String controllerName,
    AzAnimationSequence sequence
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzRootPlayAnimationSequenceAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        AzRootPlayAnimationSequenceAction::controllerName,
        AzAnimationSequence.CODEC,
        AzRootPlayAnimationSequenceAction::sequence,
        AzRootPlayAnimationSequenceAction::new
    );

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
}
