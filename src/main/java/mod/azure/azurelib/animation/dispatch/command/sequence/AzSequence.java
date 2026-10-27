package mod.azure.azurelib.animation.dispatch.command.sequence;

import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.function.UnaryOperator;

import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

/**
 * A reusable, immutable description of an ordered chain of animations, optionally annotated with timed gameplay events.
 * {@code AzSequence} is a high-level front end for {@link AzAnimationSequence}: it produces the same network payload,
 * but reads the way you think about an attack or ability.
 * <p>
 * There are two equivalent ways to create one. The chain style returns a new sequence from every call, so it is safe to
 * keep partially-built sequences around and extend them:
 *
 * <pre>
 *
 * {
 *     &#64;code
 *
 *     AzSequence attack = AzSequence.create()
 *         .then("attack_windup", AzPlayBehaviors.PLAY_ONCE)
 *         .then("attack", AzPlayBehaviors.PLAY_ONCE)
 *         .then("idle", AzPlayBehaviors.LOOP);
 * }
 * </pre>
 *
 * The builder style avoids intermediate copies and is the natural choice for {@code static final} constants:
 *
 * <pre>
 *
 * {
 *     &#64;code
 *
 *     static final AzSequence SLAM = AzSequence.builder()
 *         .play("windup")
 *         .event("damage", 12)
 *         .play("strike")
 *         .hold("recovery")
 *         .build();
 * }
 * </pre>
 *
 * <h2>Stages</h2> {@link #play(String)} plays once and continues, {@link #loop(String)} loops forever,
 * {@link #hold(String)} freezes on the final frame, and {@link #then(String, AzPlayBehavior)} accepts any behavior.
 * Looping, holding and freezing stages never finish, so adding a stage after one of them is rejected with an
 * {@link IllegalStateException} instead of silently producing an unreachable stage.
 * <h2>Stage settings</h2> Besides its behavior, a stage can set its own speed, transition length, easing, start offset,
 * freeze point, repeat count and direction through the customizer of
 * {@link #then(String, AzPlayBehavior, UnaryOperator)}. These apply to that stage only and are layered over the
 * controller's settings while it plays, so they never leak into whatever plays next (unlike the controller-wide setters
 * on {@link AzCommand} builders).
 * <h2>Events</h2> Events are not sent to the client: they are delivered by an {@link AzSequencePlayer}, which is what
 * lets them drive server-side logic such as dealing damage. There are two kinds:
 * <ul>
 * <li>{@link #event(String, int)} fires a fixed number of game ticks after the sequence starts, regardless of speed or
 * transitions. Its position in the chain is only for readability.</li>
 * <li>{@link Builder#eventAt(String, double)}, {@link Builder#eventOnStart(String)} and
 * {@link Builder#eventOnEnd(String)} are anchored to the stage they follow: "when this animation reaches keyframe tick
 * 12", "when this stage starts playing", "when it finishes". The player places them using an {@link AzSequenceTiming},
 * so they stay in step with the animation when its speed, transition length, start offset or repeat count changes, and
 * when earlier stages change length.</li>
 * </ul>
 *
 * <pre>
 *
 * {
 *     &#64;code
 *
 *     static final AzSequence SLAM = AzSequence.builder()
 *         .play("windup")
 *         .then("strike", AzPlayBehaviors.PLAY_ONCE, props -> props.withAnimationSpeed(1.5))
 *         .eventAt("damage", 8) // keyframe tick 8 of "strike", at 1.5x speed, after the transition into it
 *         .eventOnEnd("recovered")
 *         .play("recovery")
 *         .build();
 * }
 * </pre>
 *
 * <h2>Playing</h2> Use {@link #toCommand(String)} / {@link #toRootCommand()} to get a normal {@link AzCommand}, or an
 * {@link AzSequencePlayer} when the sequence has events.
 */
public final class AzSequence {

    private static final AzSequence EMPTY = new AzSequence(Collections.emptyList(), Collections.emptyList());

