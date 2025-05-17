package mod.azure.azurelib.ai.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public class AzurePathFinder extends PathFinder {

    public AzurePathFinder(NodeProcessor processor, int maxVisitedNodes) {
        super(processor, maxVisitedNodes);
    }

    @Nullable
    @Override
    public Path func_227478_a_(
        Region regionIn,
        MobEntity mob,
        Set<BlockPos> targetPositions,
        float maxRange,
        int accuracy,
        float searchDepthMultiplier
    ) {
        Path path = super.func_227478_a_(regionIn, mob, targetPositions, maxRange, accuracy, searchDepthMultiplier);
        return path == null ? null : new PatchedPath(path);
    }

    static class PatchedPath extends Path {

        public PatchedPath(Path original) {
            super(copyPathPoints(original), original.getTarget(), original.reachesTarget());
        }

        @Override
        public Vec3d getVectorFromIndex(Entity entity, int index) {
            PathPoint point = this.getPathPointFromIndex(index);
            double d0 = point.x + MathHelper.floor(entity.getWidth() + 1.0F) * 0.5D;
            double d1 = point.y;
            double d2 = point.z + MathHelper.floor(entity.getWidth() + 1.0F) * 0.5D;
            return new Vec3d(d0, d1, d2);
        }

        private static List<PathPoint> copyPathPoints(Path original) {
            List<PathPoint> points = new ArrayList();
            for (int i = 0; i < original.getCurrentPathLength(); i++) {
                points.add(original.getPathPointFromIndex(i));
            }
            return points;
        }
    }
}
