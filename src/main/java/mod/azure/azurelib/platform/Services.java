package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.AzureLibInitializer;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.platform.services.IPlatformHelper;

/**
 * Platform service holder. Forge is the only loader on 1.7.10, so the services are instantiated directly rather than
 * through {@link java.util.ServiceLoader}.
 */
public final class Services {

    public static final AzureLibInitializer INITIALIZER = new ForgeAzureLibInitializer();

    public static final AzureLibNetwork NETWORK = new ForgeAzureLibNetwork();

    public static final IPlatformHelper PLATFORM = new ForgePlatformHelper();

    private Services() {}
}
