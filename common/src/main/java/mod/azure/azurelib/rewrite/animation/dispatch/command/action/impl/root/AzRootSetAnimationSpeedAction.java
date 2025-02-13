package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AzRootSetAnimationSpeedAction(
    double animationSpeed
) implements AzAction {

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_animation_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withAnimationSpeed(animationSpeed)
                )
            );
    }

    @Override
    public <T extends AzAction> void encode(@NotNull FriendlyByteBuf buf, @NotNull T action) {
        buf.writeDouble(animationSpeed);
    }

    @Override
    public <T extends AzAction> T decode(@NotNull FriendlyByteBuf buf, @NotNull Class<T> actionClass) {
        Double animationSpeed = buf.readDouble();
        try {
            return actionClass.getDeclaredConstructor(Double.class).newInstance(animationSpeed);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to decode action for class: " + actionClass.getName(), e);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
