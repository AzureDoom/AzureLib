package mod.azure.azurelib.platform;

import net.minecraftforge.common.MinecraftForge;

import mod.azure.azurelib.hitbox.AzHitboxEvents;
import mod.azure.azurelib.network.AzServerboundPackets;
import mod.azure.azurelib.platform.services.AzureLibInitializer;

public class ForgeAzureLibInitializer implements AzureLibInitializer {

    @Override
    public void initialize() {
        Services.NETWORK.registerClientReceiverPackets();
        AzServerboundPackets.register(Services.NETWORK);
        MinecraftForge.EVENT_BUS.register(AzHitboxEvents.class);
    }
}
