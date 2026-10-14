package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.AzureLibInitializer;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.platform.services.IPlatformHelper;

/**
 * Platform service holder. StationAPI is the only platform on Beta, so the services are instantiated directly.
 */
public final class Services {

    public static final AzureLibInitializer INITIALIZER = new StationAzureLibInitializer();

    public static final AzureLibNetwork NETWORK = new StationAzureLibNetwork();

    public static final IPlatformHelper PLATFORM = new FabricPlatformHelper();

    private Services() {}
}
