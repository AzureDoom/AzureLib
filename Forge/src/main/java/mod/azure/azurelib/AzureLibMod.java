/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
@Mod(AzureLib.MOD_ID)
public final class AzureLibMod {

	public static AzureLibMod instance;

	public AzureLibMod() {
		instance = this;
		AzureLib.initialize();
	}
}
