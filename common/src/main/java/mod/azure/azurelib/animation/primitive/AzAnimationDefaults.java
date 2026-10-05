package mod.azure.azurelib.animation.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

/**
 * Playback defaults authored into an animation file (for example from the AzureLib Blockbench plugin), used when the
 * code that plays the animation doesn't choose a play behavior itself.
 *
 * <pre>{@code
 * "attack": {
 *   "loop": "repeat_x_times",   // true | false | "hold_on_last_frame" | "ping_pong" | "repeat_x_times" | "freeze_on_frame"
 *   "repeat_times": 3,          // only read for repeat_x_times
 *   "freeze_at": 1.25           // seconds, only read for freeze_on_frame
 * }
 * }</pre>
 *
 * @param playBehavior a {@link mod.azure.azurelib.animation.play_behavior.AzPlayBehaviorRegistry} name, or {@code null}
 *                     if the file doesn't specify {@code loop}. Kept as a name and resolved when the animation is
 *                     queued, so behaviors registered after resources load (e.g. weighted pools) work.
 * @param repeatTimes  the authored repeat count, or {@code 0} if none
 * @param freezeTick   the authored freeze point in ticks, or {@code -1} if none
 */
public record AzAnimationDefaults(
    @Nullable String playBehavior,
    double repeatTimes,
    double freezeTick
) {

    public static final AzAnimationDefaults NONE = new AzAnimationDefaults(null, 0, -1);

    public boolean hasFreezeTick() {
        return freezeTick >= 0;
    }

    public static AzAnimationDefaults fromJson(JsonObject animationObj) {
        var playBehavior = parsePlayBehavior(animationObj.get("loop"));

        if (playBehavior == null) {
            return NONE;
        }

        var repeatTimes = readNumber(animationObj, "repeat_times", 0);
        var freezeSeconds = readNumber(animationObj, "freeze_at", -1);
        var freezeTick = freezeSeconds < 0 ? -1 : freezeSeconds * 20d;

        return new AzAnimationDefaults(playBehavior, repeatTimes, freezeTick);
    }

    /**
     * Maps the Bedrock-compatible {@code loop} field onto play behavior registry names: {@code true} is {@code "loop"},
     * {@code false} is {@code "play_once"}, and strings are used as-is ({@code "hold_on_last_frame"} already matches
     * its behavior's name).
     */
    private static @Nullable String parsePlayBehavior(@Nullable JsonElement loop) {
        if (!(loop instanceof JsonPrimitive primitive)) {
            return null;
        }

        if (primitive.isBoolean()) {
            return primitive.getAsBoolean() ? "loop" : "play_once";
        }

        if (primitive.isString()) {
            var name = primitive.getAsString().trim();

            // Older AzureLib Blockbench plugin versions wrote Blockbench's internal names here.
            return switch (name) {
                case "" -> null;
                case "once" -> "play_once";
                case "hold" -> "hold_on_last_frame";
                default -> name;
            };
        }

        return null;
    }

    private static double readNumber(JsonObject obj, String key, double fallback) {
        var element = obj.get(key);

        if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
            return primitive.getAsDouble();
        }

        return fallback;
    }
}
