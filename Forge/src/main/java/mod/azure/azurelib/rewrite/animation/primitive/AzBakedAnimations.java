package mod.azure.azurelib.rewrite.animation.primitive;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

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
    public AzBakedAnimation getAnimation(String name) {
        return animations.get(name);
    }

}
