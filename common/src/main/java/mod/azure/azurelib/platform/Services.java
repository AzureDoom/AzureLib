package mod.azure.azurelib.platform;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.platform.services.AccessWidener;
import mod.azure.azurelib.platform.services.AzureLibInitializer;
import mod.azure.azurelib.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {

    public static final AccessWidener ACCESS_WIDENER = load(AccessWidener.class);
    public static final AzureLibInitializer INITIALIZER = load(AzureLibInitializer.class);
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        AzureLib.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}