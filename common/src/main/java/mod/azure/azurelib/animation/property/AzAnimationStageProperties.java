package mod.azure.azurelib.animation.property;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.property.codec.AzAnimationStagePropertiesCodec;

public class AzAnimationStageProperties extends AzAnimationProperties {

    public static final Function<FriendlyByteBuf, AzAnimationStageProperties> DECODER =
        AzAnimationStagePropertiesCodec.DECODER;

    public static final BiConsumer<FriendlyByteBuf, AzAnimationStageProperties> ENCODER =
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

    private AzPlayBehavior playBehavior;

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
        this.startTickOffset = startTickOffset;
    }

    public boolean hasPlayBehavior() {
        return playBehavior != null;
    }

    @Override
    public AzAnimationStageProperties withAnimationSpeed(double animationSpeed) {
        this.animationSpeed = animationSpeed;
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
    public AzAnimationStageProperties withEasingType(@NotNull AzEasingType easingType) {
        this.easingType = easingType;
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

    public AzAnimationStageProperties withPlayBehavior(@NotNull AzPlayBehavior playBehavior) {
        this.playBehavior = playBehavior;
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
        this.transitionLength = transitionLength;
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
        this.startTickOffset = startTickOffset;
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
        this.freezeTickOffset = freezeTickOffset;
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
        this.repeatXTimes = repeatXTimes;
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
        this.isReversing = isReversing;
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
