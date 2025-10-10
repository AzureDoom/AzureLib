package mod.azure.azurelib.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;

/**
 * Helper class for segregating client-side code
 */
public final class ClientUtils {

    /**
     * Retrieves the current client-side player instance.
     *
     * @return the {@link Player} instance corresponding to the local client player, or null if the player is not
     *         available
     */
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    /**
     * Retrieves the current client-side {@link Level} instance.
     *
     * @return the {@link Level} instance associated with the local client, or null if unavailable
     */
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }

    /**
     * Retrieves the current {@link AzAnimationController} for the specified target object and controller name. This
     * method attempts to access the animator associated with the target object and fetch the animation controller with
     * the provided name. If no animator or animation controller is found, appropriate warnings are logged, and null is
     * returned.
     *
     * @param target         the target object for which the animation controller is being retrieved
     * @param controllerName the name of the animation controller to be retrieved
     * @return the {@link AzAnimationController} instance associated with the specified target and controller name, or
     *         null if no animator or matching controller is found
     */
    public static AzAnimationController<Object> getCurrentAnimationController(Object target, String controllerName) {
        var animator = AzAnimatorAccessor.getOrNull(target);

        if (animator == null) {
            AzureLib.LOGGER.warn("Could not find animator for target: {}", target);
            return null;
        }

        var animationController = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (animationController == null) {
            AzureLib.LOGGER.warn("No animation controller found with name '{}' for target: {}", controllerName, target);
        }

        return animationController;
    }

    /**
     * Retrieves the current animation tick for a specified target object and animation controller name. This method is
     * client-side only and logs warnings if called on the server side or if no animation controller is found.
     *
     * @param target         the object for which the current animation tick is being retrieved
     * @param controllerName the name of the animation controller associated with the target object
     * @return the current animation tick as a double, or 0.0 if the animation tick is unavailable
     */
    public static double getCurrentAnimationTick(Object target, String controllerName) {
        if (target instanceof Entity entity && !entity.level().isClientSide) {
            AzureLib.LOGGER.warn("Animation tick can only be retrieved on the client side for target: {}", target);
            return 0D;
        }

        AzAnimationController<Object> animationController = ClientUtils.getCurrentAnimationController(
            target,
            controllerName
        );

        if (animationController == null) {
            AzureLib.LOGGER.warn(
                "No animation controller available for target: {} controller: {}",
                target,
                controllerName
            );
            return 0D;
        }

        return animationController.controllerTimer().getAdjustedTick();
    }

    /**
     * Retrieves the duration of the current animation for a given target object and animation controller name. If no
     * animation controller or current animation is found, a warning is logged, and 0.0 is returned.
     *
     * @param target         the target object for which the animation length is being retrieved
     * @param controllerName the name of the animation controller associated with the target object
     * @return the length of the current animation as a double, or 0.0 if no controller or animation is found
     */
    public static double getCurrentAnimationLength(Object target, String controllerName) {
        AzAnimationController<Object> animationController = ClientUtils.getCurrentAnimationController(
            target,
            controllerName
        );

        if (animationController == null) {
            AzureLib.LOGGER.warn("No animation controller found for target: {} controller: {}", target, controllerName);
            return 0D;
        }

        AzQueuedAnimation currentAnimation = animationController.currentAnimation();
        if (currentAnimation == null) {
            AzureLib.LOGGER.warn("No current animation found for target: {} controller: {}", target, controllerName);
            return 0D;
        }

        return currentAnimation.animation().length();
    }
}
