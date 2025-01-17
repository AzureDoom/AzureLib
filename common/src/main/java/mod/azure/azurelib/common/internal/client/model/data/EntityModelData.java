/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.client.model.data;

/**
 * Container class for various pieces of data relating to a model's current state.
 */
public record EntityModelData(
    boolean isSitting,
    boolean isChild,
    float netHeadYaw,
    float headPitch
) {}
