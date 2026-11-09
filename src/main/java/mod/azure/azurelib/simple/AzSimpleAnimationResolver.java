package mod.azure.azurelib.simple;

import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorUpdateHook;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerBuilder;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.AzControllerCancelAction;
import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.AzControllerPlayAnimationSequenceAction;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequenceBuilder;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.primitive.AzBakedAnimations;
import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

@SuppressWarnings("unused")
public final class AzSimpleAnimationResolver<T> implements AzAnimatorUpdateHook<T> {

    public enum Owner {

        INACTIVE,
        AUTOMATIC,
        TRIGGERED,
        EXTERNAL,
        RELEASING
    }

    private static final Map<String, AzAnimationSequence> SEQUENCES = new ConcurrentHashMap<>();

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private final AzAnimator<?, T> animator;

    private final AzSimpleConfig<T> config;

    private final AzAnimationSequence[] stateSequences;

    private final Map<String, Channel<T>> channelsByName = new LinkedHashMap<>();

    private final List<Channel<T>> layers = new ArrayList<>();

    private final Map<String, String> layerByAnimation = new HashMap<>();

    private Channel<T> base;

    public AzSimpleAnimationResolver(AzAnimator<?, T> animator, AzSimpleConfig<T> config) {
        this.animator = animator;
        this.config = config;

        List<AzSimpleConfig.State<T>> states = config.states();
        this.stateSequences = new AzAnimationSequence[states.size()];

        for (int i = 0; i < stateSequences.length; i++) {
            AzSimpleConfig.State<T> state = states.get(i);
            stateSequences[i] = sequence(state.animation(), state.behavior(), -1, 0);
        }
    }

    private static final class Channel<A> {

        private final String name;

        private final AzAnimationController<A> controller;

        private final AzSimpleConfig.Layer<A> layer;

        private final AzAnimationSequence automaticSequence;

        private Owner owner = Owner.INACTIVE;

        private AzAnimationSequence owned;

        private int priority;

        private AzSimpleInterruptPolicy policy = AzSimpleInterruptPolicy.ALWAYS;

        private boolean wantsOn;

        private Channel(String name, AzAnimationController<A> controller, AzSimpleConfig.Layer<A> layer) {
            this.name = name;
            this.controller = controller;
            this.layer = layer;
            this.automaticSequence = layer != null && layer.automatic() && layer.animation() != null
                ? sequence(layer.animation(), layer.behavior(), -1, 0)
                : null;
        }

        private boolean isAutomaticLayer() {
            return layer != null && layer.automatic();
        }

        private float targetWeight() {
            return layer != null ? layer.weight() : 1;
        }

        private int fadeTicks() {
            return layer != null ? layer.fadeTicks() : 0;
        }

        private void setOwner(Owner owner, AzAnimationSequence owned) {
            this.owner = owner;
            this.owned = owned;
        }
    }

    public void registerControllers(AzAnimationControllerContainer<T> container) {
        AzAnimationControllerBuilder<T> baseBuilder = AzAnimationController.builder(animator, AzSimple.BASE_CONTROLLER)
            .setAnimationSpeed(config.animationSpeed());

        if (config.transitionLength() >= 0) {
            baseBuilder.setTransitionLength(config.transitionLength());
        }

        AzAnimationController<T> baseController = baseBuilder.build();
        container.add(baseController);
        this.base = new Channel<>(AzSimple.BASE_CONTROLLER, baseController, null);
        channelsByName.put(AzSimple.BASE_CONTROLLER, base);

        for (AzSimpleConfig.Layer<T> layer : config.layers()) {
            AzAnimationControllerBuilder<T> builder = AzAnimationController.builder(animator, layer.name())
                .setBoneMask(layer.boneMask())
                .setBlendMode(layer.blendMode())
                .setWeight(layer.automatic() ? 0 : layer.weight());

            if (layer.transitionLength() >= 0) {
                builder.setTransitionLength(layer.transitionLength());
            }

            if (layer.speed() > 0) {
                builder.setAnimationSpeed(layer.speed());
            }

            AzAnimationController<T> controller = builder.build();
            container.add(controller);

            Channel<T> channel = new Channel<>(layer.name(), controller, layer);
            channelsByName.put(layer.name(), channel);
            layers.add(channel);

            if (!layer.automatic() && layer.animation() != null) {
                layerByAnimation.putIfAbsent(layer.animation(), layer.name());
            }
        }

        animator.addUpdateHook(this);
    }

    public void registerPoseModifiers(AzPoseModifierRegistry<T> modifiers) {
        for (AzSimpleConfig.PoseEntry<T> entry : config.poseModifiers()) {
            modifiers.add(entry.id(), entry.modifier()).priority(entry.priority());
        }
    }

    public AzSimpleConfig<T> config() {
        return config;
    }

    public AzAnimationController<T> baseController() {
        return base == null ? null : base.controller;
    }

    public AzAnimationController<T> layerController(String name) {
        Channel<T> channel = channelsByName.get(name);
        return channel == null || channel == base ? null : channel.controller;
    }

