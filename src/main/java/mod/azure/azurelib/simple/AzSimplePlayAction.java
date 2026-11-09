package mod.azure.azurelib.simple;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.AzControllerCancelAction;
import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.AzControllerPlayAnimationSequenceAction;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequenceBuilder;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;

public final class AzSimplePlayAction implements AzAction {

    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation("azurelib", "simple/play");

    public static final Function<PacketBuffer, AzSimplePlayAction> DECODER = buf -> new AzSimplePlayAction(
        buf.readString(32767),
        buf.readString(32767),
        AzSimplePlayMode.decode(buf)
    );

    public static final BiConsumer<PacketBuffer, AzSimplePlayAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName());
        buf.writeString(action.animationName());
        AzSimplePlayMode.encode(buf, action.mode());
    };

    private final String controllerName;

    private final String animationName;

    private final AzSimplePlayMode mode;

    public AzSimplePlayAction(String controllerName, String animationName, AzSimplePlayMode mode) {
        this.controllerName = controllerName == null ? "" : controllerName;
        this.animationName = animationName == null ? "" : animationName;
        this.mode = mode == null ? AzSimplePlayMode.STOP : mode;
    }

    public String controllerName() {
        return controllerName;
    }

    public String animationName() {
        return animationName;
    }

    public AzSimplePlayMode mode() {
        return mode;
    }

    public static AzSimplePlayAction decode(PacketBuffer buf) {
        return DECODER.apply(buf);
    }

    public static void encode(PacketBuffer buf, AzSimplePlayAction action) {
        ENCODER.accept(buf, action);
    }

    public boolean hasController() {
        return !controllerName.isEmpty();
    }

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        if (animator instanceof AzSimpleAnimatorHandle) {
            AzSimpleAnimatorHandle handle = (AzSimpleAnimatorHandle) animator;
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

        AzPlayBehavior behavior = mode == AzSimplePlayMode.LOOP
            ? AzPlayBehaviors.LOOP
            : AzPlayBehaviors.PLAY_ONCE;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AzSimplePlayAction)) {
            return false;
        }
        AzSimplePlayAction other = (AzSimplePlayAction) obj;
        return controllerName.equals(other.controllerName)
            && animationName.equals(other.animationName)
            && mode == other.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(controllerName, animationName, mode);
    }

    @Override
    public String toString() {
        return "AzSimplePlayAction[controllerName=" + controllerName
            + ", animationName=" + animationName
            + ", mode=" + mode + "]";
    }
}
