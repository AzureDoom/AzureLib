package mod.azure.azurelib.neoforge;

import mod.azure.azurelib.common.internal.common.util.IncompatibleModsCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientNonModListener {
    @SubscribeEvent
    public static void onClientStart(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            IncompatibleModsCheck.warnings(Minecraft.getInstance());
        }
    }
}
