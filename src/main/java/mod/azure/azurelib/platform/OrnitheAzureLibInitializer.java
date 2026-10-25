package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.AzureLibInitializer;

public class OrnitheAzureLibInitializer implements AzureLibInitializer {

    @Override
    public void initialize() {
        OrnitheAzureLibNetwork.registerChannel();
        Services.NETWORK.registerClientReceiverPackets();

        if (
            net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER
        ) {
            OrnitheAzureLibNetwork.registerServerListener();
        }
    }
}
