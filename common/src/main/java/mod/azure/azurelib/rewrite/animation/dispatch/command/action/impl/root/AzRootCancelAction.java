package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an action to cancel the current animation, associated with a specific animation controller by its name.
 * This action is part of the AzureLib animation framework and is used to stop an ongoing animation
 * by clearing the current animation state of the specified controller.
 * <br>
 * This class implements the {@link AzAction} interface, allowing it to be dispatched
 * to modify animation states within an {@link AzAnimator}.
 * <br>
 * The action can be serialized and deserialized for network communication or storage purposes.
 */
public record AzRootCancelAction(
    String controllerName
) implements AzAction {

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/cancel");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setCurrentAnimation(null);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    @Override
    public <T extends AzAction> void encode(@NotNull FriendlyByteBuf buf, @NotNull T action) {
        var cancelAction = (AzRootCancelAction) action;
        buf.writeUtf(cancelAction.controllerName);
    }

    @Override
    public <T extends AzAction> T decode(@NotNull FriendlyByteBuf buf) {
        String controllerName = buf.readUtf();
        try {
            return actionClass.getDeclaredConstructor(String.class).newInstance(controllerName);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to decode action for class: " + actionClass.getName(), e);
        }
    }
}
