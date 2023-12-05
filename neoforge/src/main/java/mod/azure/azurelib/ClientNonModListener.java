package mod.azure.azurelib;

import mod.azure.azurelib.util.IncompatibleModsCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientNonModListener {
    @SubscribeEvent
    public static void onClientStart(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            IncompatibleModsCheck.warnings(Minecraft.getInstance());
        }
    }
}
