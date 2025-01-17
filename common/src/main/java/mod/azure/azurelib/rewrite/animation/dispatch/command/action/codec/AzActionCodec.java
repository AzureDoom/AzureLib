package mod.azure.azurelib.rewrite.animation.dispatch.command.action.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.registry.AzActionRegistry;

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
public class AzActionCodec implements StreamCodec<FriendlyByteBuf, AzAction> {

    @Override
    public @NotNull AzAction decode(@NotNull FriendlyByteBuf byteBuf) {
        var id = byteBuf.readShort();
        var codec = AzActionRegistry
            .<AzAction, StreamCodec<FriendlyByteBuf, AzAction>>getCodecOrNull(id);

        if (codec == null) {
            throw new NullPointerException(
                "Could not find action codec for a given action id while decoding data. ID: " + id
            );
        }

        return codec.decode(byteBuf);
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf byteBuf, @NotNull AzAction action) {
        var resourceLocation = action.getResourceLocation();
        var id = AzActionRegistry.getIdOrNull(resourceLocation);
        var codec = AzActionRegistry
            .<AzAction, StreamCodec<FriendlyByteBuf, AzAction>>getCodecOrNull(resourceLocation);

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

        codec.encode(byteBuf, action);
    }
}
