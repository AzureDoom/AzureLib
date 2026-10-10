package mod.azure.azurelib.animation.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

/**
 * Playback defaults authored into an animation file (for example from the AzureLib Blockbench plugin), used when the
 * code that plays the animation doesn't choose a play behavior itself.
 *
 * <pre>
 * {@code
 * "attack": {
 *   "loop": "repeat_x_times",   // true | false | "hold_on_last_frame" | "ping_pong" | "repeat_x_times" | "freeze_on_frame"
 *   "repeat_times": 3,          // only read for repeat_x_times
 *   "freeze_at": 1.25           // seconds, only read for freeze_on_frame
 * }
 * }
 * </pre>
 *
 * @param playBehavior a {@link mod.azure.azurelib.animation.play_behavior.AzPlayBehaviorRegistry} name, or {@code null}
 *                     if the file doesn't specify {@code loop}. Kept as a name and resolved when the animation is
 *                     queued, so behaviors registered after resources load (e.g. weighted pools) work.
 * @param repeatTimes  the authored repeat count, or {@code 0} if none
 * @param freezeTick   the authored freeze point in ticks, or {@code -1} if none
 */
public final class AzAnimationDefaults {

    private final String playBehavior;

    private final double repeatTimes;

    private final double freezeTick;

    public AzAnimationDefaults(String playBehavior, double repeatTimes, double freezeTick) {
        this.playBehavior = playBehavior;
        this.repeatTimes = repeatTimes;
        this.freezeTick = freezeTick;
    }

    public String playBehavior() {
        return this.playBehavior;
    }

    public double repeatTimes() {
        return this.repeatTimes;
    }

    public double freezeTick() {
        return this.freezeTick;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzAnimationDefaults))
            return false;
        AzAnimationDefaults other = (AzAnimationDefaults) o;
        return java.util.Objects.equals(this.playBehavior, other.playBehavior)
            && Double.compare(this.repeatTimes, other.repeatTimes) == 0
            && Double.compare(this.freezeTick, other.freezeTick) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.playBehavior);
        result = 31 * result + Double.hashCode(this.repeatTimes);
        result = 31 * result + Double.hashCode(this.freezeTick);
        return result;
    }

    @Override
    public String toString() {
        return "AzAnimationDefaults[playBehavior=" + this.playBehavior + ", repeatTimes=" + this.repeatTimes
            + ", freezeTick=" + this.freezeTick + "]";
    }

    public static final AzAnimationDefaults NONE = new AzAnimationDefaults(null, 0, -1);

    public boolean hasFreezeTick() {
        return freezeTick >= 0;
    }

    public static AzAnimationDefaults fromJson(JsonObject animationObj) {
        String playBehavior = parsePlayBehavior(animationObj.get("loop"));

        if (playBehavior == null) {
            return NONE;
        }

        double repeatTimes = readNumber(animationObj, "repeat_times", 0);
        double freezeSeconds = readNumber(animationObj, "freeze_at", -1);
        double freezeTick = freezeSeconds < 0 ? -1 : freezeSeconds * 20d;

        return new AzAnimationDefaults(playBehavior, repeatTimes, freezeTick);
    }

    /**
     * Maps the Bedrock-compatible {@code loop} field onto play behavior registry names: {@code true} is {@code "loop"},
     * {@code false} is {@code "play_once"}, and strings are used as-is ({@code "hold_on_last_frame"} already matches
     * its behavior's name).
     */
    private static @Nullable String parsePlayBehavior(@Nullable JsonElement loop) {
        if (!(loop instanceof JsonPrimitive)) {
            return null;
        }

        if (((JsonPrimitive) loop).isBoolean()) {
            return ((JsonPrimitive) loop).getAsBoolean() ? "loop" : "play_once";
        }

        if (((JsonPrimitive) loop).isString()) {
            String name = ((JsonPrimitive) loop).getAsString().trim();

            // Older AzureLib Blockbench plugin versions wrote Blockbench's internal names here.
            switch ((name)) {
                case "":
                    return null;
                case "once":
                    return "play_once";
                case "hold":
                    return "hold_on_last_frame";
                default:
                    return name;
            }
        }

        return null;
    }

    private static double readNumber(JsonObject obj, String key, double fallback) {
        JsonElement element = obj.get(key);

        if (element instanceof JsonPrimitive && ((JsonPrimitive) element).isNumber()) {
            return ((JsonPrimitive) element).getAsDouble();
        }

        return fallback;
    }
}
