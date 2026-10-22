package mod.azure.azurelib.hitbox;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.UUID;

@ApiStatus.Internal
public final class AzHitboxClientAccess {

    private AzHitboxClientAccess() {
        throw new UnsupportedOperationException();
    }

    public static float[][] fitCache(AzHitboxState state, UUID modelUUID) {
        if (state.fitCache == null || !Objects.equals(state.fitModel, modelUUID)) {
            state.fitCache = new float[state.size()][];
            state.fitModel = modelUUID;
        }

        return state.fitCache;
    }

    public static void setBox(
        AzHitboxState state,
        int index,
        float cx,
        float cy,
        float cz,
        float hx,
        float hy,
        float hz,
        float qx,
        float qy,
        float qz,
        float qw
    ) {
        state.setBox(index, cx, cy, cz, hx, hy, hz, qx, qy, qz, qw);
    }

    public static void setInactive(AzHitboxState state, int index) {
        state.setInactive(index);
    }

    public static void commit(AzHitboxState state, Entity entity, long gameTime, float referenceYaw) {
        state.commit(entity, gameTime, referenceYaw);
    }

    public static void markRendered(AzHitboxState state, long gameTime) {
        state.lastRenderCaptureTime = gameTime;
    }

    public static boolean wasRenderedSince(AzHitboxState state, long gameTime) {
        return state.lastRenderCaptureTime != Long.MIN_VALUE && state.lastRenderCaptureTime >= gameTime;
    }

    public static float localData(AzHitboxState state, int index, int component) {
        return state.localData(index, component);
    }
}
