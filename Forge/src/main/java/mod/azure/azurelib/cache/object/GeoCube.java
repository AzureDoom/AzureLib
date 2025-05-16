/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import net.minecraft.client.renderer.Vector3d;
import net.minecraft.util.math.Vec3d;

/**
 * Baked cuboid for a {@link GeoBone}
 */
public class GeoCube {
    private final GeoQuad[] quads;
    private final Vec3d pivot;
    private final Vec3d rotation;
    private final Vec3d size;
    private final double inflate;
    private final boolean mirror;

    public GeoCube(GeoQuad[] quads, Vec3d pivot, Vec3d rotation, Vec3d size, double inflate, boolean mirror) {
        this.quads = quads;
        this.pivot = pivot;
        this.rotation = rotation;
        this.size = size;
        this.inflate = inflate;
        this.mirror = mirror;
    }

    public GeoQuad[] quads() {
        return quads;
    }

    public Vec3d pivot() {
        return pivot;
    }

    public Vec3d rotation() {
        return rotation;
    }

    public Vec3d size() {
        return size;
    }

    public double inflate() {
        return inflate;
    }

    public boolean mirror() {
        return mirror;
    }
}
