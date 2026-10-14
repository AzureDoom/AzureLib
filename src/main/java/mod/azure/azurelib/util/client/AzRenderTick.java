package mod.azure.azurelib.util.client;

/**
 * Tracks the current frame's partial tick. Beta keeps it in a private timer and has no render-tick event.
 */
public final class AzRenderTick {

    private static float partialTicks;

    private AzRenderTick() {}

    public static float partialTicks() {
        return partialTicks;
    }

    public static void set(float partialTick) {
        partialTicks = partialTick;
    }
}
