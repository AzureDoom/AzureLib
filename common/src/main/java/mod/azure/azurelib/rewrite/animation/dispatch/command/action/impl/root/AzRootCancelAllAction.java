package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class AzRootCancelAllAction implements AzAction {

    public static final AzRootCancelAllAction INSTANCE = new AzRootCancelAllAction();

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/cancel_all");

    public static AzRootCancelAllAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        var controllerContainer = animator.getAnimationControllerContainer();
        var controllers = controllerContainer.getAll();

        controllers.forEach(controller -> controller.setCurrentAnimation(null));
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    @Override
    public <T extends AzAction> void encode(@NotNull FriendlyByteBuf buf, @NotNull T action) {

    }

    @Override
    public <T extends AzAction> T decode(@NotNull FriendlyByteBuf buf, @NotNull Class<T> actionClass) {
        if (!AzRootCancelAllAction.class.equals(actionClass)) {
            throw new IllegalArgumentException("Unsupported action class: " + actionClass.getName());
        }

        // Safe cast because we ensured the class type is AzRootCancelAllAction
        @SuppressWarnings("unchecked")
        T action = (T) INSTANCE;

        return action;
    }
}
