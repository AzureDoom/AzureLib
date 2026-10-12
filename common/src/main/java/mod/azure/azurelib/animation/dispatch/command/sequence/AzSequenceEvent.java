package mod.azure.azurelib.animation.dispatch.command.sequence;

/**
 * A named point in time within an {@link AzSequence}.
 *
 * @param name identifier matched by an {@link AzSequenceEventListener}, e.g. {@code "damage"}
 * @param tick game ticks after the start of the sequence at which the event fires
 */
public record AzSequenceEvent(
    String name,
    int tick
) {

    /**
     * @return whether this event has the given name
     */
    public boolean is(String name) {
        return this.name.equals(name);
    }
}
