package mod.azure.azurelib.util;

import net.minecraft.block.Block;

/**
 * A block and its metadata. Minecraft 1.7.10 has no block states, so this is what AzureLib's block-on-bone rendering
 * takes in place of the {@code IBlockState} used on later versions.
 */
public final class AzBlockState {

    private final Block block;

    private final int meta;

    public AzBlockState(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    public static AzBlockState of(Block block) {
        return new AzBlockState(block, 0);
    }

    public Block getBlock() {
        return this.block;
    }

    public int getMeta() {
        return this.meta;
    }
}
