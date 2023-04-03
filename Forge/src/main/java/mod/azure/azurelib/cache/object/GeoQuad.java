package mod.azure.azurelib.cache.object;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Quad data holder
 */
public class GeoQuad {
	protected final GeoVertex[] vertices;
	protected final Vector3f normal;
	protected final Direction direction;
	
	public GeoQuad(final GeoVertex[] vertices, final Vector3f normal, final Direction direction) {
		this.vertices = vertices;
		this.normal = normal;
		this.direction = direction;
	}
	
	public static GeoQuad build(GeoVertex[] vertices, double[] uvCoords, double[] uvSize, float texWidth,
			float texHeight, boolean mirror, Direction direction) {
		return build(vertices, (float) uvCoords[0], (float) uvCoords[1], (float) uvSize[0], (float) uvSize[1], texWidth,
				texHeight, mirror, direction);
	}

	public static GeoQuad build(GeoVertex[] vertices, float u, float v, float uSize, float vSize, float texWidth,
			float texHeight, boolean mirror, Direction direction) {
		float uWidth = (u + uSize) / texWidth;
		float vHeight = (v + vSize) / texHeight;
		u /= texWidth;
		v /= texHeight;
		Vector3f normal = direction.step();

		if (!mirror) {
			float tempWidth = uWidth;
			uWidth = u;
			u = tempWidth;

			normal.mul(-1, 1, 1);
		}

		vertices[0] = vertices[0].withUVs(u, v);
		vertices[1] = vertices[1].withUVs(uWidth, v);
		vertices[2] = vertices[2].withUVs(uWidth, vHeight);
		vertices[3] = vertices[3].withUVs(u, vHeight);

		return new GeoQuad(vertices, normal, direction);
	}

	public GeoVertex[] vertices() {
		return vertices;
	}

	public Vector3f normal() {
		return normal;
	}

	public Direction direction() {
		return direction;
	}
}
