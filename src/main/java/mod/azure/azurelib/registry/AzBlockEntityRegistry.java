package mod.azure.azurelib.registry;

import net.minecraft.block.entity.BlockEntity;

import java.util.Map;

import mod.azure.azurelib.mixins.accessor.BlockEntityAccessor;

/**
 * Registers block entity classes with Beta's block entity registry (MCP {@code TileEntity.addMapping}), replacing
 * StationAPI's {@code BlockEntityRegisterEvent}. Without a mapping, Beta throws when it saves a chunk containing the
 * block entity.
 * <p>
 * Call it on <em>both</em> sides, from your OSL {@code init} entrypoint, before any world loads:
 *
 * <pre>{@code
 * AzBlockEntityRegistry.register(ExampleBlockEntity.class, "yourmodid:example_block_entity");
 * }</pre>
 *
 * The id is saved in the block entity's NBT, so it must never change once worlds exist; prefix it with your mod id. Id
 * clashes with vanilla or another mod, and registering the same class under two ids, throw instead of silently
 * replacing the mapping.
 */
public final class AzBlockEntityRegistry {

    private AzBlockEntityRegistry() {
        throw new UnsupportedOperationException();
    }

    public static synchronized void register(Class<? extends BlockEntity> blockEntityClass, String id) {
        if (blockEntityClass == null || id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Block entity class and id are required");
        }

        Map<?, ?> byId = BlockEntityAccessor.azurelib$getIdToClass();
        Object existingClass = byId.get(id);
        if (existingClass == blockEntityClass) {
            return;
        }
        if (existingClass != null) {
            throw new IllegalStateException(
                "Block entity id '" + id + "' is already registered to " + ((Class<?>) existingClass).getName()
            );
        }
        Object existingId = BlockEntityAccessor.azurelib$getClassToId().get(blockEntityClass);
        if (existingId != null) {
            throw new IllegalStateException(
                blockEntityClass.getName() + " is already registered as block entity '" + existingId + "'"
            );
        }

        BlockEntityAccessor.azurelib$register(blockEntityClass, id);
    }
}
