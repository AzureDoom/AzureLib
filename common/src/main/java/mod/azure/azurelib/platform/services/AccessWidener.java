package mod.azure.azurelib.platform.services;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public interface AccessWidener {

    /**
     * @return all entity renderers for Minecraft.
     *
     * @deprecated Minecraft.class exposes a getter for a single entity renderer by entity type. This is here for parity reasons.
     */
    @Deprecated
    public Map<EntityType<?>, EntityRenderer<?>> getEntityRenderers();
}