    public Owner owner(String controllerName) {
        Channel<T> channel = channelsByName.get(controllerName);

        if (channel == null) {
            return null;
        }

        sync(channel);
        return channel.owner;
    }

    public boolean isPlayingTriggeredAnimation() {
        return owner(AzSimple.BASE_CONTROLLER) == Owner.TRIGGERED;
    }

    private void sync(Channel<T> channel) {
        AzAnimationController<T> controller = channel.controller;
        AzAnimationSequence current = controller.currentSequence();

        if (current == null || controller.hasAnimationFinished()) {
            channel.setOwner(Owner.INACTIVE, null);
            return;
        }

        if (channel.owned != null && channel.owned.equals(current)) {
            return;
        }

        Owner previous = channel.owner;
        channel.setOwner(Owner.EXTERNAL, null);

        if (channel.layer != null && (previous == Owner.RELEASING || channel.isAutomaticLayer() && !channel.wantsOn)) {
            controller.setWeight(channel.targetWeight());
        }
    }

    private boolean start(
        Channel<T> channel,
        Owner owner,
        AzAnimationSequence sequence,
        AzDispatchSide side,
        boolean cancelFirst,
        boolean linked
    ) {
        if (cancelFirst) {
            send(new AzControllerCancelAction(channel.name), side, linked);
        }

        send(new AzControllerPlayAnimationSequenceAction(channel.name, sequence), side, linked);

        AzAnimationController<T> controller = channel.controller;

        if (!sequence.equals(controller.currentSequence()) || controller.hasAnimationFinished()) {
            return false;
        }

        channel.setOwner(owner, sequence);
        return true;
    }

    private void cancel(Channel<T> channel, AzDispatchSide side, boolean linked) {
        send(new AzControllerCancelAction(channel.name), side, linked);
        channel.setOwner(Owner.INACTIVE, null);
    }

    private void send(AzAction action, AzDispatchSide side, boolean linked) {
        action.handle(side, animator);

        if (linked) {
            animator.forEachLinkedAnimator(other -> action.handle(side, other));
        }
    }

    @Override
    public void beforeControllers(T animatable, float partialTicks) {
        if (base == null || animatable == null) {
            return;
        }

        updateBase(animatable);

        for (int i = 0, n = layers.size(); i < n; i++) {
            updateLayer(animatable, layers.get(i));
        }
    }

    private void updateBase(T animatable) {
        Channel<T> channel = base;
        sync(channel);

        int index = resolveState(animatable);

        if (index < 0) {
            return;
        }

        AzSimpleConfig.State<T> state = config.states().get(index);
        AzAnimationSequence sequence = stateSequences[index];

        switch (channel.owner) {
            case INACTIVE:
                startState(animatable, channel, state, sequence, false);
                break;

            case AUTOMATIC:
                if (!sequence.equals(channel.owned)) {
                    startState(animatable, channel, state, sequence, false);
                }
                break;

            case TRIGGERED:
                if (state.interruptsActions() && channel.policy.allows(channel.priority, state.priority())) {
                    startState(animatable, channel, state, sequence, true);
                }
                break;

            case EXTERNAL:
                if (state.interruptsActions()) {
                    startState(animatable, channel, state, sequence, true);
                }
                break;

            case RELEASING:
                break;
        }
    }

    private void startState(
        T animatable,
        Channel<T> channel,
        AzSimpleConfig.State<T> state,
        AzAnimationSequence sequence,
        boolean interrupting
    ) {
        if (hasAnimation(animatable, state.animation())) {
            start(channel, Owner.AUTOMATIC, sequence, AzDispatchSide.CLIENT, interrupting, true);
        }
    }

    private int resolveState(T animatable) {
        List<AzSimpleConfig.State<T>> states = config.states();

        for (int i = 0, n = states.size(); i < n; i++) {
            if (states.get(i).condition().test(animatable)) {
                return i;
            }
        }

        return -1;
    }

    private void updateLayer(T animatable, Channel<T> channel) {
        sync(channel);

        AzAnimationController<T> controller = channel.controller;
        AzSimpleConfig.Layer<T> layer = channel.layer;

        if (layer != null && layer.automatic()) {
            Predicate<T> condition = layer.condition();
            boolean on = condition == null || condition.test(animatable);

            if (on && !channel.wantsOn) {
                channel.wantsOn = true;

                if (channel.owner == Owner.RELEASING && channel.automaticSequence.equals(channel.owned)) {
                    channel.owner = Owner.AUTOMATIC;
                    controller.fadeWeight(channel.targetWeight(), channel.fadeTicks());
                }
            } else if (!on && channel.wantsOn) {
                channel.wantsOn = false;

                if (channel.owner == Owner.AUTOMATIC) {
                    channel.owner = Owner.RELEASING;
                    controller.fadeWeight(0, channel.fadeTicks());
                }
            }

            if (channel.wantsOn && channel.owner == Owner.INACTIVE && hasAnimation(animatable, layer.animation())) {
                if (start(channel, Owner.AUTOMATIC, channel.automaticSequence, AzDispatchSide.CLIENT, false, true)) {
                    controller.setWeight(0);
                    controller.fadeWeight(channel.targetWeight(), channel.fadeTicks());
                }
            }
        }

        if (channel.owner == Owner.RELEASING && !controller.isFadingWeight()) {
            cancel(channel, AzDispatchSide.CLIENT, true);
        }
    }

