package mod.azure.azurelib.util.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Tracks the current frame's partial tick. Minecraft 1.7.10 keeps it in a private timer, so AzureLib records it from
 * {@link TickEvent.RenderTickEvent} for render paths that don't receive it as a parameter (items, armor).
 */
@SideOnly(Side.CLIENT)
public final class AzRenderTick {

    private static float partialTicks;

    public static float partialTicks() {
        return partialTicks;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            partialTicks = event.renderTickTime;
        }
    }
}
