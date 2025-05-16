package mod.azure.azurelib;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Deprecated()
public class Keybindings {

    public static KeyMapping RELOAD = new KeyMapping(
        "key.azurelib.reload",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_INSERT,
        "category.azurelib.binds"
    );
}
