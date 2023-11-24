package mod.azure.azurelib.common.internal.common.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mod.azure.azurelib.common.internal.client.model.data.EntityModelData;
import mod.azure.azurelib.common.internal.common.core.object.DataTicket;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.common.internal.common.AzureLib;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Stores the default (builtin) {@link DataTicket DataTickets} used in AzureLib.<br>
 * Additionally handles registration of {@link SerializableDataTicket SerializableDataTickets}
 */
public final class DataTickets {
	private static final Map<String, SerializableDataTicket<?>> SERIALIZABLE_TICKETS = new ConcurrentHashMap<>();
	
	// Builtin tickets
	// These tickets are used by AzureLib by default, usually added in by the GeoRenderer for use in animations
	public static final DataTicket<BlockEntity> BLOCK_ENTITY = new DataTicket<>("block_entity", BlockEntity.class);
	public static final DataTicket<ItemStack> ITEMSTACK = new DataTicket<>("itemstack", ItemStack.class);
	public static final DataTicket<Entity> ENTITY = new DataTicket<>("entity", Entity.class);
	public static final DataTicket<EquipmentSlot> EQUIPMENT_SLOT = new DataTicket<>("equipment_slot", EquipmentSlot.class);
	public static final DataTicket<EntityModelData> ENTITY_MODEL_DATA = new DataTicket<>("entity_model_data", EntityModelData.class);
	public static final DataTicket<Double> TICK = new DataTicket<>("tick", Double.class);
	public static final DataTicket<ItemDisplayContext> ITEM_RENDER_PERSPECTIVE = new DataTicket<>("item_render_perspective", ItemDisplayContext.class);
	
	// Builtin serializable tickets
	// These are not used anywhere by default, but are provided as examples
	// and for ease of use
	public static final SerializableDataTicket<Integer> ANIM_STATE = AzureLibUtil.addDataTicket(SerializableDataTicket.ofInt(AzureLib.modResource("anim_state")));
	public static final SerializableDataTicket<String> ANIM = AzureLibUtil.addDataTicket(SerializableDataTicket.ofString(AzureLib.modResource("anim")));
	public static final SerializableDataTicket<Integer> USE_TICKS = AzureLibUtil.addDataTicket(SerializableDataTicket.ofInt(AzureLib.modResource("use_ticks")));
	public static final SerializableDataTicket<Boolean> ACTIVE = AzureLibUtil.addDataTicket(SerializableDataTicket.ofBoolean(AzureLib.modResource("active")));
	public static final SerializableDataTicket<Boolean> OPEN = AzureLibUtil.addDataTicket(SerializableDataTicket.ofBoolean(AzureLib.modResource("open")));
	public static final SerializableDataTicket<Boolean> CLOSED = AzureLibUtil.addDataTicket(SerializableDataTicket.ofBoolean(AzureLib.modResource("closed")));
	public static final SerializableDataTicket<Direction> DIRECTION = AzureLibUtil.addDataTicket(SerializableDataTicket.ofEnum(AzureLib.modResource("direction"), Direction.class));

	@Nullable
	public static SerializableDataTicket<?> byName(String id) {
		return SERIALIZABLE_TICKETS.getOrDefault(id, null);
	}

	/**
	 * Register a {@link SerializableDataTicket} with AzureLib for handling custom data transmission.<br>
	 * It is recommended you don't call this directly, and instead call it via {@link AzureLibUtil#addDataTicket}
	 * @param ticket The SerializableDataTicket instance to register
	 * @return The registered instance
	 */
	public static <D> SerializableDataTicket<D> registerSerializable(SerializableDataTicket<D> ticket) {
		SerializableDataTicket<?> existingTicket = SERIALIZABLE_TICKETS.putIfAbsent(ticket.id(), ticket);

		if (existingTicket != null)
			AzureLib.LOGGER.error("Duplicate SerializableDataTicket registered! This will cause issues. Existing: {}, New: {}", existingTicket.id(), ticket.id());

		return ticket;
	}

	private DataTickets() {
		throw new UnsupportedOperationException();
	}
}
