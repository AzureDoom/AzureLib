package mod.azure.azurelib.common.internal.common.util;

import mod.azure.azurelib.common.internal.client.config.screen.OptifineWarningScreen;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibMod;
import net.minecraft.client.Minecraft;

public class IncompatibleModsCheck {
    public static boolean optifinePresent = false;

    public static void run() {
        try {
            Class.forName("net.optifine.Config");
            optifinePresent = true;
        } catch (ClassNotFoundException e) {
            optifinePresent = false;
        }
    }

    public static void warnings(Minecraft mc) {
        if (IncompatibleModsCheck.optifinePresent) {
            if (AzureLibMod.config.disableOptifineWarning) {
                AzureLib.LOGGER.fatal("Optifine Has been detected, Disabled Warning Status: false");
                mc.setScreen(new OptifineWarningScreen());
            } else {
                AzureLib.LOGGER.fatal("Optifine Has been detected, Disabled Warning Status: true");
            }
        }
    }
}
