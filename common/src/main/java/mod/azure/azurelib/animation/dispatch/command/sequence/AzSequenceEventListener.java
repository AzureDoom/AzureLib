package mod.azure.azurelib.animation.dispatch.command.sequence;

/**
 * Receives the events of sequences played through an {@link AzSequencePlayer}.
 */
@FunctionalInterface
public interface AzSequenceEventListener {

    AzSequenceEventListener NONE = (sequence, event) -> {};

    /**
     * Called once per event, on the side that ticks the {@link AzSequencePlayer}.
     *
     * @param sequence the sequence the event belongs to, handy for telling several sequences apart
     * @param event    the event that fired
     */
    void onEvent(AzSequence sequence, AzSequenceEvent event);
}
