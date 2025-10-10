package mod.azure.azurelib.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

import mod.azure.azurelib.blocks.TickingLightEntity;
import mod.azure.azurelib.platform.Services;

public class AzureBlocksEntityRegistry {

    public static final Supplier<BlockEntityType<TickingLightEntity>> TICKING_LIGHT_ENTITY =
        registerBlockEntity(
            "lightblock",
            () -> BlockEntityType.Builder.of(TickingLightEntity::new, AzureBlocksRegistry.TICKING_LIGHT_BLOCK.get())
                .build(null)
        );

    /**
     * Registers a new Block Entity.
     *
     * @param blockEntityName The name of the Block Entity.
     * @param blockEntity     A supplier for the block entity type.
     * @param <T>             The type of the block entity.
     * @return A supplier for the registered block entity type.
     */
    static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
        String blockEntityName,
        Supplier<BlockEntityType<T>> blockEntity
    ) {
        return Services.COMMON_REGISTRY.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityName, blockEntity);
    }

    public static void init() {}
}
