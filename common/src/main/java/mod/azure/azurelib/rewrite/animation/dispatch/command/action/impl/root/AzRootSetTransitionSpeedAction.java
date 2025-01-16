package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;

/**
 * The {@code AzRootSetTransitionSpeedAction} class implements the {@link AzAction} interface and represents an action
 * that modifies the transition speed for an animator during an animation state. This action is intended for use within
 * the animation system to adjust the transition timing of animations. This class provides a unique resource location
 * identifier for this specific action and handles the logic required to apply the transition speed modification to the
 * target {@link AzAnimator}. It utilizes {@link StreamCodec} for serialization and deserialization of this action.
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
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
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
