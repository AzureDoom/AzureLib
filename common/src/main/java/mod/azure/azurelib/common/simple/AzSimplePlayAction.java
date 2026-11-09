package mod.azure.azurelib.common.simple;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.AzAnimator;
import mod.azure.azurelib.common.animation.controller.AzAnimationController;
import mod.azure.azurelib.common.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.common.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.common.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.common.animation.dispatch.command.action.impl.controller.AzControllerCancelAction;
import mod.azure.azurelib.common.animation.dispatch.command.action.impl.controller.AzControllerPlayAnimationSequenceAction;
import mod.azure.azurelib.common.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.common.animation.dispatch.command.sequence.AzAnimationSequenceBuilder;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;

public record AzSimplePlayAction(
    String controllerName,
    String animationName,
    AzSimplePlayMode mode
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzSimplePlayAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        AzSimplePlayAction::controllerName,
        ByteBufCodecs.STRING_UTF8,
        AzSimplePlayAction::animationName,
        AzSimplePlayMode.CODEC,
        AzSimplePlayAction::mode,
        AzSimplePlayAction::new
    );

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("simple/play");

    public AzSimplePlayAction {
        controllerName = controllerName == null ? "" : controllerName;
        animationName = animationName == null ? "" : animationName;
        mode = mode == null ? AzSimplePlayMode.STOP : mode;
    }

    public boolean hasController() {
        return !controllerName.isEmpty();
    }

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        if (animator instanceof AzSimpleAnimatorHandle handle) {
            handle.handleSimpleAction(originSide, this);
            return;
        }

        handleFallback(originSide, animator);
    }

    private void handleFallback(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationControllerContainer<?> container = animator.getAnimationControllerContainer();
        AzAnimationController<?> controller = hasController() ? container.getOrNull(controllerName) : null;

        if (controller == null && !hasController()) {
            Collection<? extends AzAnimationController<?>> all = container.getAll();
            controller = all.isEmpty() ? null : all.iterator().next();
        }

        if (controller == null || isBlockedByServer(originSide, controller)) {
            return;
        }

        if (mode == AzSimplePlayMode.STOP) {
            new AzControllerCancelAction(controller.name()).handle(originSide, animator);
            return;
        }

        AzPlayBehavior behavior = mode == AzSimplePlayMode.LOOP ? AzPlayBehaviors.LOOP : AzPlayBehaviors.PLAY_ONCE;
        AzAnimationSequence sequence = new AzAnimationSequenceBuilder()
            .queue(animationName, props -> props.withPlayBehavior(behavior))
            .build();

        new AzControllerPlayAnimationSequenceAction(controller.name(), sequence).handle(originSide, animator);
    }

    static boolean isBlockedByServer(AzDispatchSide originSide, AzAnimationController<?> controller) {
        return originSide == AzDispatchSide.CLIENT
            && controller.currentSequenceOrigin() == AzDispatchSide.SERVER
            && !controller.hasAnimationFinished();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
