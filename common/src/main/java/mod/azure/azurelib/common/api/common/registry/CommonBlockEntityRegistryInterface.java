package mod.azure.azurelib.common.api.common.registry;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

import mod.azure.azurelib.common.platform.Services;

/**
 * Example of using this Interface to create a new Block Entity:
 * <p>
 * The following code demonstrates how to register a new block entity type in the game:
 * </p>
 *
 * <pre>{@code
 *
 * public static final Supplier<BlockEntityType<TestBlockEntity>> TEST_BLOCKENTITY = CommonBlockEntityRegistryInterface
 *     .registerBlockEntity(
 *         "modid",
 *         "blockentityname",
 *         () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST_BLOCK.get()).build(null)
 *     );
 * }</pre>
 * <p>
 * In this example:
 * </p>
 * <ul>
 * <li><code>registerBlockEntity</code> is a method to register a new block entity with the specified mod ID and block
 * entity name.</li>
 * <li><code>TestBlockEntity::new</code> is a reference to the constructor of the <code>TestBlockEntity</code>
 * class.</li>
 * <li><code>TEST_BLOCK.get()</code> is a reference to the block associated with this block entity.</li>
 * </ul>
 * <p>
 * The {@link net.minecraft.world.level.block.entity.BlockEntityType BlockEntityType} class represents a block entity
 * type in the game.
 * </p>
 */
public interface CommonBlockEntityRegistryInterface {

    /**
     * Registers a new Block Entity.
     *
     * @param modID           The mod ID.
     * @param blockEntityName The name of the Block Entity.
     * @param blockEntity     A supplier for the block entity type.
     * @param <T>             The type of the block entity.
     * @return A supplier for the registered block entity type.
     */
    static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
        String modID,
        String blockEntityName,
        Supplier<BlockEntityType<T>> blockEntity
    ) {
        return Services.COMMON_REGISTRY.registerBlockEntity(modID, blockEntityName, blockEntity);
    }
}
