package mod.azure.azurelib.cache.object;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Baked cuboid for a {@link GeoBone}
 */
public class GeoCube {
	protected final GeoQuad[] quads;
	protected final Vector3d pivot;
	protected final Vector3d rotation;
	protected final Vector3d size;
	protected final double inflate;
	protected final boolean mirror;
	
	public GeoCube(final GeoQuad[] quads, final Vector3d pivot, final Vector3d rotation, final Vector3d size, final double inflate, final boolean mirror) {
		this.quads = quads;
		this.pivot = pivot;
		this.rotation = rotation;
		this.size = size;
		this.inflate = inflate;
		this.mirror = mirror;
	}
	
	public GeoQuad[] quads() {
		return this.quads;
	}
	
	public Vector3d pivot() {
		return this.pivot;
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
