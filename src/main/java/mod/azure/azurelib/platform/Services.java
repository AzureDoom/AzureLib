package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.AzureLibInitializer;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.platform.services.IPlatformHelper;

/**
 * Platform service holder. Ornithe (with OSL) is the only platform for this build, so the services are instantiated
 * directly.
 */
public final class Services {

    public static final AzureLibInitializer INITIALIZER = new OrnitheAzureLibInitializer();

    public static final AzureLibNetwork NETWORK = new OrnitheAzureLibNetwork();

    public static final IPlatformHelper PLATFORM = new OrnithePlatformHelper();

    private Services() {}
}
