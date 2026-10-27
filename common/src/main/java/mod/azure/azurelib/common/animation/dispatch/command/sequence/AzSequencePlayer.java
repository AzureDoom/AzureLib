package mod.azure.azurelib.common.animation.dispatch.command.sequence;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;

/**
 * Plays {@link AzSequence}s on one animation controller and delivers their {@link AzSequenceEvent events} on the side
 * that ticks it. Animations only exist on the client, so the server cannot see when a stage starts; the player instead
 * counts game ticks from the moment {@link #play(AzSequence)} is called, which is exactly how event ticks are defined.
 *
 * <pre>{@code
 *
 * // In your entity
 * private final AzSequencePlayer attackPlayer = AzSequencePlayer.forEntity(this, "base", (sequence, event) -> {
 *     if (event.is("damage")) {
 *         dealSlamDamage();
 *     }
 * });
 *
 * public void startSlam() { // e.g. from Goal#start, server side
 *     attackPlayer.play(SLAM);
 * }
 *
 * @Override
 * public void tick() {
 *     super.tick();
 *     if (!level().isClientSide()) {
 *         attackPlayer.tick();
 *     }
 * }
 * }</pre>
 *
 * <h2>Timing</h2> Events added with {@code event(name, tick)} fire that many game ticks after {@link #play}, whatever
 * the animation is doing. Events anchored to a stage ({@code eventAt}, {@code eventOnStart}, {@code eventOnEnd}) are
 * placed with this player's {@link AzSequenceTiming}: the controller's speed and transition length, each stage's own
 * overrides, and each animation's length. Give the player the same settings the controller was built with, and the
 * lengths of the animations those events depend on:
 *
 * <pre>{@code
 *
 * private final AzSequencePlayer attackPlayer = AzSequencePlayer.forEntity(this, "base", this::onAttackEvent)
 *     .withTiming(
 *         AzSequenceTiming.builder()
 *             .transitionLength(5)
 *             .length("attack_windup", 15)
 *             .length("attack", 20)
 *             .build()
 *     );
 * }</pre>
 *
 * An event whose time can't be worked out (a needed length is missing, or the animation never reaches it) is skipped
 * with a warning instead of firing at the wrong moment. A player ticked on the client can use
 * {@code AzAnimationController#sequenceTiming()}, which reads every length from the animation file.
 * <p>
 * If something else takes over the controller (a hurt or death animation, say), call {@link #cancel()} so that pending
 * events of the interrupted sequence do not fire.
 */
public final class AzSequencePlayer {

    private final String controllerName;

    private final Consumer<AzCommand> dispatcher;

    private final AzSequenceEventListener listener;

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private AzSequenceTiming timing = AzSequenceTiming.DEFAULT;

    private @Nullable AzSequence current;

    private List<AzSequenceTimeline.ScheduledEvent> schedule = List.of();

    private @Nullable AzSequence cachedSequence;

    private @Nullable AzSequenceTiming cachedTiming;

    private @Nullable AzSequenceTimeline cachedTimeline;

    private int elapsedTicks;

    private int nextEventIndex;

    /**
     * Bumped whenever the current sequence is replaced or canceled, so that an event listener which calls {@link #play}
     * or {@link #cancel} from inside {@code onEvent} does not cause stale events to fire.
     */
    private int generation;

