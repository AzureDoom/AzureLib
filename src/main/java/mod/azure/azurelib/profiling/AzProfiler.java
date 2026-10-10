package mod.azure.azurelib.profiling;

import javax.annotation.Nullable;

/**
 * Stage hooks for external profilers. AzureLib calls {@link #begin} and {@link #end} around each
 * {@link AzProfileStage}; with no listener installed each call is a field read and a null check, which the JIT inlines
 * to effectively nothing.
 * <p>
 * Only one listener is active at a time. This is a development aid, not an event bus.
 */
public final class AzProfiler {

    private static volatile AzProfilerListener listener;

    private AzProfiler() {}

    /**
     * Installs a listener, or removes the current one when {@code newListener} is {@code null}.
     *
     * @return the previously installed listener, if any
     */
    public static @Nullable AzProfilerListener setListener(@Nullable AzProfilerListener newListener) {
        AzProfilerListener previous = listener;
        listener = newListener;
        return previous;
    }

    public static @Nullable AzProfilerListener getListener() {
        return listener;
    }

    public static void begin(AzProfileStage stage, Object subject) {
        AzProfilerListener current = listener;

        if (current != null) {
            current.begin(stage, subject);
        }
    }

    public static void end(AzProfileStage stage) {
        AzProfilerListener current = listener;

        if (current != null) {
            current.end(stage);
        }
    }
}
