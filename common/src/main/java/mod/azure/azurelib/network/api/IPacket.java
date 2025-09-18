/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.network.api;

import net.minecraft.resources.ResourceLocation;

public interface IPacket<T> {

    ResourceLocation getPacketId();

    T getPacketData();

    IPacketEncoder<T> getEncoder();

    IPacketDecoder<T> getDecoder();
}
