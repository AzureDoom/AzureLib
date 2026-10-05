package mod.azure.azurelib.animation.dispatch.command.sequence;

import org.jetbrains.annotations.NotNull;

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
 * <pre>{@code
 *
 * AzSequence attack = AzSequence.create()
 *     .then("attack_windup", AzPlayBehaviors.PLAY_ONCE)
 *     .then("attack", AzPlayBehaviors.PLAY_ONCE)
 *     .then("idle", AzPlayBehaviors.LOOP);
 * }</pre>
 *
 * The builder style avoids intermediate copies and is the natural choice for {@code static final} constants:
 *
 * <pre>{@code
 *
 * static final AzSequence SLAM = AzSequence.builder()
 *     .play("windup")
 *     .event("damage", 12)
 *     .play("strike")
 *     .hold("recovery")
 *     .build();
 * }</pre>
 *
 * <h2>Stages</h2> {@link #play(String)} plays once and continues, {@link #loop(String)} loops forever,
 * {@link #hold(String)} freezes on the final frame, and {@link #then(String, AzPlayBehavior)} accepts any behavior.
 * Looping, holding and freezing stages never finish, so adding a stage after one of them is rejected with an
 * {@link IllegalStateException} instead of silently producing an unreachable stage.
 * <h2>Events</h2> {@link #event(String, int)} schedules a named event {@code tick} game ticks after the sequence
 * starts. Timing is measured from the start of the sequence, not from the stage the call follows; its position in the
 * chain is only for readability. Events are not sent to the client: they are delivered by an {@link AzSequencePlayer},
 * which is what lets them drive server-side logic such as dealing damage.
 * <h2>Playing</h2> Use {@link #toCommand(String)} / {@link #toRootCommand()} to get a normal {@link AzCommand}, or an
 * {@link AzSequencePlayer} when the sequence has events.
 */
public final class AzSequence {

    private static final AzSequence EMPTY = new AzSequence(List.of(), List.of());

    /**
     * Built-in behaviors that never finish on their own; anything queued after them could never play.
     */
    private static final Set<AzPlayBehavior> NON_FINISHING_BEHAVIORS = Set.of(
        AzPlayBehaviors.LOOP,
        AzPlayBehaviors.PING_PONG,
        AzPlayBehaviors.HOLD_ON_LAST_FRAME,
        AzPlayBehaviors.FREEZE_ON_FRAME
    );

    private final List<AzAnimationStage> stages;

    private final List<AzSequenceEvent> events;

    private final AzAnimationSequence animationSequence;

    private AzSequence(List<AzAnimationStage> stages, List<AzSequenceEvent> events) {
        this.stages = List.copyOf(stages);

        var sortedEvents = new ArrayList<>(events);
        // Stable sort: events on the same tick fire in declaration order.
        sortedEvents.sort(Comparator.comparingInt(AzSequenceEvent::tick));
        this.events = List.copyOf(sortedEvents);

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
     * Returns a new sequence with a named event scheduled {@code tick} ticks after the sequence starts.
     */
    public AzSequence event(String name, int tick) {
        return toBuilder().event(name, tick).build();
    }

    /**
     * @return a builder pre-filled with this sequence's stages and events
     */
    public Builder toBuilder() {
        var builder = new Builder();
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

        if (!(object instanceof AzSequence that)) {
            return false;
        }

        return stages.equals(that.stages) && events.equals(that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stages, events);
    }

    @Override
    public String toString() {
        var builder = new StringBuilder("AzSequence[");

        for (int i = 0; i < stages.size(); i++) {
            var stage = stages.get(i);

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
            @NotNull AzPlayBehavior playBehavior,
            @NotNull UnaryOperator<AzAnimationStageProperties> customizer
        ) {
            requireName(animationName, "Animation name");
            Objects.requireNonNull(playBehavior, "playBehavior");
            Objects.requireNonNull(customizer, "customizer");
            ensureNotAfterFinalStage(animationName);

            // Always start from a fresh instance: the with* methods on AzAnimationStageProperties mutate the
            // receiver, so building on the shared EMPTY/DEFAULT constants would leak state between sequences.
            var properties = customizer.apply(freshProperties().withPlayBehavior(playBehavior));

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
                ensureNotAfterFinalStage(other.stages.getFirst().name());
            }

            stages.addAll(other.stages);
            other.events.forEach(event -> events.add(new AzSequenceEvent(event.name(), event.tick() + offsetTicks)));
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
         * Schedules a named event {@code tick} game ticks after the sequence starts. The position of this call in the
         * chain does not affect timing.
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

        private void ensureNotAfterFinalStage(String nextAnimationName) {
            if (stages.isEmpty()) {
                return;
            }

            var last = stages.getLast();
            var lastBehavior = last.properties().playBehavior();

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
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(what + " must not be null or blank");
            }
        }
    }
}
