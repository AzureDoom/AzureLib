package mod.azure.azurelib.animation.primitive;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.keyframe.AzBoneAnimation;

/**
 * A compiled animation instance for use by the {@link AzAnimationController}<br>
 * Modifications or extensions of a compiled Animation are not supported, and therefore an instance of
 * <code>Animation</code> is considered final and immutable.
 */
public final class AzBakedAnimation {

    private final String name;

    private final double length;

    private final AzLoopType loopType;

    private final AzBoneAnimation[] boneAnimations;

    private final AzKeyframes keyframes;

    private final AzAnimationDefaults defaults;

    public AzBakedAnimation(
        String name,
        double length,
        AzLoopType loopType,
        AzBoneAnimation[] boneAnimations,
        AzKeyframes keyframes,
        AzAnimationDefaults defaults
    ) {
        this.name = name;
        this.length = length;
        this.loopType = loopType;
        this.boneAnimations = boneAnimations;
        this.keyframes = keyframes;
        this.defaults = defaults;
    }

    public String name() {
        return this.name;
    }

    public double length() {
        return this.length;
    }

    public AzLoopType loopType() {
        return this.loopType;
    }

    public AzBoneAnimation[] boneAnimations() {
        return this.boneAnimations;
    }

    public AzKeyframes keyframes() {
        return this.keyframes;
    }

    public AzAnimationDefaults defaults() {
        return this.defaults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzBakedAnimation))
            return false;
        AzBakedAnimation other = (AzBakedAnimation) o;
        return java.util.Objects.equals(this.name, other.name)
            && Double.compare(this.length, other.length) == 0
            && java.util.Objects.equals(this.loopType, other.loopType)
            && java.util.Objects.equals(this.boneAnimations, other.boneAnimations)
            && java.util.Objects.equals(this.keyframes, other.keyframes)
            && java.util.Objects.equals(this.defaults, other.defaults);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.name);
        result = 31 * result + Double.hashCode(this.length);
        result = 31 * result + java.util.Objects.hashCode(this.loopType);
        result = 31 * result + java.util.Objects.hashCode(this.boneAnimations);
        result = 31 * result + java.util.Objects.hashCode(this.keyframes);
        result = 31 * result + java.util.Objects.hashCode(this.defaults);
        return result;
    }

    @Override
    public String toString() {
        return "AzBakedAnimation[name=" + this.name + ", length=" + this.length + ", loopType=" + this.loopType
            + ", boneAnimations=" + this.boneAnimations + ", keyframes=" + this.keyframes + ", defaults="
            + this.defaults + "]";
    }

    public AzBakedAnimation(
        String name,
        double length,
        AzLoopType loopType,
        AzBoneAnimation[] boneAnimations,
        AzKeyframes keyframes
    ) {
        this(name, length, loopType, boneAnimations, keyframes, AzAnimationDefaults.NONE);
    }
}
