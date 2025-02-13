package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.sequence.AzAnimationSequence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AzRootPlayAnimationSequenceAction(
    String controllerName,
    AzAnimationSequence sequence
) implements AzAction {

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/play_animation_sequence");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.run(originSide, sequence);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    @Override
    public <T extends AzAction> void encode(@NotNull FriendlyByteBuf buf, @NotNull T action) {
        var rootAction = (AzRootPlayAnimationSequenceAction) action;
        buf.writeUtf(rootAction.controllerName());
        AzAnimationSequence.encode(buf, rootAction.sequence());
    }

    @Override
    public <T extends AzAction> T decode(@NotNull FriendlyByteBuf buf, @NotNull Class<T> actionClass) {
        if (!AzRootPlayAnimationSequenceAction.class.equals(actionClass)) {
            throw new IllegalArgumentException("Unsupported action class: " + actionClass.getName());
        }

        var controllerName = buf.readUtf();

        @SuppressWarnings("unchecked")
        T action = (T) new AzRootPlayAnimationSequenceAction(controllerName, sequence);
        return action;
    }
}
