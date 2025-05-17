/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Helper class for segregating client-side code
 */
public final class ClientUtils {

    /**
     * Get the player on the client
     */
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    /**
     * Gets the current level on the client
     */
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }
}
