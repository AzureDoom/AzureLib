package mod.azure.azurelib.network;

import mod.azure.azurelib.core.object.DataTicket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Network-compatible {@link mod.azure.azurelib.core.object.DataTicket} implementation.
 * Used for sending data from server -> client in an easy manner
 */
public abstract class SerializableDataTicket<D> extends DataTicket<D> {
	public SerializableDataTicket(String id, Class<? extends D> objectType) {
		super(id, objectType);
	}

	/**
	 * Encode the object to a packet buffer for transmission
	 * @param data The object to be serialized
	 * @param buffer The buffer to serialize the object to
	 */
	public abstract void encode(D data, PacketBuffer buffer);

	/**
	 * Decode the object from a packet buffer after transmission
	 * @param buffer The buffer to deserialize the object from
	 * @return A new instance of your data object
	 */
	public abstract D decode(PacketBuffer buffer);

	// Pre-defined typings for use

	/**
	 * Generate a new {@code SerializableDataTicket<Double>} for the given id
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<Double> ofDouble(ResourceLocation id) {
		return new SerializableDataTicket(id.toString(), Double.class) {
			@Override
			public void encode(Object data, PacketBuffer buffer) {
				buffer.writeDouble((double) data);
			}

			@Override
			public Double decode(PacketBuffer buffer) {
				return buffer.readDouble();
			}
		};
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Float>} for the given id
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<Float> ofFloat(ResourceLocation id) {
		return new SerializableDataTicket(id.toString(), Float.class) {
			@Override
			public void encode(Object data, PacketBuffer buffer) {
				buffer.writeFloat((float) data);
			}

			@Override
			public Float decode(PacketBuffer buffer) {
				return buffer.readFloat();
			}
		};
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Boolean>} for the given id
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<Boolean> ofBoolean(ResourceLocation id) {
		return new SerializableDataTicket(id.toString(), Boolean.class) {
			@Override
			public void encode(Object data, PacketBuffer buffer) {
				buffer.writeBoolean((boolean) data);
			}

			@Override
			public Boolean decode(PacketBuffer buffer) {
				return buffer.readBoolean();
			}
		};
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Integer>} for the given id
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<Integer> ofInt(ResourceLocation id) {
		return new SerializableDataTicket(id.toString(), Integer.class) {
			@Override
			public void encode(Object data, PacketBuffer buffer) {
				buffer.writeVarInt((int) data);
			}

			@Override
			public Integer decode(PacketBuffer buffer) {
				return buffer.readVarInt();
			}
		};
	}

	/**
	 * Generate a new {@code SerializableDataTicket<String>} for the given id
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static SerializableDataTicket<String> ofString(ResourceLocation id) {
		return new SerializableDataTicket(id.toString(), String.class) {
			@Override
			public void encode(Object data, PacketBuffer buffer) {
				buffer.writeUtf((String) data);
			}

			@Override
			public String decode(PacketBuffer buffer) {
				return buffer.readUtf();
			}
		};
	}

	/**
	 * Generate a new {@code SerializableDataTicket<Enum>} for the given id
	 * @param id The unique id of your ticket. Include your modid
	 */
	public static <E extends Enum<E>> SerializableDataTicket<E> ofEnum(ResourceLocation id, Class<E> enumClass) {
		return new SerializableDataTicket(id.toString(), enumClass) {
			@Override
			public void encode(Object data, PacketBuffer buffer) {
				buffer.writeUtf(data.toString());
			}

			@Override
			public E decode(PacketBuffer buffer) {
				return Enum.valueOf(enumClass, buffer.readUtf());
			}
		};
	}
}
