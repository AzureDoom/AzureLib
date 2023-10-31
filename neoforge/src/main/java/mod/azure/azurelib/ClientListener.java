package mod.azure.azurelib;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AzureLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientListener {

	@SubscribeEvent
	public static void registerKeys(final RegisterKeyMappingsEvent event) {
		Keybindings.RELOAD = new KeyMapping("key.azurelib.reload", GLFW.GLFW_KEY_R, "category.azurelib.binds");
		event.register(Keybindings.RELOAD);
	}
}
