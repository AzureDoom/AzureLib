package mod.azure.azurelib.common.animation.pose;

/**
 * The per-channel blend rules shared by animation controllers and the procedural pose layer, so a weighted controller
 * and a weighted pose modifier combine a bone channel the same way.
 * <ul>
 * <li>Override: {@link #blend} moves from what is underneath toward the new value by the weight.</li>
 * <li>Additive rotation and position: {@link #additive} adds the value's offset from the bind pose, scaled by the
 * weight.</li>
 * <li>Additive scale: {@link #additiveScale} multiplies by the value's ratio to the bind scale, eased from 1 by the
 * weight.</li>
 * </ul>
 */
public final class AzTransformMath {

    private AzTransformMath() {}

    public static float blend(float from, float to, float weight) {
        return from + (to - from) * weight;
    }

    public static float additive(float base, float value, float bind, float weight) {
        return base + (value - bind) * weight;
    }

    public static float additiveScale(float base, float value, float bind, float weight) {
        return base * blend(1, ratio(value, bind), weight);
    }

    public static float ratio(float value, float bindScale) {
        return bindScale == 0 ? value : value / bindScale;
    }
}
