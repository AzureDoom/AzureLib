package mod.azure.azurelib.animation.controller;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.animation.pose.AzPoseLayer;
import mod.azure.azurelib.animation.pose.AzPoseModifierEntry;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.animation.primitive.AzPoseWeights;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;

import java.util.HashMap;
import java.util.Map;

final class AzPoseWeightDriver {

    private final AzAnimationController<?> controller;

    AzPoseWeightDriver(AzAnimationController<?> controller) {
        this.controller = controller;
    }

    void update() {
        AzAnimationContext<?> context = controller.animator().context();

        if (context == null) {
            return;
        }

        State state = state(context);
        AzQueuedAnimation current = controller.currentAnimation();
        AzAnimationControllerStateMachine<?> stateMachine = controller.stateMachine();
        AzBakedAnimation active = current == null || stateMachine.isStopped() ? null : current.animation();

        if (active != null && active.poseWeights().isEmpty()) {
            active = null;
        }

        AzPoseLayer<?> layer = context.poseLayer();
        double now = context.timer().getAnimTime();

        if (active != state.applied) {
            switchTo(layer, state, active, now);
        }

        if (active == null) {
            return;
        }

        double animTick = sampleTick(active);

        for (AzPoseWeights.Entry entry : active.poseWeights().entries()) {
            if (entry.track() == null) {
                continue;
            }

            double target = entry.targetAt(animTick);
            double fadeIn = entry.fadeIn();
            double elapsed = now - state.startedAt;

            if (fadeIn > 0 && elapsed < fadeIn) {
                double from = state.startWeight(entry.modifierId(), target);
                double progress = Math.max(0, elapsed / fadeIn);
                layer.setWeight(entry.modifierId(), from + (target - from) * progress);
            } else {
                layer.setWeight(entry.modifierId(), target);
            }
        }
    }

    private void switchTo(AzPoseLayer<?> layer, State state, AzBakedAnimation next, double now) {
        AzBakedAnimation previous = state.applied;
        AzPoseWeights nextWeights = next == null ? AzPoseWeights.NONE : next.poseWeights();

        if (previous != null) {
            for (AzPoseWeights.Entry entry : previous.poseWeights().entries()) {
                if (entry.restore() && nextWeights.get(entry.modifierId()) == null) {
                    layer.fadeWeight(entry.modifierId(), defaultWeight(entry.modifierId()), entry.fadeOut());
                }
            }
        }

        state.applied = next;
        state.startedAt = now;
        state.clearStartWeights();

        if (next == null) {
            return;
        }

        double animTick = sampleTick(next);

        for (AzPoseWeights.Entry entry : nextWeights.entries()) {
            String id = entry.modifierId();

            if (entry.track() != null) {
                state.rememberStartWeight(id, layer.getWeight(id));
            } else {
                layer.fadeWeight(id, entry.targetAt(animTick), entry.fadeIn());
            }
        }
    }

    private double sampleTick(AzBakedAnimation animation) {
        if (controller.stateMachine().isTransitioning()) {
            return controller.isPlayingReversed() ? animation.length() : 0;
        }

        return controller.sampleTick();
    }

    private double defaultWeight(String modifierId) {
        AzPoseModifierEntry<?> entry = controller.animator().getPoseModifiers().get(modifierId);
        return entry == null ? 1 : entry.defaultWeight();
    }

    private State state(AzAnimationContext<?> context) {
        return (State) context.attachments().computeIfAbsent(this, ignored -> new State());
    }

    private static final class State {

        private AzBakedAnimation applied;

        private double startedAt;

        private final Map<String, Double> startWeights = new HashMap<>(4);

        double startWeight(String modifierId, double fallback) {
            Double weight = startWeights.get(modifierId);
            return weight == null ? fallback : weight;
        }

        void rememberStartWeight(String modifierId, double weight) {
            startWeights.put(modifierId, weight);
        }

        void clearStartWeights() {
            startWeights.clear();
        }
    }
}
