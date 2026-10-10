package mod.azure.azurelib.animation.primitive;

import net.minecraft.util.ResourceLocation;

import java.util.Map;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.util.AzureLibException;

/**
 * Represents a container for baked animations in the AzureLib framework. This record holds mappings for precompiled
 * animation instances ({@link AzBakedAnimation}) and resource includes ({@link ResourceLocation}) for use in
 * animation-driven content. <br>
 * The `AzBakedAnimations` structure provides functionality for retrieving animations by name and supporting external
 * resource references via the includes mapping, enabling extensibility and reuse of animations across various contexts.
 * <br>
 * Immutable and designed for efficient storage and retrieval of animation data.
 */
public final class AzBakedAnimations {

    private final Map<String, AzBakedAnimation> animations;

    private final Map<String, ResourceLocation> includes;

    public AzBakedAnimations(Map<String, AzBakedAnimation> animations, Map<String, ResourceLocation> includes) {
        this.animations = animations;
        this.includes = includes;
    }

    public Map<String, AzBakedAnimation> animations() {
        return this.animations;
    }

    public Map<String, ResourceLocation> includes() {
        return this.includes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzBakedAnimations))
            return false;
        AzBakedAnimations other = (AzBakedAnimations) o;
        return java.util.Objects.equals(this.animations, other.animations)
            && java.util.Objects.equals(this.includes, other.includes);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.animations);
        result = 31 * result + java.util.Objects.hashCode(this.includes);
        return result;
    }

    @Override
    public String toString() {
        return "AzBakedAnimations[animations=" + this.animations + ", includes=" + this.includes + "]";
    }

    /**
     * Gets an {@link AzBakedAnimation} by its name, if present
     */
    @Nullable
    public AzBakedAnimation getAnimation(String name) {
        AzBakedAnimation result = animations.get(name);

        if (result != null || includes == null)
            return result;

        ResourceLocation otherFileID = includes.get(name);

        if (otherFileID == null)
            return null;

        AzBakedAnimations otherBakedAnims = AzBakedAnimationCache.getInstance().getNullable(otherFileID);

        if (otherBakedAnims == null) {
            AzureLib.LOGGER.error(
                "Animation '{}' is included from '{}', but that file is missing or failed to load",
                name,
                otherFileID
            );
            return null;
        }

        if (otherBakedAnims == this) {
            throw new AzureLibException(
                "The animation file '" + otherFileID + "' refers back to itself through includes."
            );
        }

        return otherBakedAnims.getAnimationWithoutIncludes(name);
    }

    private AzBakedAnimation getAnimationWithoutIncludes(String name) {
        return animations.get(name);
    }

}
