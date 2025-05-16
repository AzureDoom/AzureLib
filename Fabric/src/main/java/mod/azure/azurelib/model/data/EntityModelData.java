/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.model.data;

/**
 * Container class for various pieces of data relating to a model's current state.
 */

@Deprecated()
public class EntityModelData {

    public boolean isSitting;

    public boolean isChild;

    public float netHeadYaw;

    public float headPitch;

    public EntityModelData(boolean shouldSit, boolean isChild, float netHeadYaw, float headPitch) {
        this.isSitting = shouldSit;
        this.isChild = isChild;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
    }

    public boolean isSitting() {
        return isSitting;
    }

    public boolean isChild() {
        return isChild;
    }

    public float netHeadYaw() {
        return netHeadYaw;
    }

    public float headPitch() {
        return headPitch;
    }
}
