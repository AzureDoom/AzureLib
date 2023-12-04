package mod.azure.azurelib.common.internal.common.config;

import mod.azure.azurelib.common.api.common.config.Config;
import mod.azure.azurelib.common.internal.common.AzureLib;

@Config(id = AzureLib.MOD_ID)
public class AzureLibConfig {
    @Configurable
    @Configurable.Synchronized
    public boolean disableOptifineWarning = false;
    @Configurable
    @Configurable.Synchronized
    public boolean useVanillaUseKey = true;
}
