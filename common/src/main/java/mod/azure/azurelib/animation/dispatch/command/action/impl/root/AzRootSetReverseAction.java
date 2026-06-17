package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public record AzRootSetReverseAction(
    boolean hasReverse
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzRootSetReverseAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        AzRootSetReverseAction::hasReverse,
        AzRootSetReverseAction::new
    );

    public static final Identifier RESOURCE_LOCATION = AzureLib.modResource(
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
    public Identifier getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
