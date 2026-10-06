package mod.azure.azurelib.animation.easing;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import mod.azure.azurelib.animation.controller.keyframe.AzAnimationPoint;
import mod.azure.azurelib.core.utils.Interpolations;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
     * Whether this easing interprets the keyframe's {@code easingArgs} as type-specific data (bezier handles,
     * catmull-rom neighbors). Such types are only valid on a keyframe authored with that exact type and must not be
     * force-applied as a controller-wide override.
     */
    default boolean usesKeyframeData() {
        return false;
    }
}
