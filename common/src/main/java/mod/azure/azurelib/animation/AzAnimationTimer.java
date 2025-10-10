package mod.azure.azurelib.animation;

import net.minecraft.client.Minecraft;

import mod.azure.azurelib.util.client.RenderUtils;

/**
 * AzAnimationTimer is responsible for managing animation progression based on game events and time deltas. It keeps
 * track of the current animation time and ensures smooth transitions during various game states, such as pausing and
 * resuming. <br>
 * The class relies on the provided {@link AzAnimatorConfig} for configurable behaviors, such as whether animations
 * continue during game pauses or specific error handling preferences.
 */
public class AzAnimationTimer {

    private final AzAnimatorConfig config;

    // Remnants from GeoModel.
    private double animTime;

    private double lastGameTickTime;

    private boolean wasPausedLastFrame;

    /**
     * Constructs a new instance of AzAnimationTimer with the given configuration.
     *
     * @param config The configuration settings used to configure the animation timer. It includes parameters such as
     *               bone reset time, behavior during game pause, and whether to crash if a bone is missing.
     */
    public AzAnimationTimer(AzAnimatorConfig config) {
        this.config = config;
    }

    /**
     * Updates the animation timer by calculating the time delta since the last frame and applying it to the internal
     * animation time. This method handles game pause states and adjusts the time calculations accordingly. <br>
     * Behavior: <b>If the game is paused:</b>
     * <ul>
     * <li>Sets an internal flag to indicate the paused state.</li>
     * <li>Returns immediately if animations should not play while paused.</li>
     * </ul>
     * <b>If transitioning from paused to unpaused:</b>
     * <ul>
     * <li>Resets the frame delta to prevent large time skips in animations.</li>
     * </ul>
     * Accumulates the computed time delta into the animation time tracker to control the progression of animations.
     */
    public void tick() {
        var minecraft = Minecraft.getInstance();
        var currentRenderTick = RenderUtils.getCurrentTick();

        if (minecraft.isPaused()) {
            if (!wasPausedLastFrame) {
                // If this is the first frame of the game pause time, we need to set a flag.
                this.wasPausedLastFrame = true;
            }

            if (!config.shouldPlayAnimationsWhileGamePaused()) {
                // If we cannot play animations while the game is paused, then return early.
                return;
            }
        }

        // Compute the delta render tick for this frame. This calculation by default does not account for the game
        // pause state, which means that the difference here could be massive by the time the player unpauses.
        var deltaRenderTick = currentRenderTick - lastGameTickTime;

        if (wasPausedLastFrame && !minecraft.isPaused()) {
            // If this is the first frame of the game play time, we need to set a flag and adjust the deltaRenderTick.
            this.wasPausedLastFrame = false;
            // To account for the deltaRenderTick being massive on exiting the game pause state, we simply set
            // it to 0. This will result in no difference being added to animTime, allowing animations to
            // continue right where it left off.
            deltaRenderTick = 0;
        }

        // Add the deltaRenderTick to animTime. animTime is what controls the progress of animations.
        this.animTime += deltaRenderTick;
        this.lastGameTickTime = currentRenderTick;
    }

    /**
     * Retrieves the current animation time.
     *
     * @return The current animation time as a double value, representing the accumulated time used for the progression
     *         of animations.
     */
    public double getAnimTime() {
        return animTime;
    }
}
