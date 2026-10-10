package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.AzureLibInitializer;

public class ForgeAzureLibInitializer implements AzureLibInitializer {

    @Override
    public void initialize() {
        Services.NETWORK.registerClientReceiverPackets();
    }
}
