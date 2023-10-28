package mod.azure.azurelib;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AzureLib.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientListener {

	@SubscribeEvent
	public static void registerKeys(final RegisterKeyMappingsEvent event) {
		Keybindings.RELOAD = new KeyMapping("key.azurelib.reload", GLFW.GLFW_KEY_R, "category.azurelib.binds");
		event.register(Keybindings.RELOAD);
	}
}
