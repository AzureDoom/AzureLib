package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.rewrite.animation.controller.AzAnimationController;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationControllerContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;

/**
 * The AzRootCancelAllAction class implements the AzAction interface and defines an action that cancels all ongoing
 * animations within an animator by setting the current animation of all controllers to null. <br>
 * This class is designed to work within a system that manages animations for objects using animation controllers. Once
 * this action is handled, all animation controllers associated with a specific animator will have their current
 * animations cleared.
 */
public class AzRootCancelAllAction implements AzAction {

    public static final AzRootCancelAllAction INSTANCE = new AzRootCancelAllAction();

    public static final Function<FriendlyByteBuf, AzRootCancelAllAction> DECODER = buf -> INSTANCE;

    public static final BiConsumer<FriendlyByteBuf, AzRootCancelAllAction> ENCODER = (buf, action) -> {
        // No data to write since this is a singleton
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/cancel_all");

    private AzRootCancelAllAction() {}

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        AzAnimationControllerContainer<?> controllerContainer = animator.getAnimationControllerContainer();
        Collection<? extends AzAnimationController<?>> controllers = controllerContainer.getAll();

        controllers.forEach(controller -> controller.setCurrentAnimation(null));
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootCancelAllAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Always returns the singleton instance
    }

    public static void encode(FriendlyByteBuf buf, AzRootCancelAllAction action) {
        ENCODER.accept(buf, action); // Does nothing since no data is encoded
    }
}
