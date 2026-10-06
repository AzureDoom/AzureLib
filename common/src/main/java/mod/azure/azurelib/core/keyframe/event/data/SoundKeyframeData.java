/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.keyframe.event.data;

import java.util.Objects;

import mod.azure.azurelib.animation.controller.keyframe.AzKeyframe;

/**
 * Sound {@link AzKeyframe} instruction holder
 */
@SuppressWarnings("unused")
public class SoundKeyframeData extends KeyFrameData {

    private final String sound;

    private final int hashCode;

    public SoundKeyframeData(Double startTick, String sound) {
        super(startTick);

        this.sound = sound;
        this.hashCode = 31 * (31 + Double.hashCode(getStartTick())) + Objects.hashCode(sound);
    }

    /**
     * Gets the sound id given by the {@link AzKeyframe} instruction from the {@code animation.json}
     */
    public String getSound() {
        return this.sound;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