    /**
     * Built-in behaviors that never finish on their own; anything queued after them could never play.
     */
    private static final Set<AzPlayBehavior> NON_FINISHING_BEHAVIORS = ImmutableSet.of(
        AzPlayBehaviors.LOOP,
        AzPlayBehaviors.PING_PONG,
        AzPlayBehaviors.HOLD_ON_LAST_FRAME,
        AzPlayBehaviors.FREEZE_ON_FRAME
    );

    private static final Comparator<AzSequenceEvent> EVENT_ORDER = Comparator
        .comparing(AzSequenceEvent::isAnchored)
        .thenComparingInt(AzSequenceEvent::tick)
        .thenComparingInt(AzSequenceEvent::stage)
        .thenComparingInt(event -> {
            switch (event.anchor()) {
                case SEQUENCE:
                case STAGE_START:
                    return 0;
                case KEYFRAME:
                    return 1;
                case STAGE_END:
                    return 2;
                default:
                    throw new IllegalStateException("Unexpected anchor: " + event.anchor());
            }
        })
        .thenComparingDouble(AzSequenceEvent::stageTick);

    private final List<AzAnimationStage> stages;

    private final List<AzSequenceEvent> events;

    private final AzAnimationSequence animationSequence;

    private AzSequence(List<AzAnimationStage> stages, List<AzSequenceEvent> events) {
        this.stages = Collections.unmodifiableList(new ArrayList<>(stages));

        ArrayList<AzSequenceEvent> sortedEvents = new ArrayList<>(events);
        // Stable sort: events on the same tick fire in declaration order. Fixed-tick events first by tick, then
        // anchored events by stage and position within it; the player re-sorts by resolved time anyway.
        sortedEvents.sort(EVENT_ORDER);
        this.events = Collections.unmodifiableList(new ArrayList<>(sortedEvents));

        this.animationSequence = new AzAnimationSequence(this.stages);
    }

    /**
     * @return an empty sequence to start a chain from
     */
    public static AzSequence create() {
        return EMPTY;
    }

    /**
     * @return a mutable builder; call {@link Builder#build()} when finished
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a new sequence with {@code animationName} appended, played with the given behavior.
     */
    public AzSequence then(String animationName, AzPlayBehavior playBehavior) {
        return toBuilder().then(animationName, playBehavior).build();
    }

    /**
     * Returns a new sequence with {@code animationName} appended, played with the given behavior and further stage
     * properties. The customizer runs after the behavior is applied, so it may override it.
     */
    public AzSequence then(
        String animationName,
        AzPlayBehavior playBehavior,
        UnaryOperator<AzAnimationStageProperties> customizer
    ) {
        return toBuilder().then(animationName, playBehavior, customizer).build();
    }

    /**
     * Returns a new sequence with every stage and event of {@code other} appended. The appended events are shifted by
     * {@code offsetTicks}, which you would normally set to the duration of this sequence.
     */
    public AzSequence then(AzSequence other, int offsetTicks) {
        return toBuilder().append(other, offsetTicks).build();
    }

    /**
     * Returns a new sequence with {@code animationName} appended as a play-once stage.
     */
    public AzSequence play(String animationName) {
        return toBuilder().play(animationName).build();
    }

    /**
     * Returns a new sequence with {@code animationName} appended as a looping (final) stage.
     */
    public AzSequence loop(String animationName) {
        return toBuilder().loop(animationName).build();
    }

    /**
     * Returns a new sequence with {@code animationName} appended as a stage that holds on its last frame (final).
     */
    public AzSequence hold(String animationName) {
        return toBuilder().hold(animationName).build();
    }

    /**
     * Returns a new sequence with {@code animationName} appended, played however its animation file says (the
     * {@code loop}, {@code repeat_times} and {@code freeze_at} set in Blockbench). Only add further stages after it if
     * the file's behavior finishes (play once, repeat x times).
     */
    public AzSequence authored(String animationName) {
        return toBuilder().authored(animationName).build();
    }

