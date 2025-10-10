package mod.azure.azurelib.animation.primitive;

import com.google.gson.JsonElement;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mod.azure.azurelib.animation.controller.AzAnimationController;

/**
 * Loop type functional interface to define post-play handling for a given animation. <br>
 * Custom loop types are supported by extending this class and providing the extended class instance as the loop type
 * for the animation
 */
public interface AzLoopType {

    String name();

    /**
     * Override in a custom instance to dynamically decide whether an animation should repeat or stop
     *
     * @param animatable       The animating object relevant to this method call
     * @param controller       The {@link AzAnimationController} playing the current animation
     * @param currentAnimation The current animation that just played
     * @return Whether the animation should play again, or stop
     */
    boolean shouldPlayAgain(
        Object animatable,
        AzAnimationController<?> controller,
        AzBakedAnimation currentAnimation
    );

    Map<String, AzLoopType> LOOP_TYPES = new ConcurrentHashMap<>(5);

    AzLoopType FALSE = register("false", (animatable, controller, currentAnimation) -> false);

    AzLoopType TRUE = register("true", (animatable, controller, currentAnimation) -> true);

    /**
     * A predefined {@code AzLoopType} that indicates an animation should play only once without repeating. This loop
     * type is used to configure animations that are intended to run a single time and stop when completed, instead of
     * looping or holding on the last frame. The associated logic for this type always returns {@code false} for the
     * repeat condition, preventing the animation from replaying once it has finished.
     */
    AzLoopType PLAY_ONCE = register("play_once", FALSE);

    /**
     * A pre-defined AzLoopType representing the behavior of holding on the last frame of an animation after it
     * completes.
     * <p>
     * When used as the loop type for an animation, the animation will pause on its final frame. The `pause()` method of
     * the controller's state machine ensures this behavior. This is useful when the desired outcome is for the
     * animation to display its last frame persistently without replaying or resetting.
     * <p>
     * Implementation Details: - Utilizes the `controller.getStateMachine().pause()` method to halt the animation. -
     * Returns `true`, indicating the controller should not proceed to the next state or reset/replay the animation.
     */
    AzLoopType HOLD_ON_LAST_FRAME = register("hold_on_last_frame", (animatable, controller, currentAnimation) -> {
        controller.stateMachine().pause();

        return true;
    });

    /**
     * Represents a preconfigured {@code AzLoopType} that determines the looping behavior of an animation. The
     * {@code LOOP} type is designed to always repeat the animation, ensuring that the looping condition remains
     * consistently true. This constant is registered with AzureLib's loop handler using the static {@code register}
     * method. When used, it applies a behavior where the animation will indefinitely loop, regardless of any dynamic
     * runtime evaluations about the animatable object or animation state.
     */
    AzLoopType LOOP = register("loop", TRUE);

    /**
     * Retrieve a AzLoopType instance based on a {@link JsonElement}. Returns either {@link AzLoopType#PLAY_ONCE} or
     * {@link AzLoopType#LOOP} based on a boolean or string element type, or any other registered loop type with a
     * matching type string.
     *
     * @param json The <code>loop</code> {@link JsonElement} to attempt to parse
     * @return A usable AzLoopType instance
     */
    static AzLoopType fromJson(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) {
            return PLAY_ONCE;
        }

        var primitive = json.getAsJsonPrimitive();

        if (primitive.isBoolean()) {
            return primitive.getAsBoolean() ? LOOP : PLAY_ONCE;
        }

        if (primitive.isString()) {
            return fromString(primitive.getAsString());
        }

        return PLAY_ONCE;
    }

    /**
     * Retrieves an AzLoopType instance based on the given name. If the name does not match any registered loop type,
     * the default {@link AzLoopType#PLAY_ONCE} is returned.
     *
     * @param name The name of the loop type to retrieve.
     * @return The corresponding AzLoopType instance, or the default AzLoopType if no match is found.
     */
    static AzLoopType fromString(String name) {
        return LOOP_TYPES.getOrDefault(name, PLAY_ONCE);
    }

    static AzLoopType register(String name, AzLoopType loopType) {
        return register(name, (a, b, c) -> loopType.shouldPlayAgain(a, b, c));
    }

    /**
     * Register a AzLoopType with AzureLib for handling loop functionality of animations..<br>
     * <b><u>MUST be called during mod construct</u></b><br>
     * It is recommended you don't call this directly, and instead call it via {@code AzureLibUtil#addCustomLoopType}
     *
     * @param name                    The name of the loop type
     * @param shouldPlayAgainFunction The loop type to register
     * @return The registered {@code AzLoopType}
     */
    static AzLoopType register(
        String name,
        TriFunction<Object, AzAnimationController<?>, AzBakedAnimation, Boolean> shouldPlayAgainFunction
    ) {
        var loopType = new AzLoopType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean shouldPlayAgain(
                Object animatable,
                AzAnimationController<?> controller,
                AzBakedAnimation currentAnimation
            ) {
                return shouldPlayAgainFunction.apply(animatable, controller, currentAnimation);
            }
        };

        LOOP_TYPES.put(name, loopType);

        return loopType;
    }
}
