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

    protected final @Nullable Double animationSpeed;

    protected final @Nullable AzEasingType easingType;

    protected final @Nullable Float transitionLength;

    protected final @Nullable Double startTickOffset;

    protected final @Nullable Double freezeTickOffset;

    protected final @Nullable Double repeatXTimes;

    protected final @Nullable Boolean isReversing;

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

    /**
     * Returns these properties with every value that {@code overrides} sets put in place of this one's. Values that
     * {@code overrides} leaves unset ({@code null}) keep this object's value. Used to layer a sequence stage's own
     * playback settings over its controller's, so they apply to that stage only and never stick to the controller.
     *
     * @param overrides the properties to layer on top, or {@code null} for none
     * @return the merged properties, or {@code this} if {@code overrides} sets nothing
     */
    public AzAnimationProperties overlay(@Nullable AzAnimationProperties overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return this;
        }

        return new AzAnimationProperties(
            overrides.animationSpeed != null ? overrides.animationSpeed : animationSpeed,
            overrides.easingType != null ? overrides.easingType : easingType,
            overrides.transitionLength != null ? overrides.transitionLength : transitionLength,
            overrides.startTickOffset != null ? overrides.startTickOffset : startTickOffset,
            overrides.freezeTickOffset != null ? overrides.freezeTickOffset : freezeTickOffset,
            overrides.repeatXTimes != null ? overrides.repeatXTimes : repeatXTimes,
            overrides.isReversing != null ? overrides.isReversing : isReversing
        );
    }

    /**
     * @return whether none of the playback values are set
     */
    public boolean isEmpty() {
        return animationSpeed == null && easingType == null && transitionLength == null && startTickOffset == null
            && freezeTickOffset == null && repeatXTimes == null && isReversing == null;
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
