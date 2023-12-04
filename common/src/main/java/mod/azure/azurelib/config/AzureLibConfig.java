package mod.azure.azurelib.config;

import mod.azure.azurelib.AzureLib;

@Config(id = AzureLib.MOD_ID)
public class AzureLibConfig {
    @Configurable
    @Configurable.Synchronized
    public boolean disableOptifineWarning = false;
}
