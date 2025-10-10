package mod.azure.azurelib.common.animation.easing;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

import mod.azure.azurelib.common.animation.controller.keyframe.AzAnimationPoint;
import mod.azure.azurelib.core.utils.Interpolations;

public interface AzEasingType {

    String name();

    Double2DoubleFunction buildTransformer(Double value);

    StreamCodec<FriendlyByteBuf, AzEasingType> STREAM_CODEC = StreamCodec.of(
        (buf, val) -> buf.writeUtf(val.name()),
        buf -> Objects.requireNonNull(AzEasingTypeRegistry.getOrNull(buf.readUtf()))
    );

    default double apply(AzAnimationPoint animationPoint) {
        Double easingVariable = null;

        if (animationPoint.keyframe() != null && animationPoint.keyframe().easingArgs().size() > 0)
            easingVariable = animationPoint.keyframe().easingArgs().get(0).get();

        return apply(animationPoint, easingVariable, animationPoint.currentTick() / animationPoint.transitionLength());
    }

    default double apply(AzAnimationPoint animationPoint, Double easingValue, double lerpValue) {
        if (animationPoint.currentTick() >= animationPoint.transitionLength())
            return (float) animationPoint.animationEndValue();

        return Interpolations.lerp(
            animationPoint.animationStartValue(),
            animationPoint.animationEndValue(),
            buildTransformer(easingValue).apply(lerpValue)
        );
    }
}