    public void handle(AzDispatchSide originSide, AzSimplePlayAction action) {
        if (base == null) {
            return;
        }

        if (action.mode() == AzSimplePlayMode.STOP) {
            stop(originSide, action);
        } else {
            play(originSide, action);
        }
    }

    private void play(AzDispatchSide originSide, AzSimplePlayAction action) {
        AzAnimationContext<T> context = animator.context();
        T animatable = context == null ? null : context.animatable();
        String name = action.animationName();
        AzSimpleConfig.Action settings = config.action(name);
        String target = targetFor(action, settings);
        AzSimpleAnimationResolver.Channel<T> channel = channelsByName.get(target);

        if (channel == null) {
            warnOnce("layer:" + target, "AzSimple: no layer named '{}' to play '{}' on", target, name);
            return;
        }

        if (animatable == null || !hasAnimation(animatable, name)) {
            return;
        }

        sync(channel);

        if (channel.owner == Owner.TRIGGERED && !channel.policy.allows(channel.priority, settings.priority())) {
            return;
        }

        if (AzSimplePlayAction.isBlockedByServer(originSide, channel.controller)) {
            return;
        }

        AzPlayBehavior behavior = action.mode() == AzSimplePlayMode.LOOP
            ? AzPlayBehaviors.LOOP
            : settings.hold() ? AzPlayBehaviors.HOLD_ON_LAST_FRAME : AzPlayBehaviors.PLAY_ONCE;
        AzAnimationSequence sequence = sequence(name, behavior, settings.transitionLength(), settings.speed());
        boolean alreadyRunning = sequence.equals(channel.controller.currentSequence());

        if (alreadyRunning && behavior == AzPlayBehaviors.LOOP) {
            channel.setOwner(Owner.TRIGGERED, sequence);
        } else if (!start(channel, Owner.TRIGGERED, sequence, originSide, alreadyRunning, false)) {
            return;
        }

        channel.priority = settings.priority();
        channel.policy = settings.interruptPolicy();

        if (channel.layer != null) {
            channel.controller.setWeight(channel.targetWeight());
        }
    }

    private void stop(AzDispatchSide originSide, AzSimplePlayAction action) {
        if (action.hasController()) {
            AzSimpleAnimationResolver.Channel<T> channel = channelsByName.get(action.controllerName());

            if (channel != null) {
                stop(originSide, channel);
            }

            return;
        }

        stop(originSide, base);

        for (AzSimpleAnimationResolver.Channel<T> layer : layers) {
            stop(originSide, layer);
        }
    }

    private void stop(AzDispatchSide originSide, Channel<T> channel) {
        sync(channel);

        if (channel.owner != Owner.TRIGGERED && channel.owner != Owner.EXTERNAL) {
            return;
        }

        AzAnimationController<T> controller = channel.controller;

        if (AzSimplePlayAction.isBlockedByServer(originSide, controller)) {
            return;
        }

        if (channel.layer != null && channel.fadeTicks() > 0 && controller.weight() > 0) {
            channel.setOwner(Owner.RELEASING, controller.currentSequence());
            controller.fadeWeight(0, channel.fadeTicks());
            return;
        }

        cancel(channel, originSide, false);
    }

    private String targetFor(AzSimplePlayAction action, AzSimpleConfig.Action settings) {
        if (action.hasController()) {
            return action.controllerName();
        }

        if (settings.layer() != null) {
            return settings.layer();
        }

        return layerByAnimation.getOrDefault(action.animationName(), AzSimple.BASE_CONTROLLER);
    }

    private boolean hasAnimation(T animatable, String name) {
        if (name == null) {
            return false;
        }

        ResourceLocation location = animator.getAnimationLocation(animatable);
        AzBakedAnimations animations = AzBakedAnimationCache.getInstance().getNullable(location);

        if (animations == null) {
            warnOnce("file:" + location, "AzSimple: animation file {} was not found", location);
            return false;
        }

        if (animations.getAnimation(name) == null) {
            warnOnce(
                "anim:" + location + "#" + name,
                "AzSimple: animation '{}' was not found in {}",
                name,
                location
            );
            return false;
        }

        return true;
    }

    private static void warnOnce(String key, String message, Object... args) {
        if (WARNED.add(key)) {
            AzureLib.LOGGER.warn(message, args);
        }
    }

    static AzAnimationSequence sequence(String name, AzPlayBehavior behavior, int transitionLength, double speed) {
        String key = name + "\n" + behavior.name() + "\n" + transitionLength + "\n" + speed;

        return SEQUENCES.computeIfAbsent(
            key,
            ignored -> new AzAnimationSequenceBuilder().queue(name, props -> {
                AzAnimationStageProperties stage = props.withPlayBehavior(behavior);

                if (transitionLength >= 0) {
                    stage = stage.withTransitionLength(transitionLength);
                }

                if (speed > 0) {
                    stage = stage.withAnimationSpeed(speed);
                }

                return stage;
            }).build()
        );
    }
}
