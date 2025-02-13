package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AzRootSetTransitionSpeedAction(
    float transitionSpeed
) implements AzAction {

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

    @Override
    public <T extends AzAction> void encode(@NotNull FriendlyByteBuf buf, @NotNull T action) {
        var speedAction = (AzRootSetTransitionSpeedAction) action;
        buf.writeFloat(speedAction.transitionSpeed());
    }

    @Override
    public <T extends AzAction> T decode(@NotNull FriendlyByteBuf buf, @NotNull Class<T> actionClass) {
        if (!AzRootSetTransitionSpeedAction.class.equals(actionClass)) {
            throw new IllegalArgumentException("Unsupported action class: " + actionClass.getName());
        }

        var transitionSpeed = buf.readFloat();

        @SuppressWarnings("unchecked")
        T action = (T) new AzRootSetTransitionSpeedAction(transitionSpeed);
        return action;

    }
}
