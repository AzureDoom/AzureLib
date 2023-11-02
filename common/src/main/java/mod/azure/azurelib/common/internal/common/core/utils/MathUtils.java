package mod.azure.azurelib.common.internal.common.core.utils;

public final class MathUtils {
	public static int clamp(int x, int min, int max) {
		return Math.max(Math.min(x, max), min);
	}

	public static float clamp(float x, float min, float max) {
		return Math.max(Math.min(x, max), min);
	}

	public static double clamp(double x, double min, double max) {
		return Math.max(Math.min(x, max), min);
	}

	public static int cycler(int x, int min, int max) {
		if (x > max) {
			return min;
		}

		return x < min ? max : x;
	}

	public static float cycler(float x, float min, float max) {
		if (x > max) {
			return min;
		}

		return x < min ? max : x;
	}

	public static double cycler(double x, double min, double max) {
		if (x > max) {
			return min;
		}

		return x < min ? max : x;
	}

	private MathUtils() {
		throw new UnsupportedOperationException();
	}
}
