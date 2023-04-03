package mod.azure.azurelib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * Helper class for segregating client-side code
 */
public final class ClientUtils {
	/**
	 * Get the player on the client
	 */
	public static PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	/**
	 * Gets the current level on the client
	 */
	public static World getLevel() {
		return Minecraft.getInstance().level;
	}
}
