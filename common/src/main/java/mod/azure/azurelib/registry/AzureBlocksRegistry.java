package mod.azure.azurelib.registry;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.function.Supplier;

import mod.azure.azurelib.blocks.TickingLightBlock;
import mod.azure.azurelib.platform.Services;

public class AzureBlocksRegistry {

    public static final Supplier<TickingLightBlock> TICKING_LIGHT_BLOCK = registerBlock(
        "lightblock",
        () -> new TickingLightBlock(
            BlockBehaviour.Properties.of(Material.AIR)
                .sound(SoundType.CANDLE)
                .lightLevel(TickingLightBlock.LIGHT_EMISSION)
                .noLootTable()
                .noCollission()
                .noOcclusion()
        )
    );

    /**
     * Registers a new Block.
     *
     * @param blockName The name of the block.
     * @param block     A supplier for the block.
     * @param <T>       The type of the block.
     * @return A supplier for the registered block.
     */
    static <T extends Block> Supplier<T> registerBlock(String blockName, Supplier<T> block) {
        return Services.COMMON_REGISTRY.register(Registry.BLOCK, blockName, block);
    }

    public static void init() {}
}
