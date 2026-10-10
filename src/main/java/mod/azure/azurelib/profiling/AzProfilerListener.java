package mod.azure.azurelib.profiling;

/**
 * Receives stage boundaries from {@link AzProfiler}. Calls arrive on whichever thread runs the stage, which for
 * rendering and animation is the render thread.
 * <p>
 * Implementations must tolerate unbalanced calls: an {@code end} with no matching {@code begin} happens when a listener
 * is installed partway through a stage, and a missing {@code end} happens when a stage throws.
 */
public interface AzProfilerListener {

    void begin(AzProfileStage stage, Object subject);

    void end(AzProfileStage stage);
}
