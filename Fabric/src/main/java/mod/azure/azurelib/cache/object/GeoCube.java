package mod.azure.azurelib.cache.object;

import net.minecraft.world.phys.Vec3;

/**
 * Baked cuboid for a {@link GeoBone}
 */
public class GeoCube {
	protected final GeoQuad[] quads;
	protected final Vec3 pivot;
	protected final Vec3 rotation;
	protected final Vec3 size;
	protected final double inflate;
	protected final boolean mirror;

	public GeoCube(final GeoQuad[] quads, final Vec3 pivot, final Vec3 rotation, final Vec3 size, final double inflate, final boolean mirror) {
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

	public Vec3 pivot() {
		return this.pivot;
	}

	public Vec3 rotation() {
		return rotation;
	}

	public Vec3 size() {
		return size;
	}

	public double inflate() {
		return inflate;
	}

	public boolean mirror() {
		return mirror;
	}
}
