/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.keyframe.event.data;

import java.util.Objects;

import mod.azure.azurelib.core.keyframe.Keyframe;

/**
 * Particle {@link Keyframe} instruction holder
 */
public class ParticleKeyframeData extends KeyFrameData {

    private final String effect;

    private final String locator;

    private final String script;

    public ParticleKeyframeData(double startTick, String effect, String locator, String script) {
        super(startTick);

        this.script = script;
        this.locator = locator;
        this.effect = effect;
    }

    /**
     * Gets the effect id given by the {@link Keyframe} instruction from the {@code animation.json}
     */
    public String getEffect() {
        return this.effect;
    }

    /**
     * Gets the locator string given by the {@link Keyframe} instruction from the {@code animation.json}
     */
    public String getLocator() {
        return this.locator;
    }

    /**
     * Gets the script string given by the {@link Keyframe} instruction from the {@code animation.json}
     */
    public String script() {
        return this.script;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartTick(), effect, locator, script);
    }
}
