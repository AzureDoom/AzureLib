package mod.azure.azurelib.common.platform;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.common.platform.services.IPlatformHelper;
import mod.azure.azurelib.common.platform.services.AzureLibInitializer;
import mod.azure.azurelib.common.platform.services.GeoRenderPhaseEventFactory;

import java.util.ServiceLoader;

public class Services {

    public static final GeoRenderPhaseEventFactory GEO_RENDER_PHASE_EVENT_FACTORY = load(GeoRenderPhaseEventFactory.class);
    public static final AzureLibInitializer INITIALIZER = load(AzureLibInitializer.class);
    public static final AzureLibNetwork NETWORK = load(AzureLibNetwork.class);
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        AzureLib.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}