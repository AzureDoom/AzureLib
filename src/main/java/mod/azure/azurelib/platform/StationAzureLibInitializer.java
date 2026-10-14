package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.AzureLibInitializer;

public class StationAzureLibInitializer implements AzureLibInitializer {

    @Override
    public void initialize() {
        Services.NETWORK.registerClientReceiverPackets();
    }
}
