package mod.azure.azurelib.hitbox.client;

import org.lwjgl.input.Keyboard;

/**
 * Client tick work for hitboxes, driven by {@code MinecraftMixin_AzHitboxClientTick} at the end of
 * {@code Minecraft#tick()} (Beta has no client tick event).
 */
public final class AzHitboxClientTicker {

    private static boolean debugEnabled;

    private static boolean debugKeyWasDown;

    private AzHitboxClientTicker() {
        throw new UnsupportedOperationException();
    }

    public static void tick() {
        pollDebugToggle();
        AzHitboxClientHooks.tickOffscreen();
    }

    /**
     * Whether hitbox outlines are drawn. Beta has no F3 + B entity box view, so AzureLib provides its own on the same
     * keys. Beta toggles the F3 overlay as soon as F3 goes down, so the overlay flips too; press F3 again to clear it.
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    private static void pollDebugToggle() {
        boolean down = Keyboard.isCreated() && Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(
            Keyboard.KEY_B
        );

        if (down && !debugKeyWasDown) {
            debugEnabled = !debugEnabled;
        }

        debugKeyWasDown = down;
    }
}
