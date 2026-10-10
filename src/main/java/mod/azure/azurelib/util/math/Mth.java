package mod.azure.azurelib.util.math;

/**
 * Subset of the 1.18 {@code net.minecraft.util.Mth} helpers used by AzureLib, re-implemented so AzureLib does not
 * depend on the differently-named 1.7.10 {@code MathHelper}.
 */
public final class Mth {

    public static final float PI = (float) Math.PI;

    public static final float HALF_PI = (float) (Math.PI / 2D);

    public static final float TWO_PI = (float) (Math.PI * 2D);

    public static final float DEG_TO_RAD = (float) (Math.PI / 180D);

    public static final float RAD_TO_DEG = (float) (180D / Math.PI);

    private Mth() {
        throw new UnsupportedOperationException();
    }

    public static float sin(float value) {
        return (float) Math.sin(value);
    }

    public static float cos(float value) {
        return (float) Math.cos(value);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static float fastInvSqrt(float value) {
        return (float) (1.0D / Math.sqrt(value));
    }

    public static double fastInvSqrt(double value) {
        return 1.0D / Math.sqrt(value);
    }

    public static float fastInvCubeRoot(float value) {
        return (float) (1.0D / Math.cbrt(value));
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int floor(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int ceil(double value) {
        int i = (int) value;
        return value > i ? i + 1 : i;
    }

    public static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static float wrapDegrees(float value) {
        float f = value % 360.0F;
        if (f >= 180.0F) {
            f -= 360.0F;
        }
        if (f < -180.0F) {
            f += 360.0F;
        }
        return f;
    }

    public static double wrapDegrees(double value) {
        double d = value % 360.0D;
        if (d >= 180.0D) {
            d -= 360.0D;
        }
        if (d < -180.0D) {
            d += 360.0D;
        }
        return d;
    }

    public static float rotLerp(float delta, float start, float end) {
        return start + delta * wrapDegrees(end - start);
    }

    public static float degreesDifference(float start, float end) {
        return wrapDegrees(end - start);
    }

    public static float degreesDifferenceAbs(float start, float end) {
        return Math.abs(degreesDifference(start, end));
    }

    public static boolean equal(float a, float b) {
        return Math.abs(b - a) < 1.0E-5F;
    }

    public static boolean equal(double a, double b) {
        return Math.abs(b - a) < 1.0E-5F;
    }

    public static float abs(float value) {
        return Math.abs(value);
    }

    public static double square(double value) {
        return value * value;
    }

    public static float square(float value) {
        return value * value;
    }
}
