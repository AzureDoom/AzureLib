package mod.azure.azurelib.animation.primitive;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;

/**
 * Represents a container for baked animations in the AzureLib framework. This record holds mappings for precompiled
 * animation instances ({@link AzBakedAnimation}) and resource includes ({@link ResourceLocation}) for use in
 * animation-driven content. <br>
 * The `AzBakedAnimations` structure provides functionality for retrieving animations by name and supporting external
 * resource references via the includes mapping, enabling extensibility and reuse of animations across various contexts.
 * <br>
 * Immutable and designed for efficient storage and retrieval of animation data.
 */
public record AzBakedAnimations(
    Map<String, AzBakedAnimation> animations,
    Map<String, ResourceLocation> includes
) {

    /**
     * Gets an {@link AzBakedAnimation} by its name, if present
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

}
