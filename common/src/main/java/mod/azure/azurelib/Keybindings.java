package mod.azure.azurelib;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
	public static KeyMapping RELOAD;
	public static KeyMapping SCOPE;
	public static KeyMapping FIRE_WEAPON;

	private Keybindings() {
		throw new UnsupportedOperationException();
	}
}
