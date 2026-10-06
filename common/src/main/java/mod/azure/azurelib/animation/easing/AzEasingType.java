package mod.azure.azurelib.animation.easing;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.controller.keyframe.AzAnimationPoint;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframe;
import mod.azure.azurelib.core.utils.Interpolations;

public interface AzEasingType {

    String name();

    Double2DoubleFunction buildTransformer(Double value);

    Function<FriendlyByteBuf, AzEasingType> DECODER = buf -> Objects.requireNonNull(
        AzEasingTypeRegistry.getOrNull(buf.readUtf())
    );

    BiConsumer<FriendlyByteBuf, AzEasingType> ENCODER = (buf, val) -> buf.writeUtf(val.name());

    default double apply(AzAnimationPoint animationPoint) {
        Double easingVariable = null;
        var keyframe = animationPoint.keyframe();

        if (keyframe != null && !keyframe.easingArgs().isEmpty())
            easingVariable = keyframe.easingArgs().get(0).get();

        return apply(animationPoint, easingVariable, animationPoint.currentTick() / animationPoint.transitionLength());
    }

    default double apply(AzAnimationPoint animationPoint, Double easingValue, double lerpValue) {
        if (animationPoint.currentTick() >= animationPoint.transitionLength())
            return (float) animationPoint.animationEndValue();

        return Interpolations.lerp(
            animationPoint.animationStartValue(),
            animationPoint.animationEndValue(),
            buildTransformer(easingValue).get(lerpValue)
        );
    }

    /**
     * Resolves, ahead of time, the curve this type would build for {@code keyframe}, so it can be baked when the
     * animation loads instead of being looked up every frame.
     * <p>
     * Return {@code null} when the curve can't be known in advance (for example, when it depends on a Molang
     * expression) or when this type doesn't work through a single curve. That is the default, and simply means the
     * curve is resolved at playback time as before. A non-null result must be exactly what this type would use for that
     * keyframe at playback.
     * </p>
     *
     * @param keyframe a keyframe whose easing type is this type
     */
    @Nullable
    default Double2DoubleFunction resolveTransformer(AzKeyframe<?> keyframe) {
        return null;
    }

    default boolean usesKeyframeData() {
        return false;
    }
}
