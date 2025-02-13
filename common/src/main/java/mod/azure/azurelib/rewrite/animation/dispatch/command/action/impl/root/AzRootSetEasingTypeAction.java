package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.easing.AzEasingType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AzRootSetEasingTypeAction(
    AzEasingType easingType
) implements AzAction {

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_easing_type");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withEasingType(easingType)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    @Override
    public <T extends AzAction> void encode(@NotNull FriendlyByteBuf buf, @NotNull T action) {
        var easingAction = (AzRootSetEasingTypeAction) action;
        AzEasingType.encode(buf, easingAction.easingType());
    }

    @Override
    public <T extends AzAction> T decode(@NotNull FriendlyByteBuf buf, @NotNull Class<T> actionClass) {
        if (!AzRootSetEasingTypeAction.class.equals(actionClass)) {
            throw new IllegalArgumentException("Unsupported action class: " + actionClass.getName());
        }

        @SuppressWarnings("unchecked")
        T action = (T) new AzRootSetEasingTypeAction(easingType);
        return action;

    }
}