    /**
     * Returns a new sequence with {@code animationName} appended as a stage that plays forward and backward
     * indefinitely (final).
     */
    public AzSequence pingPong(String animationName) {
        return toBuilder().pingPong(animationName).build();
    }

    /**
     * Returns a new sequence with {@code animationName} appended as a play-once stage that runs from its last frame to
     * its first.
     */
    public AzSequence playReversed(String animationName) {
        return toBuilder().playReversed(animationName).build();
    }

    /**
     * Returns a new sequence with {@code animationName} appended as a stage that freezes at {@code freezeTick} (final).
     */
    public AzSequence freeze(String animationName, double freezeTick) {
        return toBuilder().freeze(animationName, freezeTick).build();
    }

    /**
     * Returns a new sequence with a named event scheduled {@code tick} game ticks after the sequence starts.
     */
    public AzSequence event(String name, int tick) {
        return toBuilder().event(name, tick).build();
    }

    /**
     * Returns a new sequence with a named event fired when the last stage's animation reaches {@code animationTick}.
     * See {@link Builder#eventAt(String, double)}.
     */
    public AzSequence eventAt(String name, double animationTick) {
        return toBuilder().eventAt(name, animationTick).build();
    }

    /**
     * Returns a new sequence with a named event fired when the last stage starts playing. See
     * {@link Builder#eventOnStart(String)}.
     */
    public AzSequence eventOnStart(String name) {
        return toBuilder().eventOnStart(name).build();
    }

    /**
     * Returns a new sequence with a named event fired when the last stage finishes. See
     * {@link Builder#eventOnEnd(String)}.
     */
    public AzSequence eventOnEnd(String name) {
        return toBuilder().eventOnEnd(name).build();
    }

    /**
     * Works out when each stage plays and each event fires with the given timing. {@link AzSequencePlayer} does this
     * itself; this is for inspecting or debugging a sequence, or for game logic that needs a stage's duration.
     */
    public AzSequenceTimeline timeline(AzSequenceTiming timing) {
        return AzSequenceTimeline.compute(this, timing);
    }

