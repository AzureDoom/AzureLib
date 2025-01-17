/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.keyframe;

/**
 * A named pair object that stores a {@link Keyframe} and a double representing a temporally placed {@code Keyframe}
 *
 * @param keyframe  The {@code Keyframe} at the tick time
 * @param startTick The animation tick time at the start of this {@code Keyframe}
 */
public record KeyframeLocation<T extends Keyframe<?>>(
    T keyframe,
    double startTick
) {}