    private AzSequencePlayer(String controllerName, Consumer<AzCommand> dispatcher, AzSequenceEventListener listener) {
        this.controllerName = Objects.requireNonNull(controllerName, "controllerName");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    /**
     * Creates a player that drives {@code controllerName} on an entity.
     */
    public static AzSequencePlayer forEntity(
        Entity entity,
        String controllerName,
        AzSequenceEventListener listener
    ) {
        Objects.requireNonNull(entity, "entity");
        return new AzSequencePlayer(controllerName, command -> command.sendForEntity(entity), listener);
    }

    /**
     * Creates a player that drives {@code controllerName} on a block entity.
     */
    public static AzSequencePlayer forBlockEntity(
        BlockEntity blockEntity,
        String controllerName,
        AzSequenceEventListener listener
    ) {
        Objects.requireNonNull(blockEntity, "blockEntity");
        return new AzSequencePlayer(controllerName, command -> command.sendForBlockEntity(blockEntity), listener);
    }

    /**
     * Creates a player with a custom dispatcher, e.g. {@code command -> command.sendForItem(holder, stack)}.
     */
    public static AzSequencePlayer of(
        String controllerName,
        Consumer<AzCommand> dispatcher,
        AzSequenceEventListener listener
    ) {
        return new AzSequencePlayer(controllerName, dispatcher, listener);
    }

    /**
     * Sets the timing used to place stage-anchored events, and returns this player for chaining. Takes effect from the
     * next {@link #play}.
     */
    public AzSequencePlayer withTiming(AzSequenceTiming timing) {
        this.timing = Objects.requireNonNull(timing, "timing");
        return this;
    }

    /**
     * @return the timing used to place stage-anchored events
     */
    public AzSequenceTiming timing() {
        return timing;
    }

    /**
     * @return the timeline of the current sequence (when its stages play and its events fire), or {@code null} if
     *         nothing is playing
     */
    public @Nullable AzSequenceTimeline timeline() {
        return current == null ? null : timelineFor(current);
    }

    /**
     * Starts {@code sequence} from the beginning, replacing whatever this player was playing. The controller is
     * canceled first, so replaying the sequence that is already running restarts it on the client too and keeps events
     * in step with the animation. Events at tick 0 fire before this method returns.
     */
    public void play(AzSequence sequence) {
        Objects.requireNonNull(sequence, "sequence");

        dispatcher.accept(
            AzCommand.controllerBuilder()
                .cancel(controllerName)
                .playSequence(controllerName, sequence)
                .build()
        );

        this.current = sequence;
        this.schedule = sequence.hasEvents() ? timelineFor(sequence).events() : List.of();
        this.elapsedTicks = 0;
        this.nextEventIndex = 0;
        this.generation++;

        fireDueEvents();
    }

    /**
     * Stops the controller and drops any events that have not fired yet.
     */
    public void cancel() {
        if (current == null) {
            return;
        }

        dispatcher.accept(AzCommand.controllerBuilder().cancel(controllerName).build());
        clear();
    }

    /**
     * Drops pending events without touching the animation. Use this when another command has already replaced the
     * animation on the controller.
     */
    public void clear() {
        this.current = null;
        this.schedule = List.of();
        this.elapsedTicks = 0;
        this.nextEventIndex = 0;
        this.generation++;
    }

    /**
     * Advances the player by one game tick and fires any events that are now due. Call once per tick on the side that
     * should receive events (normally the server).
     */
    public void tick() {
        if (current == null) {
            return;
        }

        elapsedTicks++;
        fireDueEvents();
    }

    private void fireDueEvents() {
        var sequence = current;

        if (sequence == null) {
            return;
        }

        var events = schedule;
        var startGeneration = generation;

        while (generation == startGeneration && nextEventIndex < events.size()) {
            var scheduled = events.get(nextEventIndex);

            // Fractional due ticks fire on the first whole tick at or after them, never early.
            if (scheduled.tick() > elapsedTicks + 1.0E-6) {
                break;
            }

            nextEventIndex++;
            listener.onEvent(sequence, scheduled.event());
        }
    }

    private AzSequenceTimeline timelineFor(AzSequence sequence) {
        if (cachedTimeline != null && cachedSequence == sequence && cachedTiming == timing) {
            return cachedTimeline;
        }

        var timeline = AzSequenceTimeline.compute(sequence, timing);

        if (!timeline.skipped().isEmpty() && WARNED.add(sequence.toString())) {
            for (var skipped : timeline.skipped()) {
                AzureLib.LOGGER.warn(
                    "Sequence event '{}' in {} on controller '{}' will not fire: {}",
                    skipped.event().name(),
                    sequence,
                    controllerName,
                    skipped.reason()
                );
            }
        }

        this.cachedSequence = sequence;
        this.cachedTiming = timing;
        this.cachedTimeline = timeline;

        return timeline;
    }

    /**
     * @return the last sequence started by {@link #play}, or {@code null} after {@link #cancel()} / {@link #clear()}
     */
    public @Nullable AzSequence current() {
        return current;
    }

    /**
     * @return whether {@code sequence} is the one this player last started and has not been canceled
     */
    public boolean isCurrent(AzSequence sequence) {
        return current != null && current.equals(sequence);
    }

    /**
     * @return whether the current sequence still has events that have not fired
     */
    public boolean hasPendingEvents() {
        return current != null && nextEventIndex < schedule.size();
    }

    /**
     * @return game ticks since the current sequence started
     */
    public int elapsedTicks() {
        return elapsedTicks;
    }

    public String controllerName() {
        return controllerName;
    }
}
