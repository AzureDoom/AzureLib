/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.object;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

public class FreePositionTracker implements PositionTracker {

    private final Vec3 pos;

    public FreePositionTracker(Vec3 pos) {
        this.pos = pos;
    }

    @Override
    public Vec3 currentPosition() {
        return pos;
    }

    @Override
    public BlockPos currentBlockPosition() {
        return BlockPos.containing(pos);
    }

    @Override
    public boolean isVisibleBy(LivingEntity entity) {
        return true;
    }
}
