/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import org.joml.Vector3f;

/**
 * Vertex data holder
 *
 * @param position The position of the vertex
 * @param texU     The texture U coordinate
 * @param texV     The texture V coordinate
 */
public record AzVertex(
    Vector3f position,
    float texU,
    float texV
) {

    public AzVertex(double x, double y, double z) {
        this(new Vector3f((float) x, (float) y, (float) z), 0, 0);
    }

    public AzVertex withUVs(float texU, float texV) {
        return new AzVertex(this.position, texU, texV);
    }
}
