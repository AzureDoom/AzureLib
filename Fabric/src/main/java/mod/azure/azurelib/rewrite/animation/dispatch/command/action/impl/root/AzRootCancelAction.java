package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationController;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;

/**
 * Represents an action that cancels the current animation of a specified animation controller in the animation system.
 */
public class AzRootCancelAction implements AzAction {

    private final String controllerName;

    public static final Function<FriendlyByteBuf, AzRootCancelAction> DECODER = buf -> {
        String controllerName = buf.readUtf(); // Read UTF-8 string for the controller's name
        return new AzRootCancelAction(controllerName);
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootCancelAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName()); // Write UTF-8 string for the controller's name
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/cancel");

    public AzRootCancelAction(String controllerName) {
        this.controllerName = controllerName;
    }

    public String controllerName() {
        return controllerName;
    }

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setCurrentAnimation(null);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootCancelAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf); // Delegate to the DECODER functional interface
    }

    public static void encode(FriendlyByteBuf buf, AzRootCancelAction action) {
        ENCODER.accept(buf, action); // Delegate to the ENCODER functional interface
    }

    @Override
    public String toString() {
        return "AzRootCancelAction{" +
            "controllerName='" + controllerName + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AzRootCancelAction that = (AzRootCancelAction) o;

        return controllerName.equals(that.controllerName);
    }

    @Override
    public int hashCode() {
        return controllerName.hashCode();
    }
}
