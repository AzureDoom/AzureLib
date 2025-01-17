/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface AbstractPacket extends CustomPacketPayload {

    void handle();
}
