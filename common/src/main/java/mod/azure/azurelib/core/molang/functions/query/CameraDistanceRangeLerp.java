package mod.azure.azurelib.core.molang.functions.query;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * {@code query.camera_distance_range_lerp(a, b)}: 0 when the camera is at or closer than the nearer distance, 1 at or
 * beyond the farther one, and a linear blend between. The two distances can be given in either order. Handy for fading
 * out detail animations at range.
 */
public class CameraDistanceRangeLerp extends ContextQueryFunction {

    public CameraDistanceRangeLerp(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    protected double evaluate(MolangQueryContext context) {
        double a = this.getArg(0);
        double b = this.getArg(1);
        double near = Math.min(a, b);
        double far = Math.max(a, b);

        if (far == near)
            return context.distanceFromCamera() >= far ? 1 : 0;

        double t = (context.distanceFromCamera() - near) / (far - near);

        return Math.clamp(t, 0, 1);
    }
}
