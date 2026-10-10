package mod.azure.azurelib.animation.dispatch.command.sequence;

/**
 * A named point in time within an {@link AzSequence}.
 *
 * @param name identifier matched by an {@link AzSequenceEventListener}, e.g. {@code "damage"}
 * @param tick game ticks after the start of the sequence at which the event fires
 */
public final class AzSequenceEvent {

    private final String name;

    private final int tick;

    public AzSequenceEvent(String name, int tick) {
        this.name = name;
        this.tick = tick;
    }

    public String name() {
        return this.name;
    }

    public int tick() {
        return this.tick;
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
}
