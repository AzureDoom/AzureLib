package mod.azure.azurelib.rewrite.animation.property;

import net.minecraft.network.PacketBuffer;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.rewrite.animation.easing.AzEasingType;
import mod.azure.azurelib.rewrite.animation.easing.AzEasingTypes;
import mod.azure.azurelib.rewrite.animation.property.codec.AzAnimationPropertiesCodec;

public class AzAnimationProperties {

    public static final Function<PacketBuffer, AzAnimationProperties> DECODER = AzAnimationPropertiesCodec.DECODER;

    public static final BiConsumer<PacketBuffer, AzAnimationProperties> ENCODER = AzAnimationPropertiesCodec.ENCODER;

    public static final AzAnimationProperties DEFAULT = new AzAnimationProperties(1D, AzEasingTypes.NONE, 0F);

    public static final AzAnimationProperties EMPTY = new AzAnimationProperties(null, null, null);

    protected final Double animationSpeed;

    protected final AzEasingType easingType;

    protected final Float transitionLength;

    public AzAnimationProperties(
        Double animationSpeed,
        AzEasingType easingType,
        Float transitionLength
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

    public AzAnimationProperties withEasingType(AzEasingType easingType) {
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength);
    }

    public AzAnimationProperties withTransitionLength(float transitionLength) {
        return new AzAnimationProperties(animationSpeed, easingType, transitionLength);
    }

    public double animationSpeed() {
        return animationSpeed == null ? DEFAULT.animationSpeed() : animationSpeed;
    }

    public AzEasingType easingType() {
        return easingType == null ? DEFAULT.easingType() : easingType;
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
