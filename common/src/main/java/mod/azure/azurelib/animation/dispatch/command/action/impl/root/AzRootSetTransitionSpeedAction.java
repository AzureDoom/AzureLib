package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

/**
 * Represents an action that sets the transition speed for all animation controllers within the associated
 * {@link AzAnimator}. This class is a record type, encapsulating a {@code float} value representing the transition
 * speed.
 */
public record AzRootSetTransitionSpeedAction(
    float transitionSpeed
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzRootSetTransitionSpeedAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        AzRootSetTransitionSpeedAction::transitionSpeed,
        AzRootSetTransitionSpeedAction::new
    );

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_transition_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withTransitionLength(transitionSpeed)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
