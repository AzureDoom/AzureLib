package mod.azure.azurelib.resource;

/**
 * A pixel marker for a glowlayer mask
 * 
 * @param x     The X coordinate of the pixel
 * @param y     The Y coordinate of the pixel
 * @param alpha The alpha value of the mask
 */
public class Pixel {
	public int x;
	public int y;
	public int alpha;

	public Pixel(int x, int y, int alpha) {
		this.x = x;
		this.y = y;
		this.alpha = alpha;
	}
}