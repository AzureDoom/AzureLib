package mod.azure.azurelib.rewrite.animation.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import mod.azure.azurelib.rewrite.animation.easing.AzEasingType;
import mod.azure.azurelib.rewrite.animation.easing.AzEasingTypes;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.rewrite.animation.property.codec.AzAnimationStagePropertiesCodec;

public class AzAnimationStageProperties extends AzAnimationProperties {

    public static final AzAnimationStagePropertiesCodec CODEC = new AzAnimationStagePropertiesCodec();

    public static final AzAnimationStageProperties DEFAULT = new AzAnimationStageProperties(
        1D,
        AzEasingTypes.NONE,
        AzPlayBehaviors.PLAY_ONCE,
        0F,
        0D
    );

    private AzPlayBehavior playBehavior;

    public AzAnimationStageProperties(
        @Nullable Double animationSpeed,
        @Nullable AzEasingType easingType,
        @Nullable AzPlayBehavior playBehavior,
        @Nullable Float transitionLength,
        @Nullable Double startTickOffset
    ) {
        super(animationSpeed, easingType, transitionLength, startTickOffset);
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
            startTickOffset
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
            startTickOffset
        );
    }

    public AzAnimationStageProperties withPlayBehavior(@NotNull AzPlayBehavior playBehavior) {
        this.playBehavior = playBehavior;
        return new AzAnimationStageProperties(
            animationSpeed,
            easingType,
            playBehavior,
            transitionLength,
            startTickOffset
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
            startTickOffset
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
            startTickOffset
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
