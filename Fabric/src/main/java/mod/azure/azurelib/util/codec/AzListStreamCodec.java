package mod.azure.azurelib.util.codec;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AzListStreamCodec<T> {

    private final Function<FriendlyByteBuf, T> decoder; // Function to decode an element from the buffer

    private final BiConsumer<FriendlyByteBuf, T> encoder; // BiConsumer to encode an element into the buffer

    public AzListStreamCodec(Function<FriendlyByteBuf, T> decoder, BiConsumer<FriendlyByteBuf, T> encoder) {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    public @NotNull List<T> decode(FriendlyByteBuf buf) {
        int size = buf.readByte(); // Read the size of the list
        List<T> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(buf)); // Decode each element using the provided decoder
        }

        return list;
    }

    public void encode(FriendlyByteBuf buf, List<T> elements) {
        buf.writeByte(elements.size()); // Write the size of the list
        elements.forEach(element -> encoder.accept(buf, element)); // Encode each element using the provided encoder
    }

}
