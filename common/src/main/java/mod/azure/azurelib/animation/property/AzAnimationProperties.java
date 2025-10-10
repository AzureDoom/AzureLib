package mod.azure.azurelib.animation.property;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.property.codec.AzAnimationPropertiesCodec;

public class AzAnimationProperties {

    public static final Function<FriendlyByteBuf, AzAnimationProperties> DECODER = AzAnimationPropertiesCodec.DECODER;

    public static final BiConsumer<FriendlyByteBuf, AzAnimationProperties> ENCODER = AzAnimationPropertiesCodec.ENCODER;

    public static final AzAnimationProperties DEFAULT = new AzAnimationProperties(
        1D,
        null,
        0F,
        0D,
        0D,
        1D,
        false
    );

    public static final AzAnimationProperties EMPTY = new AzAnimationProperties(
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

    protected @Nullable Double animationSpeed;

    protected @Nullable AzEasingType easingType;

    protected @Nullable Float transitionLength;

    protected @Nullable Double startTickOffset;

    protected @Nullable Double freezeTickOffset;

    protected @Nullable Double repeatXTimes;

    protected @Nullable Boolean isReversing;

    public AzAnimationProperties(
        @Nullable Double animationSpeed,
        @Nullable AzEasingType easingType,
        @Nullable Float transitionLength,
        @Nullable Double startTickOffset,
        @Nullable Double freezeTickOffset,
        @Nullable Double repeatXTimes,
        @Nullable Boolean isReversing
    ) {
        this.animationSpeed = animationSpeed;
        this.easingType = easingType;
        this.transitionLength = transitionLength;
        this.startTickOffset = startTickOffset;
        this.freezeTickOffset = freezeTickOffset;
        this.repeatXTimes = repeatXTimes;
        this.isReversing = isReversing;
    }

    public boolean hasAnimationSpeed() {
        return animationSpeed != null;
    }

    public boolean hasEasingType() {
        return easingType != null;
    }

    public boolean hasTransitionLength() {
        return transitionLength != null;
    }

    public boolean hasStartTickOffset() {
        return startTickOffset != null;
    }

    public boolean hasFreezeTickOffset() {
        return freezeTickOffset != null;
    }

    public boolean hasRepeatXTimes() {
        return repeatXTimes != null;
    }

    public boolean hasReversing() {
        return isReversing != null;
    }

    public AzAnimationProperties withAnimationSpeed(double animationSpeed) {
        this.animationSpeed = animationSpeed;
        return new AzAnimationProperties(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public AzAnimationProperties withEasingType(@NotNull AzEasingType easingType) {
        this.easingType = easingType;
        return new AzAnimationProperties(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public AzAnimationProperties withTransitionLength(float transitionLength) {
        this.transitionLength = transitionLength;
        return new AzAnimationProperties(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public AzAnimationProperties withStartTickOffset(double startTickOffset) {
        this.startTickOffset = startTickOffset;
        return new AzAnimationProperties(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public AzAnimationProperties withFreezeTickOffset(double freezeTickOffset) {
        this.freezeTickOffset = freezeTickOffset;
        return new AzAnimationProperties(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public AzAnimationProperties withRepeatXTimes(double repeatXTimes) {
        this.repeatXTimes = repeatXTimes;
        return new AzAnimationProperties(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public AzAnimationProperties withShouldReverse(boolean isReversing) {
        this.isReversing = isReversing;
        return new AzAnimationProperties(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }

    public double animationSpeed() {
        return animationSpeed == null ? DEFAULT.animationSpeed() : animationSpeed;
    }

    public AzEasingType easingType() {
        return easingType;
    }

    public float transitionLength() {
        return transitionLength == null ? DEFAULT.transitionLength() : transitionLength;
    }

    public double startTickOffset() {
        return startTickOffset == null ? DEFAULT.startTickOffset() : startTickOffset;
    }

    public double freezeTickOffset() {
        return freezeTickOffset == null ? DEFAULT.freezeTickOffset() : freezeTickOffset;
    }

    public double repeatXTimes() {
        return repeatXTimes == null ? DEFAULT.repeatXTimes() : repeatXTimes;
    }

    public boolean isReversing() {
        return isReversing == null ? DEFAULT.isReversing() : isReversing;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        AzAnimationProperties that = (AzAnimationProperties) object;

        return Objects.equals(animationSpeed, that.animationSpeed) && Objects.equals(easingType, that.easingType)
            && Objects.equals(transitionLength, that.transitionLength) && Objects.equals(
                startTickOffset,
                that.startTickOffset
            ) && Objects.equals(freezeTickOffset, that.freezeTickOffset) && Objects.equals(
                repeatXTimes,
                that.repeatXTimes
            ) && Objects.equals(isReversing, that.isReversing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            animationSpeed,
            easingType,
            transitionLength,
            startTickOffset,
            freezeTickOffset,
            repeatXTimes,
            isReversing
        );
    }
}
