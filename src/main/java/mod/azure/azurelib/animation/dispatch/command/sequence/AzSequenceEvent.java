package mod.azure.azurelib.animation.dispatch.command.sequence;

public final class AzSequenceEvent {

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

    private final String name;

    private final int tick;

    private final int stage;

    private final double stageTick;

    private final Anchor anchor;

    /**
     * A named point in time within an {@link AzSequence}.
     *
     * @param name identifier matched by an {@link AzSequenceEventListener}, e.g. {@code "damage"}
     * @param tick game ticks after the start of the sequence at which the event fires
     */
    public AzSequenceEvent(String name, int tick, int stage, double stageTick, Anchor anchor) {
        this.name = name;
        this.tick = tick;
        this.stage = stage;
        this.stageTick = stageTick;
        this.anchor = anchor;
    }

    /**
     * An event {@code tick} game ticks after the sequence starts.
     */
    public AzSequenceEvent(String name, int tick) {
        this(name, tick, -1, 0, Anchor.SEQUENCE);
    }

    public String name() {
        return this.name;
    }

    public int tick() {
        return this.tick;
    }

    public int stage() {
        return this.stage;
    }

    public double stageTick() {
        return this.stageTick;
    }

    public Anchor anchor() {
        return this.anchor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzSequenceEvent))
            return false;
        AzSequenceEvent other = (AzSequenceEvent) o;
        return java.util.Objects.equals(this.name, other.name)
            && this.tick == other.tick;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.name);
        result = 31 * result + Integer.hashCode(this.tick);
        return result;
    }

    @Override
    public String toString() {
        return "AzSequenceEvent[name=" + this.name + ", tick=" + this.tick + "]";
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
}
