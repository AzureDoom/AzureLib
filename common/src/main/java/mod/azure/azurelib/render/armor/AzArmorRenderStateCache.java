package mod.azure.azurelib.render.armor;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Bridges vanilla's 26.2 entity extraction phase to the later armor submit phase.
 * <p>
 * Vanilla armor rendering only receives an {@link EntityRenderState} during submit, while AzureLib's existing armor
 * configuration API still permits entity-aware model/texture callbacks. The render state is weakly keyed so this does
 * not retain per-frame render states after vanilla is finished with them.
 * </p>
 */
public final class AzArmorRenderStateCache {

    private static final Map<EntityRenderState, Entity> ENTITY_BY_RENDER_STATE = new WeakHashMap<>();

    private AzArmorRenderStateCache() {}

    public static synchronized void capture(EntityRenderState renderState, Entity entity) {
        ENTITY_BY_RENDER_STATE.put(renderState, entity);
    }

    public static synchronized @Nullable Entity get(EntityRenderState renderState) {
        return ENTITY_BY_RENDER_STATE.get(renderState);
    }
}
