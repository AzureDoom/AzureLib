package mod.azure.azurelib.common.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.AzAnimator;
import mod.azure.azurelib.common.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.common.animation.dispatch.command.action.AzAction;

public record AzRootSetFreezeTickAction(
    double freezeTickOffset
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzRootSetFreezeTickAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE,
        AzRootSetFreezeTickAction::freezeTickOffset,
        AzRootSetFreezeTickAction::new
    );

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
}
