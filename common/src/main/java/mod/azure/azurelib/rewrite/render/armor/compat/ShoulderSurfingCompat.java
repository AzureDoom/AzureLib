package mod.azure.azurelib.rewrite.render.armor.compat;

import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;

import java.util.function.Supplier;

import mod.azure.azurelib.common.platform.Services;

/**
 * A utility class designed to handle interactions with the "Shoulder Surfing" mod. This class provides methods for:
 * <ul>
 * <li>Detecting whether the "Shoulder Surfing" mod is loaded,</li>
 * <li>Initializing a compatibility layer that integrates with the mod's features and</li>
 * <li>Retrieving alpha transparency values for rendering purposes.</li>
 * </ul>
 * <p>
 * When the "Shoulder Surfing" mod is detected, this class ensures smooth integration by setting a custom supplier for
 * alpha transparency values, which are retrieved from the mod's implementation. If the mod is not available, the
 * default behavior is provided to ensure robustness.
 * </p>
 * <p>
 * This class is intended to abstract away direct mod-related dependencies, allowing for safer and modular interaction
 * with the "Shoulder Surfing" mod.
 * </p>
 */
public class ShoulderSurfingCompat {

    private static Supplier<Float> alphaSupplier = () -> 1.0F;

    private static boolean isLoaded = false;

    /**
     * Initializes the compatibility layer for the "Shoulder Surfing" mod. If the "Shoulder Surfing" mod is detected as
     * loaded, this method sets the internal state to indicate its presence and assigns a supplier function to retrieve
     * the camera entity's alpha value from the mod's implementation.
     */
    public static void init() {
        if (Services.PLATFORM.isModLoaded("shouldersurfing")) {
            isLoaded = true;
            alphaSupplier = () -> ShoulderSurfing.getInstance().getCameraEntityRenderer().getCameraEntityAlpha();
        }
    }

    /**
     * Determines if the compatibility layer for the "Shoulder Surfing" mod is initialized and the mod is loaded.
     *
     * @return {@code true} if the "Shoulder Surfing" mod is detected as loaded, and the compatibility layer is
     *         initialized; {@code false} otherwise.
     */
    public static boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Retrieves the alpha value for rendering, which is based on an externally supplied float value. This value
     * determines the transparency level to be applied during rendering, particularly when integrating with the
     * "Shoulder Surfing" mod.
     *
     * @return A float representing the alpha value for transparency, where 1.0 indicates full opacity and values closer
     *         to 0
     */
    public static float getAlpha() {
        return alphaSupplier.get();
    }

    private ShoulderSurfingCompat() { /* NO-OP */}
}
