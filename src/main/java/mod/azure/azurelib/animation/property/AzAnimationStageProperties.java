package mod.azure.azurelib.animation.property;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.property.codec.AzAnimationStagePropertiesCodec;
import mod.azure.azurelib.network.AzByteBuf;

public class AzAnimationStageProperties extends AzAnimationProperties {

    public static final Function<AzByteBuf, AzAnimationStageProperties> DECODER =
        AzAnimationStagePropertiesCodec.DECODER;

    public static final BiConsumer<AzByteBuf, AzAnimationStageProperties> ENCODER =
        AzAnimationStagePropertiesCodec.ENCODER;;

    public static final AzAnimationStageProperties DEFAULT = new AzAnimationStageProperties(
        1D,
        AzEasingTypes.NONE,
        AzPlayBehaviors.PLAY_ONCE,
        0F,
        0D,
        0D,
        1D,
        false
    );

    public static final AzAnimationStageProperties EMPTY = new AzAnimationStageProperties(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

    private final @Nullable AzPlayBehavior playBehavior;

    public AzAnimationStageProperties(
        @Nullable Double animationSpeed,
        @Nullable AzEasingType easingType,
        @Nullable AzPlayBehavior playBehavior,
        @Nullable Float transitionLength,
        @Nullable Double startTickOffset,
        @Nullable Double freezeTickOffset,
        @Nullable Double repeatXTimes,
        @Nullable Boolean isReversing
    ) {
        super(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
        this.playBehavior = playBehavior;
    }

    public boolean hasPlayBehavior() {
        return playBehavior != null;
    }

    @Override
    public AzAnimationStageProperties withAnimationSpeed(double animationSpeed) {
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    @Override
    public AzAnimationStageProperties withEasingType(@Nonnull AzEasingType easingType) {
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public AzAnimationStageProperties withPlayBehavior(@Nonnull AzPlayBehavior playBehavior) {
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    @Override
    public AzAnimationStageProperties withTransitionLength(float transitionLength) {
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    @Override
    public AzAnimationStageProperties withStartTickOffset(double startTickOffset) {
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    @Override
    public AzAnimationStageProperties withFreezeTickOffset(double freezeTickOffset) {
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    @Override
    public AzAnimationStageProperties withRepeatXTimes(double repeatXTimes) {
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    @Override
    public AzAnimationStageProperties withShouldReverse(boolean isReversing) {
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public AzPlayBehavior playBehavior() {
        return playBehavior == null ? DEFAULT.playBehavior() : playBehavior;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        AzAnimationStageProperties that = (AzAnimationStageProperties) object;

        return Objects.equals(playBehavior, that.playBehavior) && super.equals(object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playBehavior);
    }
}
