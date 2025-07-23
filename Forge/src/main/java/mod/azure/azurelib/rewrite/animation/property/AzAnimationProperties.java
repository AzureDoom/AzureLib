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

    public static final AzAnimationProperties DEFAULT = new AzAnimationProperties(1D, null, 0F);

    public static final AzAnimationProperties EMPTY = new AzAnimationProperties(null, null, null);

    protected final @Nullable Double animationSpeed;

    protected final @Nullable AzEasingType easingType;

    protected final @Nullable Float transitionLength;

    public AzAnimationProperties(
        @Nullable Double animationSpeed,
        @Nullable AzEasingType easingType,
        @Nullable Float transitionLength
    ) {
        this.animationSpeed = animationSpeed;
        this.easingType = easingType;
        this.transitionLength = transitionLength;
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

    public AzAnimationProperties withAnimationSpeed(double animationSpeed) {
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength);
    }

    public AzAnimationProperties withEasingType(@NotNull AzEasingType easingType) {
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength);
    }

    public AzAnimationProperties withTransitionLength(float transitionLength) {
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength);
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
            && Objects.equals(transitionLength, that.transitionLength);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationSpeed, easingType, transitionLength);
    }
}
