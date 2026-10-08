package mod.azure.azurelib.render.lod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.profiling.AzProfileStage;
import mod.azure.azurelib.profiling.AzProfiler;

/**
 * Applies {@link AzLodConfig} rules to an entity each render frame.
 * <p>
 * Call {@link #update} once per render call (before the model renderer runs). It will:
 * </p>
 * <ol>
 * <li>Compute camera distance squared (cheap, no sqrt).</li>
 * <li>Apply bone visibility based on hierarchy depth vs {@link AzLodConfig#boneLodDepth()}.</li>
 * <li>Return whether the animation system should run this frame based on {@link AzLodConfig#animLodTickInterval()}. The
 * caller must actually skip the animator when this returns {@code false}; see
 * {@code AzEntityRenderer#shouldAnimateThisFrame()}.</li>
 * </ol>
 * <p>
 * Bones that are shown/hidden by LOD will be restored to their natural hidden state on the next model reload. The LOD
 * manager only ever <em>adds</em> hidden flags — it never forces a hidden bone visible.
 * </p>
 */
public final class AzLodManager {

    private final AzLodConfig config;

    private static final Set<AzBakedModel> MODELS_WITH_LOD_HIDDEN_BONES = Collections.newSetFromMap(
        new WeakHashMap<>()
    );

    /**
     * Tracks the entity tick at which we last ran a full animation update for this entity. Used to implement tick-rate
     * reduction for animation LOD. Starts far in the past so the first frame always animates.
     */
    private int lastAnimTick = Integer.MIN_VALUE / 2;

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

        AzProfiler.begin(AzProfileStage.LOD_UPDATE, entity);
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        var camPos = camera.getPosition();
        var distSq = entity.distanceToSqr(camPos.x, camPos.y, camPos.z);

        applyBoneLod(bakedModel, distSq);
        var animate = shouldAnimate(entity, distSq);
        AzProfiler.end(AzProfileStage.LOD_UPDATE);
        return animate;
    }

    /**
     * Walks the bone tree and hides bones whose depth exceeds the LOD threshold. Bones that are already hidden (by the
     * model or a previous system) are left alone.
     */
    private void applyBoneLod(AzBakedModel bakedModel, double distSq) {
        if (distSq <= config.boneLodDistanceSq()) {
            if (!MODELS_WITH_LOD_HIDDEN_BONES.remove(bakedModel)) {
                return;
            }

            for (var bone : bakedModel.getBonesByName().values()) {
                if (bone.getLodHidden()) {
                    bone.setHidden(false);
                    bone.setLodHidden(false);
                }
            }
            return;
        }

        var maxDepth = config.boneLodDepth();

        MODELS_WITH_LOD_HIDDEN_BONES.add(bakedModel);

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
     * Returns true if the animation system should run this frame.
     * <p>
     * Within {@code animLodDistance}, every frame animates. Past it, exactly one frame animates once at least
     * {@code animLodTickInterval} entity ticks have passed since the last update; every other frame reuses the last
     * pose. Counting elapsed ticks (rather than {@code tickCount % interval}) means an entity can't miss its slot when
     * the frame rate is below the tick rate, and update work is naturally spread across entities.
     * </p>
     */
    private boolean shouldAnimate(Entity entity, double distSq) {
        var currentTick = entity.tickCount;

        if (distSq <= config.animLodDistanceSq()) {
            lastAnimTick = currentTick;
            return true;
        }

        // currentTick < lastAnimTick: tickCount went backwards (entity reset); resync rather than freezing.
        if (currentTick - lastAnimTick >= config.animLodTickInterval() || currentTick < lastAnimTick) {
            lastAnimTick = currentTick;
            return true;
        }

        return false;
    }
}
