package mod.azure.azurelib.render.armor.compat;

import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.function.Supplier;

import mod.azure.azurelib.platform.Services;

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

    private static boolean isLoaded = false;

    /**
     * Initializes the compatibility layer for the "Shoulder Surfing" mod. This method checks whether the "Shoulder
     * Surfing" mod is loaded using the platform-specific implementation of the {@code isModLoaded} method. If the mod
     * is detected, it sets the internal state to indicate that the compatibility layer is successfully loaded.
     */
    public static void init() {
        if (Services.PLATFORM.isModLoaded("shouldersurfing")) {
            isLoaded = true;
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
     * Retrieves the alpha transparency value for the provided entity. The alpha value determines the transparency level
     * of the rendered entity, where 1.0 represents fully opaque and values below 1.0 represent varying degrees of
     * transparency. This method integrates with the "Shoulder Surfing" mod to retrieve custom alpha values if
     * applicable.
     *
     * @param currentEntity the entity for which the alpha transparency value is being determined.
     * @return the alpha transparency value for the provided entity. Returns 1.0 if the camera entity is not available
     *         or if the entity is not being rendered with custom transparency settings from the "Shoulder Surfing" mod.
     */
    public static float getAlpha(Entity currentEntity) {
        Supplier<Float> alphaSupplier;
        var cameraEntity = Minecraft.getInstance().getCameraEntity();
        var cameraEntityRenderer = ShoulderSurfing.getInstance().getCameraEntityRenderer();

        if (cameraEntity == null) {
            return 1.0F;
        }

        if (currentEntity.is(cameraEntity) && cameraEntityRenderer.isRenderingCameraEntity()) {
            alphaSupplier = cameraEntityRenderer::getCameraEntityAlpha;
        } else {
            alphaSupplier = () -> 1.0F;
        }

        return alphaSupplier.get();
    }

    private ShoulderSurfingCompat() { /* NO-OP */}
}
