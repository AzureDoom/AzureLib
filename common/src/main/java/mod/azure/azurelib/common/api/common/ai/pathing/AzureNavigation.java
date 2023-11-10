package mod.azure.azurelib.common.api.common.ai.pathing;

import java.util.Objects;

import mod.azure.azurelib.common.internal.common.ai.pathing.AzurePathFinder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

/* Credit to Bob Mowzie and pau101 for most of the code, 
 * code source for the base class can be found here: 
 * https://github.com/BobMowzie/MowziesMobs/blob/master/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java
 * */
public class AzureNavigation extends GroundPathNavigation {
    @Nullable
    private BlockPos pathToPosition;

	public AzureNavigation(Mob entity, Level world) {
		super(entity, world);
	}

	@Override
	protected PathFinder createPathFinder(int maxVisitedNodes) {
		this.nodeEvaluator = new WalkNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		return new AzurePathFinder(this.nodeEvaluator, maxVisitedNodes);
	}

	@Override
	protected void trimPath() {
		super.trimPath();
		for (int i = 0; i < this.path.getNodeCount(); ++i) {
			Node node = this.path.getNode(i);
			Node node2 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
			BlockState blockState = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));
			if (!blockState.is(BlockTags.STAIRS))
				continue;
			this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
			if (node2 == null || node.y < node2.y)
				continue;
			this.path.replaceNode(i + 1, node.cloneAndMove(node2.x, node.y + 1, node2.z));
		}
	}

	@Override
	protected void followThePath() {
		Path path = Objects.requireNonNull(this.path);
		Vec3 entityPos = this.getTempMobPos();
		int pathLength = path.getNodeCount();
		for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
			if (path.getNode(i).y != Math.floor(entityPos.y)) {
				pathLength = i;
				break;
			}
		}
		final Vec3 base = entityPos.add(-this.mob.getBbWidth() * 0.5F, 0.0F, -this.mob.getBbWidth() * 0.5F);
		final Vec3 max = base.add(this.mob.getBbWidth(), this.mob.getBbHeight(), this.mob.getBbWidth());
		if (this.tryShortcut(path, new Vec3(this.mob.getX(), this.mob.getY(), this.mob.getZ()), pathLength, base, max)) {
			if (this.isAt(path, 0.5F) || this.atElevationChange(path) && this.isAt(path, this.mob.getBbWidth() * 0.5F)) {
				this.mob.getLookControl().setLookAt(path.getNextEntityPos(this.mob));
				path.setNextNodeIndex(path.getNextNodeIndex() + 1);
			}
		}
		this.doStuckDetection(entityPos);
	}

    @Override
    public Path createPath(BlockPos blockPos, int i) {
        this.pathToPosition = blockPos;
        return super.createPath(blockPos, i);
    }

    @Override
    public Path createPath(Entity entity, int i) {
        this.pathToPosition = entity.blockPosition();
        return super.createPath(entity, i);
    }

    @Override
    public boolean moveTo(Entity entity, double d) {
        Path path = this.createPath(entity, 0);
        if (path != null) {
            return this.moveTo(path, d);
        }
        this.pathToPosition = entity.blockPosition();
        this.speedModifier = d;
        return true;
    }

	@Override
	public void tick() {
		super.tick();
        if (this.isDone()) {
            if (this.pathToPosition != null) {
                if (this.pathToPosition.closerToCenterThan(this.mob.position(), this.mob.getBbWidth()) || this.mob.getY() > (double)this.pathToPosition.getY() && BlockPos.containing(this.pathToPosition.getX(), this.mob.getY(), this.pathToPosition.getZ()).closerToCenterThan(this.mob.position(), this.mob.getBbWidth())) {
                    this.pathToPosition = null;
                } else {
                    this.mob.getMoveControl().setWantedPosition(this.pathToPosition.getX(), this.pathToPosition.getY(), this.pathToPosition.getZ(), this.speedModifier);
                }
            }
            return;
        }
		if (this.getTargetPos() != null)
			this.mob.getLookControl().setLookAt(this.getTargetPos().getX(), this.getTargetPos().getY(), this.getTargetPos().getZ());
	}

	private boolean isAt(Path path, float threshold) {
		final Vec3 pathPos = path.getNextEntityPos(this.mob);
		return Mth.abs((float) (this.mob.getX() - pathPos.x)) < threshold && Mth.abs((float) (this.mob.getZ() - pathPos.z)) < threshold && Math.abs(this.mob.getY() - pathPos.y) < 1.0D;
	}

	private boolean atElevationChange(Path path) {
		final int curr = path.getNextNodeIndex();
		final int end = Math.min(path.getNodeCount(), curr + Mth.ceil(this.mob.getBbWidth() * 0.5F) + 1);
		final int currY = path.getNode(curr).y;
		for (int i = curr + 1; i < end; i++) {
			if (path.getNode(i).y != currY) {
				return true;
			}
		}
		return false;
	}

	private boolean tryShortcut(Path path, Vec3 entityPos, int pathLength, Vec3 base, Vec3 max) {
		for (int i = pathLength; --i > path.getNextNodeIndex();) {
			final Vec3 vec = path.getEntityPosAtNode(this.mob, i).subtract(entityPos);
			if (this.sweep(vec, base, max)) {
				path.setNextNodeIndex(i);
				return false;
			}
		}
		return true;
	}

	static final float EPSILON = 1.0E-8F;

	// Based off of
	// https://github.com/andyhall/voxel-aabb-sweep/blob/d3ef85b19c10e4c9d2395c186f9661b052c50dc7/index.js
	private boolean sweep(Vec3 vec, Vec3 base, Vec3 max) {
		float t = 0.0F;
		float max_t = (float) vec.length();
		if (max_t < EPSILON)
			return true;
		final float[] tr = new float[3];
		final int[] ldi = new int[3];
		final int[] tri = new int[3];
		final int[] step = new int[3];
		final float[] tDelta = new float[3];
		final float[] tNext = new float[3];
		final float[] normed = new float[3];
		for (int i = 0; i < 3; i++) {
			float value = element(vec, i);
			boolean dir = value >= 0.0F;
			step[i] = dir ? 1 : -1;
			float lead = element(dir ? max : base, i);
			tr[i] = element(dir ? base : max, i);
			ldi[i] = leadEdgeToInt(lead, step[i]);
			tri[i] = trailEdgeToInt(tr[i], step[i]);
			normed[i] = value / max_t;
			tDelta[i] = Mth.abs(max_t / value);
			float dist = dir ? (ldi[i] + 1 - lead) : (lead - ldi[i]);
			tNext[i] = tDelta[i] < Float.POSITIVE_INFINITY ? tDelta[i] * dist : Float.POSITIVE_INFINITY;
		}
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		do {
			// stepForward
			int axis = (tNext[0] < tNext[1]) ? ((tNext[0] < tNext[2]) ? 0 : 2) : ((tNext[1] < tNext[2]) ? 1 : 2);
			float dt = tNext[axis] - t;
			t = tNext[axis];
			ldi[axis] += step[axis];
			tNext[axis] += tDelta[axis];
			for (int i = 0; i < 3; i++) {
				tr[i] += dt * normed[i];
				tri[i] = trailEdgeToInt(tr[i], step[i]);
			}
			// checkCollision
			int stepx = step[0];
			int x0 = (axis == 0) ? ldi[0] : tri[0];
			int x1 = ldi[0] + stepx;
			int stepy = step[1];
			int y0 = (axis == 1) ? ldi[1] : tri[1];
			int y1 = ldi[1] + stepy;
			int stepz = step[2];
			int z0 = (axis == 2) ? ldi[2] : tri[2];
			int z1 = ldi[2] + stepz;
			for (int x = x0; x != x1; x += stepx) {
				for (int z = z0; z != z1; z += stepz) {
					for (int y = y0; y != y1; y += stepy) {
						BlockState block = this.level.getBlockState(pos.set(x, y, z));
						if (!block.isPathfindable(this.level, pos, PathComputationType.LAND))
							return false;
					}
					BlockPathTypes below = this.nodeEvaluator.getBlockPathType(this.level, x, y0 - 1, z, this.mob);
					if (below == BlockPathTypes.WATER || below == BlockPathTypes.LAVA || below == BlockPathTypes.OPEN)
						return false;
					BlockPathTypes in = this.nodeEvaluator.getBlockPathType(this.level, x, y0, z, this.mob);
					float priority = this.mob.getPathfindingMalus(in);
					if (priority < 0.0F || priority >= 8.0F)
						return false;
					if (in == BlockPathTypes.DAMAGE_FIRE || in == BlockPathTypes.DANGER_FIRE || in == BlockPathTypes.DAMAGE_OTHER)
						return false;
				}
			}
		} while (t <= max_t);
		return true;
	}

	static int leadEdgeToInt(float coord, int step) {
		return Mth.floor(coord - step * EPSILON);
	}

	static int trailEdgeToInt(float coord, int step) {
		return Mth.floor(coord + step * EPSILON);
	}

	static float element(Vec3 v, int i) {
		switch (i) {
		case 0:
			return (float) v.x;
		case 1:
			return (float) v.y;
		case 2:
			return (float) v.z;
		default:
			return 0.0F;
		}
	}
}
