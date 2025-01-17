package mod.azure.azurelib.common.internal.common.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

import mod.azure.azurelib.common.api.common.registry.CommonBlockRegistryInterface;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.blocks.TickingLightBlock;

public class AzureBlocksRegistry implements CommonBlockRegistryInterface {

    public static final Supplier<TickingLightBlock> TICKING_LIGHT_BLOCK = CommonBlockRegistryInterface.registerBlock(
        AzureLib.MOD_ID,
        "lightblock",
        () -> new TickingLightBlock(
            BlockBehaviour.Properties.of()
                .sound(SoundType.CANDLE)
                .lightLevel(TickingLightBlock.litBlockEmission(15))
                .pushReaction(PushReaction.DESTROY)
                .noOcclusion()
        )
    );

    public static void init() {}
}
