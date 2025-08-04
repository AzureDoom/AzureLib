package mod.azure.azurelib.rewrite.animation.property;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.rewrite.animation.easing.AzEasingType;
import mod.azure.azurelib.rewrite.animation.property.codec.AzAnimationPropertiesCodec;

public class AzAnimationProperties {

    public static final Function<FriendlyByteBuf, AzAnimationProperties> DECODER = AzAnimationPropertiesCodec.DECODER;

    public static final BiConsumer<FriendlyByteBuf, AzAnimationProperties> ENCODER = AzAnimationPropertiesCodec.ENCODER;

    protected @Nullable Double animationSpeed;

    protected @Nullable AzEasingType easingType;

    protected @Nullable Float transitionLength;

    protected @Nullable Double startTickOffset;

    public AzAnimationProperties(
        @Nullable Double animationSpeed,
        @Nullable AzEasingType easingType,
        @Nullable Float transitionLength,
        @Nullable Double startTickOffset
    ) {
        this.animationSpeed = animationSpeed;
        this.easingType = easingType;
        this.transitionLength = transitionLength;
        this.startTickOffset = startTickOffset;
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

    public AzAnimationProperties withAnimationSpeed(double animationSpeed) {
        this.animationSpeed = animationSpeed;
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength, startTickOffset);
    }

    public AzAnimationProperties withEasingType(@NotNull AzEasingType easingType) {
        this.easingType = easingType;
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength, startTickOffset);
    }

    public AzAnimationProperties withTransitionLength(float transitionLength) {
        this.transitionLength = transitionLength;
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength, startTickOffset);
    }

    public AzAnimationProperties withStartTickOffset(double startTickOffset) {
        this.startTickOffset = startTickOffset;
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength, startTickOffset);
    }

    public double animationSpeed() {
        return animationSpeed == null ? 1D : animationSpeed;
    }

    public AzEasingType easingType() {
        return easingType;
    }

    public float transitionLength() {
        return transitionLength == null ? 0F : transitionLength;
    }

    public double startTickOffset() {
        return startTickOffset == null ? 0D : startTickOffset;
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
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationSpeed, easingType, transitionLength, startTickOffset);
    }
}
