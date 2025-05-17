package mod.azure.azurelib.platform;

import java.util.ServiceLoader;

import mod.azure.azurelib.platform.services.AzureEvents;
import mod.azure.azurelib.platform.services.AzureLibInitializer;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.platform.services.IPlatformHelper;

public class Services {

    public static final AzureEvents GEO_RENDER_PHASE_EVENT_FACTORY = load(AzureEvents.class);

    public static final AzureLibInitializer INITIALIZER = load(AzureLibInitializer.class);

    public static final AzureLibNetwork NETWORK = load(AzureLibNetwork.class);

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
