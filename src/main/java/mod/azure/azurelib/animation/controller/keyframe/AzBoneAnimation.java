package mod.azure.azurelib.animation.controller.keyframe;

import mod.azure.azurelib.core.math.IValue;

/**
 * A record of a deserialized animation for a given bone.<br>
 * Responsible for holding the various {@link AzKeyframe Keyframes} for the bone's animation transformations
 *
 * @param boneName          The name of the bone as listed in the {@code animation.json}
 * @param rotationKeyframes The deserialized rotation {@code Keyframe} stack
 * @param positionKeyframes The deserialized position {@code Keyframe} stack
 * @param scaleKeyframes    The deserialized scale {@code Keyframe} stack
 */
public final class AzBoneAnimation {

    private final String boneName;

    private final AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes;

    private final AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes;

    private final AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes;

    public AzBoneAnimation(
        String boneName,
        AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes,
        AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes,
        AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes
    ) {
        this.boneName = boneName;
        this.rotationKeyframes = rotationKeyframes;
        this.positionKeyframes = positionKeyframes;
        this.scaleKeyframes = scaleKeyframes;
    }

    public String boneName() {
        return this.boneName;
    }

    public AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes() {
        return this.rotationKeyframes;
    }

    public AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes() {
        return this.positionKeyframes;
    }

    public AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes() {
        return this.scaleKeyframes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzBoneAnimation))
            return false;
        AzBoneAnimation other = (AzBoneAnimation) o;
        return java.util.Objects.equals(this.boneName, other.boneName)
            && java.util.Objects.equals(this.rotationKeyframes, other.rotationKeyframes)
            && java.util.Objects.equals(this.positionKeyframes, other.positionKeyframes)
            && java.util.Objects.equals(this.scaleKeyframes, other.scaleKeyframes);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.boneName);
        result = 31 * result + java.util.Objects.hashCode(this.rotationKeyframes);
        result = 31 * result + java.util.Objects.hashCode(this.positionKeyframes);
        result = 31 * result + java.util.Objects.hashCode(this.scaleKeyframes);
        return result;
    }

    @Override
    public String toString() {
        return "AzBoneAnimation[boneName=" + this.boneName + ", rotationKeyframes=" + this.rotationKeyframes
            + ", positionKeyframes=" + this.positionKeyframes + ", scaleKeyframes=" + this.scaleKeyframes + "]";
    }
}
