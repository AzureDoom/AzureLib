package mod.azure.azurelib.ai.pathing;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/* Credit to Bob Mowzie and pau101 for most of the code, 
 * code source for the base class can be found here: 
 * https://github.com/BobMowzie/MowziesMobs/blob/master/src/main/java/com/bobmowzie/mowziesmobs/server/ai/MMPathNavigateGround.java
 * */
public class AzureNavigation extends GroundPathNavigator {
    @Nullable
    private BlockPos pathToPosition;

	public AzureNavigation(MobEntity entity, World world) {
		super(entity, world);
	}

	@Override
	protected PathFinder createPathFinder(int maxVisitedNodes) {
		this.nodeEvaluator = new WalkNodeProcessor();
		this.nodeEvaluator.setCanPassDoors(true);
		return new AzurePathFinder(this.nodeEvaluator, maxVisitedNodes);
	}

	@Override
	protected void followThePath() {
		Path path = Objects.requireNonNull(this.path);
		Vector3d entityPos = this.getTempMobPos();
		int pathLength = path.getNodeCount();
		for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
			if (path.getNode(i).y != Math.floor(entityPos.y)) {
				pathLength = i;
				break;
			}
		}
		final Vector3d base = entityPos.add(-this.mob.getBbWidth() * 0.5F, 0.0F, -this.mob.getBbWidth() * 0.5F);
		final Vector3d max = base.add(this.mob.getBbWidth(), this.mob.getBbHeight(), this.mob.getBbWidth());
		if (this.tryShortcut(path, new Vector3d(this.mob.getX(), this.mob.getY(), this.mob.getZ()), pathLength, base, max)) {
			if (this.isAt(path, 0.5F) || this.atElevationChange(path) && this.isAt(path, this.mob.getBbWidth() * 0.5F)) {
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
                if (this.pathToPosition.closerThan(this.mob.position(), this.mob.getBbWidth()) || this.mob.getY() > (double)this.pathToPosition.getY() && new BlockPos(this.pathToPosition.getX(), this.mob.getY(), this.pathToPosition.getZ()).closerThan(this.mob.position(), this.mob.getBbWidth())) {
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
		final Vector3d pathPos = path.getNextEntityPos(this.mob);
		return MathHelper.abs((float) (this.mob.getX() - pathPos.x)) < threshold && MathHelper.abs((float) (this.mob.getZ() - pathPos.z)) < threshold && Math.abs(this.mob.getY() - pathPos.y) < 1.0D;
	}

	private boolean atElevationChange(Path path) {
		final int curr = path.getNextNodeIndex();
		final int end = Math.min(path.getNodeCount(), curr + MathHelper.ceil(this.mob.getBbWidth() * 0.5F) + 1);
		final int currY = path.getNode(curr).y;
		for (int i = curr + 1; i < end; i++) {
			if (path.getNode(i).y != currY) {
				return true;
			}
		}
		return false;
	}

	private boolean tryShortcut(Path path, Vector3d entityPos, int pathLength, Vector3d base, Vector3d max) {
		for (int i = pathLength; --i > path.getNextNodeIndex();) {
			final Vector3d vec = path.getEntityPosAtNode(this.mob, i).subtract(entityPos);
			if (this.sweep(vec, base, max)) {
				path.setNextNodeIndex(i);
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean canMoveDirectly(Vector3d start, Vector3d end, int sizeX, int sizeY, int sizeZ) {
		return true;
	}

	static final float EPSILON = 1.0E-8F;

	// Based off of https://github.com/andyhall/voxel-aabb-sweep/blob/d3ef85b19c10e4c9d2395c186f9661b052c50dc7/index.js
	private boolean sweep(Vector3d vec, Vector3d base, Vector3d max) {
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
			tDelta[i] = MathHelper.abs(max_t / value);
			float dist = dir ? (ldi[i] + 1 - lead) : (lead - ldi[i]);
			tNext[i] = tDelta[i] < Float.POSITIVE_INFINITY ? tDelta[i] * dist : Float.POSITIVE_INFINITY;
		}
		final BlockPos.Mutable pos = new BlockPos.Mutable();
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
						if (!block.isPathfindable(this.level, pos, PathType.LAND))
							return false;
					}
					PathNodeType below = this.nodeEvaluator.getBlockPathType(this.level, x, y0 - 1, z, this.mob, 1, 1, 1, true, true);
					if (below == PathNodeType.WATER || below == PathNodeType.LAVA || below == PathNodeType.OPEN)
						return false;
					PathNodeType in = this.nodeEvaluator.getBlockPathType(this.level, x, y0, z, this.mob, 1, y1 - y0, 1, true, true);
					float priority = this.mob.getPathfindingMalus(in);
					if (priority < 0.0F || priority >= 8.0F)
						return false;
					if (in == PathNodeType.DAMAGE_FIRE || in == PathNodeType.DANGER_FIRE || in == PathNodeType.DAMAGE_OTHER)
						return false;
				}
			}
		} while (t <= max_t);
		return true;
	}

	static int leadEdgeToInt(float coord, int step) {
		return MathHelper.floor(coord - step * EPSILON);
	}

	static int trailEdgeToInt(float coord, int step) {
		return MathHelper.floor(coord + step * EPSILON);
	}

	static float element(Vector3d v, int i) {
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
