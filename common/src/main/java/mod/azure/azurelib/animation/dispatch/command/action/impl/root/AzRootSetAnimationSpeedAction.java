package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

/**
 * Represents an action for setting the animation speed of all animation controllers in an {@link AzAnimator} instance.
 * This action is part of the AzureLib animation system and encapsulates the behavior for modifying the animation speed
 * property.
 */
public record AzRootSetAnimationSpeedAction(
    double animationSpeed
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzRootSetAnimationSpeedAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE,
        AzRootSetAnimationSpeedAction::animationSpeed,
        AzRootSetAnimationSpeedAction::new
    );

    public static final Identifier RESOURCE_LOCATION = AzureLib.modResource("root/set_animation_speed");

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
    public Identifier getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