    /**
     * @return a builder pre-filled with this sequence's stages and events
     */
    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.stages.addAll(stages);
        builder.events.addAll(events);
        return builder;
    }

    /**
     * @return the low-level sequence that is sent over the network and run by the animation controller
     */
    public AzAnimationSequence toAnimationSequence() {
        return animationSequence;
    }

    /**
     * @return a command that plays this sequence on a single controller
     */
    public AzCommand toCommand(String controllerName) {
        return AzCommand.controllerBuilder().playSequence(controllerName, this).build();
    }

    /**
     * @return a command that plays this sequence on every controller of the animator
     */
    public AzCommand toRootCommand() {
        return AzCommand.rootBuilder().playSequence(this).build();
    }

    public List<AzAnimationStage> stages() {
        return stages;
    }

    /**
     * @return the events of this sequence, ordered by tick
     */
    public List<AzSequenceEvent> events() {
        return events;
    }

    public boolean isEmpty() {
        return stages.isEmpty();
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof AzSequence)) {
            return false;
        }

        AzSequence that = (AzSequence) object;

        return stages.equals(that.stages) && events.equals(that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stages, events);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("AzSequence[");

        for (int i = 0; i < stages.size(); i++) {
            AzAnimationStage stage = stages.get(i);

            if (i > 0) {
                builder.append(" -> ");
            }

            builder.append(stage.name()).append('(').append(stage.properties().playBehavior().name()).append(')');
        }

        if (!events.isEmpty()) {
            builder.append(", events=").append(events);
        }

        return builder.append(']').toString();
    }

    /**
     * Mutable builder for {@link AzSequence}. Not thread-safe; the built sequence is.
     */
    public static final class Builder {

        private final List<AzAnimationStage> stages = new ArrayList<>();

        private final List<AzSequenceEvent> events = new ArrayList<>();

        private Builder() {}

        /**
         * Appends {@code animationName}, played with the given behavior.
         */
        public Builder then(String animationName, AzPlayBehavior playBehavior) {
            return then(animationName, playBehavior, UnaryOperator.identity());
        }

        /**
         * Appends {@code animationName}, played with the given behavior and further stage properties. The customizer
         * runs after the behavior is applied, so it may override it.
         */
        public Builder then(
            String animationName,
            AzPlayBehavior playBehavior,
            UnaryOperator<AzAnimationStageProperties> customizer
        ) {
            requireName(animationName, "Animation name");
            Objects.requireNonNull(playBehavior, "playBehavior");
            Objects.requireNonNull(customizer, "customizer");
            ensureNotAfterFinalStage(animationName);

            // Start from all-unset properties so every value the stage doesn't set is inherited from the controller.
            AzAnimationStageProperties properties = customizer.apply(freshProperties().withPlayBehavior(playBehavior));

            if (properties == null) {
                throw new IllegalArgumentException("Stage customizer for '" + animationName + "' returned null");
            }

            stages.add(new AzAnimationStage(animationName, properties));
            return this;
        }

        /**
         * Appends every stage and event of {@code other}. Its events are shifted by {@code offsetTicks}.
         */
        public Builder append(AzSequence other, int offsetTicks) {
            Objects.requireNonNull(other, "other");

            if (offsetTicks < 0) {
                throw new IllegalArgumentException("offsetTicks must be >= 0, was " + offsetTicks);
            }

            if (!other.stages.isEmpty()) {
                ensureNotAfterFinalStage(other.stages.get(0).name());
            }

            int stageOffset = stages.size();

            stages.addAll(other.stages);
            // Fixed-tick events move by offsetTicks; anchored events stay on their stage, wherever it ends up.
            other.events.forEach(event -> events.add(event.shifted(stageOffset, offsetTicks)));
            return this;
        }

        /**
         * Appends a stage that plays once and then continues to the next stage (or stops if it is the last one).
         */
        public Builder play(String animationName) {
            return then(animationName, AzPlayBehaviors.PLAY_ONCE);
        }

        /**
         * Appends a stage that loops until something else is played. Nothing can follow it.
         */
        public Builder loop(String animationName) {
            return then(animationName, AzPlayBehaviors.LOOP);
        }

        /**
         * Appends a stage that plays once and then holds its final frame. Nothing can follow it.
         */
        public Builder hold(String animationName) {
            return then(animationName, AzPlayBehaviors.HOLD_ON_LAST_FRAME);
        }

        /**
         * Appends a stage played however its animation file says. See {@link AzSequence#authored(String)}.
         */
        public Builder authored(String animationName) {
            return then(animationName, AzPlayBehaviors.AS_AUTHORED);
        }

        /**
         * Appends a stage that plays forward, then backward, indefinitely. Nothing can follow it.
         */
        public Builder pingPong(String animationName) {
            return then(animationName, AzPlayBehaviors.PING_PONG);
        }

        /**
         * Appends a stage that plays once from its last frame to its first, then continues to the next stage. For other
         * behaviors, use {@code then(name, behavior, p -> p.withShouldReverse(true))}.
         */
        public Builder playReversed(String animationName) {
            return then(animationName, AzPlayBehaviors.PLAY_ONCE, properties -> properties.withShouldReverse(true));
        }

        /**
         * Appends a stage that plays until {@code freezeTick} (animation ticks) and freezes there. The freeze point
         * belongs to this stage only. Nothing can follow it.
         */
        public Builder freeze(String animationName, double freezeTick) {
            if (!(freezeTick > 0)) {
                throw new IllegalArgumentException(
                    "Freeze tick of '" + animationName + "' must be > 0, was " + freezeTick
                );
            }

            return then(
                animationName,
                AzPlayBehaviors.FREEZE_ON_FRAME,
                properties -> properties.withFreezeTickOffset(freezeTick)
            );
        }

        /**
         * Fires a named event when the most recently added stage's animation reaches {@code animationTick} on its own
         * timeline, the tick you see in Blockbench (keyframe events use the same time base). For a reversed stage that
         * is still the keyframe's tick, reached counting down. The player accounts for the transition into the stage,
         * its speed and start offset, and the length of every stage before it. Repeating, looping and ping-pong stages
         * fire it on their first pass only.
         *
         * @param name          an identifier your {@link AzSequenceEventListener} can match on
         * @param animationTick the tick on the stage animation's timeline, {@code >= 0}
         * @throws IllegalStateException if no stage has been added yet
         */
        public Builder eventAt(String name, double animationTick) {
            requireName(name, "Event name");

            if (!(animationTick >= 0)) {
                throw new IllegalArgumentException("Event '" + name + "' has an invalid tick: " + animationTick);
            }

            events.add(
                new AzSequenceEvent(name, 0, lastStageIndex(name), animationTick, AzSequenceEvent.Anchor.KEYFRAME)
            );
            return this;
        }

        /**
         * Fires a named event when the most recently added stage starts playing, once the transition into it is done.
         *
         * @throws IllegalStateException if no stage has been added yet
         */
        public Builder eventOnStart(String name) {
            requireName(name, "Event name");
            events.add(new AzSequenceEvent(name, 0, lastStageIndex(name), 0, AzSequenceEvent.Anchor.STAGE_START));
            return this;
        }

        /**
         * Fires a named event when the most recently added stage finishes: after its last repeat, at its freeze point,
         * or at the end of the first pass for looping, ping-pong and holding stages.
         *
         * @throws IllegalStateException if no stage has been added yet
         */
        public Builder eventOnEnd(String name) {
            requireName(name, "Event name");
            events.add(new AzSequenceEvent(name, 0, lastStageIndex(name), 0, AzSequenceEvent.Anchor.STAGE_END));
            return this;
        }

        /**
         * Schedules a named event {@code tick} game ticks after the sequence starts. The position of this call in the
         * chain does not affect timing, and neither do playback speed or transitions; use
         * {@link #eventAt(String, double)} for an event that should follow the animation.
         *
         * @param name an identifier your {@link AzSequenceEventListener} can match on
         * @param tick ticks after the start of the sequence, {@code >= 0}
         */
        public Builder event(String name, int tick) {
            requireName(name, "Event name");

            if (tick < 0) {
                throw new IllegalArgumentException("Event '" + name + "' has a negative tick: " + tick);
            }

            events.add(new AzSequenceEvent(name, tick));
            return this;
        }

        public AzSequence build() {
            if (stages.isEmpty() && events.isEmpty()) {
                return EMPTY;
            }

            return new AzSequence(stages, events);
        }

        private int lastStageIndex(String eventName) {
            if (stages.isEmpty()) {
                throw new IllegalStateException(
                    "Event '" + eventName + "' is anchored to a stage, so add a stage before it"
                );
            }

            return stages.size() - 1;
        }

        private void ensureNotAfterFinalStage(String nextAnimationName) {
            if (stages.isEmpty()) {
                return;
            }

            AzAnimationStage last = stages.get(stages.size() - 1);
            AzPlayBehavior lastBehavior = last.properties().playBehavior();

            if (NON_FINISHING_BEHAVIORS.contains(lastBehavior)) {
                throw new IllegalStateException(
                    "Cannot add '" + nextAnimationName + "' after '" + last.name() + "': its play behavior '"
                        + lastBehavior.name() + "' never finishes, so the stage would never play."
                );
            }
        }

        private static AzAnimationStageProperties freshProperties() {
            return new AzAnimationStageProperties(null, null, null, null, null, null, null, null);
        }

        private static void requireName(String value, String what) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(what + " must not be null or blank");
            }
        }
    }
}
