package mod.azure.azurelib.animation.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.platform.Services;

public final class AzPendingDispatches {

    private static final Map<Object, List<Consumer<AzAnimator<?, ?>>>> PENDING = new WeakHashMap<>();

    private AzPendingDispatches() {}

    public static void enqueue(Object animatable, Consumer<AzAnimator<?, ?>> dispatch) {
        PENDING.computeIfAbsent(animatable, k -> new ArrayList<>()).add(dispatch);
    }

    public static void flush(Object animatable, AzAnimator<?, ?> animator) {
        List<Consumer<AzAnimator<?, ?>>> queued = PENDING.remove(animatable);
        if (queued != null) {
            if (Services.PLATFORM.isDevelopmentEnvironment())
                AzureLib.LOGGER.info("Replaying {} queued command(s) for {}", queued.size(), animatable);
            queued.forEach(d -> d.accept(animator));
        }
    }
}
