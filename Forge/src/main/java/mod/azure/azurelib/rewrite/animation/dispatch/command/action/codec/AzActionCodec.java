package mod.azure.azurelib.rewrite.animation.dispatch.command.action.codec;

import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.registry.AzActionRegistry;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The AzActionCodec class serves as an implementation of the {@link StreamCodec} interface specifically designed for
 * encoding and decoding {@link AzAction} objects. This codec encodes and decodes AzAction instances using their
 * associated resource locations and registered codecs within the {@link AzActionRegistry}. <br>
 * This class provides the necessary functionality to serialize an AzAction to a {@link PacketBuffer} and deserialize it
 * back, ensuring proper handling of resource location and associated data. It relies on the AzActionRegistry to
 * dynamically retrieve the appropriate codec and handle the serialization or deserialization process. <br>
 * Use this implementation in scenarios where AzAction objects need to be serialized or deserialized for efficient data
 * transmission or storage.
 */
/**
 * The AzActionCodec class is responsible for encoding and decoding {@link AzAction} instances in 1.20.1, using the
 * {@link PacketBuffer} and the {@link AzActionRegistry} for maintaining associations between resource locations and
 * their respective action codecs.
 */
public class AzActionCodec {

    public AzAction decode(PacketBuffer byteBuf) {
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

    public void encode(PacketBuffer byteBuf, AzAction action) {
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
