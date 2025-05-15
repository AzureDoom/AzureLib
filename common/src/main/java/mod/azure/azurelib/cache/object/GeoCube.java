/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import net.minecraft.world.phys.Vec3;

/**
 * Baked cuboid for a {@link GeoBone}
 */
public record GeoCube(GeoQuad[] quads, Vec3 pivot, Vec3 rotation, Vec3 size, double inflate, boolean mirror) {}
