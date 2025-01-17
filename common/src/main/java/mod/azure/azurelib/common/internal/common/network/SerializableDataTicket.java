/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.internal.common.constant.DataTickets;
import mod.azure.azurelib.core.object.DataTicket;

/**
 * Network-compatible {@link DataTicket} implementation. Used for sending data from server -> client in an easy manner
 *
 * @deprecated
 */
@Deprecated(forRemoval = true)
public abstract class SerializableDataTicket<D> extends DataTicket<D> {

    public static final StreamCodec<RegistryFriendlyByteBuf, SerializableDataTicket<?>> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.STRING_UTF8,
            SerializableDataTicket::id,
            DataTickets::byName
        );

    protected SerializableDataTicket(String id, Class<? extends D> objectType) {
        super(id, objectType);
    }

    /**
     * Generate a new {@code SerializableDataTicket<Double>} for the given id
     *
     * @param id The unique id of your ticket. Include your modid
     */
    public static SerializableDataTicket<Double> ofDouble(ResourceLocation id) {
        return new SerializableDataTicket<>(id.toString(), Double.class) {

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Double> streamCodec() {
                return ByteBufCodecs.DOUBLE;
            }
        };
    }

    // Pre-defined typings for use

    /**
     * Generate a new {@code SerializableDataTicket<Float>} for the given id
     *
     * @param id The unique id of your ticket. Include your modid
     */
    public static SerializableDataTicket<Float> ofFloat(ResourceLocation id) {
        return new SerializableDataTicket<>(id.toString(), Float.class) {

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Float> streamCodec() {
                return ByteBufCodecs.FLOAT;
            }
        };
    }

    /**
     * Generate a new {@code SerializableDataTicket<Boolean>} for the given id
     *
     * @param id The unique id of your ticket. Include your modid
     */
    public static SerializableDataTicket<Boolean> ofBoolean(ResourceLocation id) {
        return new SerializableDataTicket<>(id.toString(), Boolean.class) {

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Boolean> streamCodec() {
                return ByteBufCodecs.BOOL;
            }
        };
    }

    /**
     * Generate a new {@code SerializableDataTicket<Integer>} for the given id
     *
     * @param id The unique id of your ticket. Include your modid
     */
    public static SerializableDataTicket<Integer> ofInt(ResourceLocation id) {
        return new SerializableDataTicket<>(id.toString(), Integer.class) {

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Integer> streamCodec() {
                return ByteBufCodecs.VAR_INT;
            }
        };
    }

    /**
     * Generate a new {@code SerializableDataTicket<String>} for the given id
     *
     * @param id The unique id of your ticket. Include your modid
     */
    public static SerializableDataTicket<String> ofString(ResourceLocation id) {
        return new SerializableDataTicket<>(id.toString(), String.class) {

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, String> streamCodec() {
                return ByteBufCodecs.STRING_UTF8;
            }
        };
    }

    /**
     * Generate a new {@code SerializableDataTicket<Enum>} for the given id
     *
     * @param id The unique id of your ticket. Include your modid
     */
    public static <E extends Enum<E>> SerializableDataTicket<E> ofEnum(ResourceLocation id, Class<E> enumClass) {
        return new SerializableDataTicket<>(id.toString(), enumClass) {

            public StreamCodec<? super RegistryFriendlyByteBuf, E> streamCodec() {
                return new StreamCodec<>() {

                    @Override
                    public @NotNull E decode(@NotNull RegistryFriendlyByteBuf buf) {
                        return Enum.valueOf(enumClass, buf.readUtf());
                    }

                    @Override
                    public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull E data) {
                        buf.writeUtf(data.toString());
                    }
                };
            }
        };
    }

    /**
     * @return The {@link StreamCodec} for the given SerializableDataTicket
     */
    public abstract StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec();
}
