package mod.azure.azurelib.animation.dispatch.command.sequence;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * A named point in time within an {@link AzSequence}. An event is either measured from the start of the sequence in
 * game ticks ({@link Anchor#SEQUENCE}, from {@link AzSequence.Builder#event(String, int)}), or anchored to one of the
 * sequence's stages ({@link Anchor#KEYFRAME}, {@link Anchor#STAGE_START}, {@link Anchor#STAGE_END}). Anchored events
 * are placed by an {@link AzSequencePlayer} using an {@link AzSequenceTiming}, so they follow playback speed, start
 * offsets, transitions, repeats and stage lengths instead of a fixed game-tick count.
 *
 * @param name      identifier matched by an {@link AzSequenceEventListener}, e.g. {@code "damage"}
 * @param tick      for {@link Anchor#SEQUENCE}: game ticks after the start of the sequence; otherwise unused (0)
 * @param stage     for anchored events: index of the stage in {@link AzSequence#stages()}; otherwise -1
 * @param stageTick for {@link Anchor#KEYFRAME}: the tick on the stage animation's own timeline, as shown in Blockbench
 *                  (the same time base keyframe events use); otherwise unused (0)
 * @param anchor    what the event's time is measured from
 */
public record AzSequenceEvent(
    String name,
    int tick,
    int stage,
    double stageTick,
    Anchor anchor
) {

    /**
     * What an event's time is measured from.
     */
    public enum Anchor {

        /** {@code tick} game ticks after the sequence starts. Not affected by speed or transitions. */
        SEQUENCE,
        /** When the stage's animation reaches {@code stageTick} on its own timeline (keyframe time). */
        KEYFRAME,
        /** When the stage's animation starts playing, after the transition into it. */
        STAGE_START,
        /**
         * When the stage's animation finishes: after its last repeat for repeating stages, at the freeze point for
         * freezing stages, and at the end of the first pass for looping, ping-pong and holding stages.
         */
        STAGE_END
    }

    public AzSequenceEvent {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(anchor, "anchor");
    }

    /**
     * An event {@code tick} game ticks after the sequence starts.
     */
    public AzSequenceEvent(String name, int tick) {
        this(name, tick, -1, 0, Anchor.SEQUENCE);
    }

    /**
     * @return whether this event has the given name
     */
    public boolean is(String name) {
        return this.name.equals(name);
    }

    /**
     * @return whether this event is tied to a stage rather than to a fixed game-tick count
     */
    public boolean isAnchored() {
        return anchor != Anchor.SEQUENCE;
    }

    /**
     * @return this event moved to another stage index (anchored) or shifted by {@code tickOffset} game ticks (not
     *         anchored); used when sequences are appended to each other
     */
    AzSequenceEvent shifted(int stageOffset, int tickOffset) {
        return isAnchored()
            ? new AzSequenceEvent(name, tick, stage + stageOffset, stageTick, anchor)
            : new AzSequenceEvent(name, tick + tickOffset, stage, stageTick, anchor);
    }

    @Override
    public @NonNull String toString() {
        return switch (anchor) {
            case SEQUENCE -> name + "@" + tick;
            case KEYFRAME -> name + "@stage" + stage + ":" + stageTick;
            case STAGE_START -> name + "@stage" + stage + ":start";
            case STAGE_END -> name + "@stage" + stage + ":end";
        };
    }
}
