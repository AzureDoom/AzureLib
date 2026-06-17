package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;

/**
 * Represents an action that plays a specified animation sequence on all animation controllers. This class is a concrete
 * implementation of the {@link AzAction} interface, encapsulating the details required to trigger and manage animation
 * sequences within the AzureLib animation system. It allows the animation sequence to be applied to one or more
 * animation controllers identified by a controller name.
 */
public record AzRootPlayAnimationSequenceAction(
    AzAnimationSequence sequence
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzRootPlayAnimationSequenceAction> CODEC = StreamCodec.composite(
        AzAnimationSequence.CODEC,
        AzRootPlayAnimationSequenceAction::sequence,
        AzRootPlayAnimationSequenceAction::new
    );

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/play_animation_sequence");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controllerContainer = animator.getAnimationControllerContainer();
        var controllers = controllerContainer.getAll();

        controllers.forEach(controller -> controller.run(originSide, sequence));
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
