package mod.azure.azurelib.animation.primitive;

import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents a container for baked animations in the AzureLib framework. Holds mappings for precompiled animation
 * instances ({@link AzBakedAnimation}) and resource includes ({@link ResourceLocation}) for use in animation-driven
 * content.
 */
public class AzBakedAnimations {

    private final Map<String, AzBakedAnimation> animations;

    private final Map<String, ResourceLocation> includes;

    public AzBakedAnimations(Map<String, AzBakedAnimation> animations, Map<String, ResourceLocation> includes) {
        this.animations = animations;
        this.includes = includes;
    }

    public Map<String, AzBakedAnimation> animations() {
        return animations;
    }

    public Map<String, ResourceLocation> includes() {
        return includes;
    }

    /**
     * Gets an {@link AzBakedAnimation} by its name, if present.
     */
    @Nullable
    public AzBakedAnimation getAnimation(String name) {
        AzBakedAnimation result = animations.get(name);
        if (result == null && includes != null) {
            ResourceLocation otherFileID = includes.getOrDefault(name, null);
            if (otherFileID != null) {
                AzBakedAnimations otherBakedAnims = AzBakedAnimationCache.getInstance().getNullable(otherFileID);
                if (otherBakedAnims.equals(this)) {
                    throw new AzureLibException(
                        "The animation file '" + otherFileID +
                            "' refers back to itself through includes."
                    );
                } else {
                    result = otherBakedAnims.getAnimationWithoutIncludes(name);
                }
            }
        }
        return result;
    }

    private AzBakedAnimation getAnimationWithoutIncludes(String name) {
        return animations.get(name);
    }

    @Override
    public String toString() {
        return "AzBakedAnimations{" +
            "animations=" + animations +
            ", includes=" + includes +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AzBakedAnimations that = (AzBakedAnimations) o;

        if (!animations.equals(that.animations))
            return false;
        return includes.equals(that.includes);
    }

    @Override
    public int hashCode() {
        int result = animations.hashCode();
        result = 31 * result + includes.hashCode();
        return result;
    }
}
