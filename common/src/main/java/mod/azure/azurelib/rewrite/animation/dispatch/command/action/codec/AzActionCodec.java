package mod.azure.azurelib.rewrite.animation.dispatch.command.action.codec;

import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.registry.AzActionRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * The AzActionCodec class serves as an implementation of the {@link StreamCodec} interface specifically designed for
 * encoding and decoding {@link AzAction} objects. This codec encodes and decodes AzAction instances using their
 * associated resource locations and registered codecs within the {@link AzActionRegistry}. <br>
 * This class provides the necessary functionality to serialize an AzAction to a {@link FriendlyByteBuf} and deserialize
 * it back, ensuring proper handling of resource location and associated data. It relies on the AzActionRegistry to
 * dynamically retrieve the appropriate codec and handle the serialization or deserialization process. <br>
 * Use this implementation in scenarios where AzAction objects need to be serialized or deserialized for efficient data
 * transmission or storage.
 */
public class AzActionCodec {

    public static AzAction decode(FriendlyByteBuf byteBuf) {
        // Read the ID of the action
        var id = byteBuf.readShort();

        // Retrieve the corresponding codec from the registry
        var codec = AzActionRegistry
                .<AzAction, AzActionCodec>getActionClassOrNull(id);

        // Throw an error if the codec is not found
        if (codec == null) {
            throw new NullPointerException(
                    "Could not find action codec for a given action id while decoding data. ID: " + id
            );
        }

        // Use the codec to decode the action
        return codec.decode(byteBuf);
    }

    public static void encode(FriendlyByteBuf byteBuf, AzAction action) {
        // Get the resource location of the action
        var resourceLocation = action.getResourceLocation();

        // Retrieve the ID and the corresponding codec for the action
        var id = AzActionRegistry.getIdOrNull(resourceLocation);
        var codec = AzActionRegistry
                .<AzAction, AzActionCodec>getCodecOrNull(resourceLocation);

        // Throw an error if either the ID or the codec is not found
        if (id == null) {
            throw new NullPointerException(
                    "Could not find action id for a given resource location while encoding data. Resource Location: "
                            + resourceLocation
            );
        }

        byteBuf.writeShort(id);

        if (codec == null) {
            throw new NullPointerException(
                    "Could not find action codec for a given resource location while encoding data. Resource Location: "
                            + resourceLocation + ", ID: " + id
            );
        }

        // Use the codec to encode the action
        codec.encode(byteBuf, action);
    }

}
