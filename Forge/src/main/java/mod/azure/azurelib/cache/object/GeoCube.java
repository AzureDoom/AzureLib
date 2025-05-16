/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Baked cuboid for a {@link GeoBone}
 */
@Deprecated()
public class GeoCube {

    private final GeoQuad[] quads;

    private final Vector3d pivot;

    private final Vector3d rotation;

    private final Vector3d size;

    private final double inflate;

    private final boolean mirror;

    public GeoCube(GeoQuad[] quads, Vector3d pivot, Vector3d rotation, Vector3d size, double inflate, boolean mirror) {
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

    public Vector3d pivot() {
        return pivot;
    }

    public Vector3d rotation() {
        return rotation;
    }

    public Vector3d size() {
        return size;
    }

    public double inflate() {
        return inflate;
    }

    public boolean mirror() {
        return mirror;
    }
}
