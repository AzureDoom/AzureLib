package mod.azure.azurelib.cache.object;

import net.minecraft.util.math.vector.Vector3f;

/**
 * Vertex data holder
 * @param position The position of the vertex
 * @param texU The texture U coordinate
 * @param texV The texture V coordinate
 */
public class GeoVertex {
	protected final Vector3f position;
	protected final float texU;
	protected final float texV;
	
	public GeoVertex(final Vector3f position, final float texU, final float texV) {
		this.position = position;
		this.texU = texU;
		this.texV = texV;
	}
	
	public GeoVertex(double x, double y, double z) {
		this(new Vector3f((float)x, (float)y, (float)z), 0, 0);
	}

	public GeoVertex withUVs(float texU, float texV) {
		return new GeoVertex(this.position, texU, texV);
	}

	public Vector3f position() {
		return position;
	}

	public float texU() {
		return texU;
	}

	public float texV() {
		return texV;
	}
}