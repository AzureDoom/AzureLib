package mod.azure.azurelib.render.lod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;

/**
 * Applies {@link AzLodConfig} rules to an entity each render frame.
 * <p>
 * Call {@link #update} once per render call (before the model renderer runs). It will:
 * </p>
 * <ol>
 * <li>Compute camera distance squared (cheap, no sqrt).</li>
 * <li>Apply bone visibility based on hierarchy depth vs {@link AzLodConfig#boneLodDepth()}.</li>
 * <li>Return whether the animation system should run this frame based on
 * {@link AzLodConfig#animLodTickInterval()}.</li>
 * </ol>
 * <p>
 * Bones that are shown/hidden by LOD will be restored to their natural hidden state on the next model reload. The LOD
 * manager only ever <em>adds</em> hidden flags — it never forces a hidden bone visible.
 * </p>
 */
public final class AzLodManager {

    private final AzLodConfig config;

    /**
     * Tracks the game tick at which we last ran a full animation update for this entity. Used to implement tick-rate
     * reduction for animation LOD.
     */
    private int lastAnimTick = -1;

    public AzLodManager(AzLodConfig config) {
        this.config = config;
    }

    /**
     * Called once per render frame before the model renderer runs.
     *
     * @param entity     The entity being rendered
     * @param bakedModel The model whose bones will be LOD-culled
     * @return {@code true} if the animation system should run this frame, {@code false} if animation LOD says to skip
     *         this frame
     */
    public boolean update(Entity entity, AzBakedModel bakedModel) {
        if (config == AzLodConfig.DISABLED) {
            return true;
        }

        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        var camPos = camera.getPosition();
        var distSq = entity.distanceToSqr(camPos.x, camPos.y, camPos.z);

        applyBoneLod(bakedModel, distSq);
        return shouldAnimate(entity, distSq);
    }

    /**
     * Walks the bone tree and hides bones whose depth exceeds the LOD threshold. Bones that are already hidden (by the
     * model or a previous system) are left alone.
     */
    private void applyBoneLod(AzBakedModel bakedModel, double distSq) {
        if (distSq <= config.boneLodDistanceSq()) {
            for (var bone : bakedModel.getBonesByName().values()) {
                if (bone.getLodHidden()) {
                    bone.setHidden(false);
                    bone.setLodHidden(false);
                }
            }
            return;
        }

        var maxDepth = config.boneLodDepth();

        for (var rootBone : bakedModel.getTopLevelBones()) {
            applyBoneLodRecursive(rootBone, 0, maxDepth);
        }
    }

    private void applyBoneLodRecursive(AzBone bone, int depth, int maxDepth) {
        if (bone.isHidden() && !bone.getLodHidden()) {
            return;
        }

        if (depth > maxDepth) {
            if (!bone.isHidden()) {
                bone.setHidden(true);
                bone.setLodHidden(true);
            }
            return;
        }

        for (var child : bone.getChildBones()) {
            applyBoneLodRecursive(child, depth + 1, maxDepth);
        }
    }

    /**
     * Returns true if the animation system should run this frame. Past animLodDistance, animations only advance every N
     * ticks.
     */
    private boolean shouldAnimate(Entity entity, double distSq) {
        if (distSq <= config.animLodDistanceSq()) {
            return true;
        }

        var currentTick = entity.tickCount;
        var interval = config.animLodTickInterval();

        if (currentTick != lastAnimTick && (currentTick % interval) == 0) {
            lastAnimTick = currentTick;
            return true;
        }

        return lastAnimTick == currentTick;
    }
}
