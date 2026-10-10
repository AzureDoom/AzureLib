package mod.azure.azurelib.animation.dispatch.command.action.codec;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.action.registry.AzActionRegistry;

/**
 * The AzActionCodec class provides functionality for encoding and decoding {@link AzAction} objects to and from a
 * {@link PacketBuffer}. It acts as a utility layer facilitating serialization and deserialization of actions within the
 * animation system.
 * <p>
 * The encoding process uses a unique identifier (ID) associated with each {@link AzAction}, retrieved via the
 * {@link AzActionRegistry}. Similarly, the decoding process relies on the ID to retrieve the corresponding decoder from
 * the registry. The {@link AzActionRegistry} maintains mappings between IDs, resource locations, and codecs.
 */
public class AzActionCodec {

    public @Nonnull AzAction decode(@Nonnull PacketBuffer byteBuf) {
        // Decode the ID for the corresponding AzAction
        short id = byteBuf.readShort();
        // Retrieve the action's codec using its ID from the registry
        Function<PacketBuffer, AzAction> codec = AzActionRegistry.getDecoderOrNull(id);

        if (codec == null) {
            throw new NullPointerException(
                "Could not find action decoder for a given action ID while decoding data. ID: " + id
            );
        }

        // Use the codec to decode the AzAction
        return codec.apply(byteBuf);
    }

    public void encode(@Nonnull PacketBuffer byteBuf, @Nonnull AzAction action) {
        // Get the resource location for the AzAction
        ResourceLocation resourceLocation = action.getResourceLocation();
        // Retrieve the corresponding ID and codec for the resource location
        Short id = AzActionRegistry.getIdOrNull(resourceLocation);
        BiConsumer<PacketBuffer, AzAction> encoder = AzActionRegistry.getEncoderOrNull(resourceLocation);

        if (id == null) {
            throw new NullPointerException(
                "Could not find action ID for a given resource location while encoding data. Resource Location: "
                    + resourceLocation
            );
        }

        // Write the ID to the buffer
        byteBuf.writeShort(id);

        if (encoder == null) {
            throw new NullPointerException(
                "Could not find action encoder for a given resource location while encoding data. Resource Location: "
                    + resourceLocation + ", ID: " + id
            );
        }

        // Use the encoder to encode the AzAction
        encoder.accept(byteBuf, action);
    }
}
