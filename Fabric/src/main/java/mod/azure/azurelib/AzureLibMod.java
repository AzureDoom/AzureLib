package mod.azure.azurelib;

import net.fabricmc.api.ModInitializer;

public final class AzureLibMod implements ModInitializer {

	@Override
	public void onInitialize() {
		AzureLib.initialize();
	}
}
