/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import mod.azure.azurelib.common.registry.AzureBlocksRegistry;

/**
 * Helper class for various AzureLib-specific functions.
 */
public record AzureLibUtil() {

    public static <T> T self(Object object) {
        return (T) object;
    }

    public static boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB, int distance) {
        return Math.abs(blockPosA.getX() - blockPosB.getX()) <= distance && Math.abs(
            blockPosA.getY() - blockPosB.getY()
        ) <= distance && Math.abs(
            blockPosA.getZ() - blockPosB.getZ()
        ) <= distance;
    }

    public static BlockPos findFreeSpace(Level world, BlockPos blockPos, int maxDistance) {
        if (blockPos == null)
            return null;

        var offsets = new int[maxDistance * 2 + 1];
        offsets[0] = 0;
        for (var i = 2; i <= maxDistance * 2; i += 2) {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (var x : offsets)
            for (var y : offsets)
                for (var z : offsets) {
                    var offsetPos = blockPos.offset(x, y, z);
                    var state = world.getBlockState(offsetPos);
                    if (state.isAir() || state.getBlock().equals(AzureBlocksRegistry.TICKING_LIGHT_BLOCK.get()))
                        return offsetPos;
                }
        return null;
    }
}
