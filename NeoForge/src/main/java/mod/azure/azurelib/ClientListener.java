package mod.azure.azurelib;

import mod.azure.azurelib.AzureLibMod.AzureEntities;
import mod.azure.azurelib.entities.SpellBlockRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AzureLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientListener {

	@SubscribeEvent
	public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(AzureEntities.TIMER_SPELL_TURRET_ENTITY.get(), context -> new SpellBlockRenderer());
	}
}
