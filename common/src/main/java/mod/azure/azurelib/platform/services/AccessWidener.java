package mod.azure.azurelib.platform.services;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.function.Function;

public interface AccessWidener {

    /**
     * @return all entity renderers for Minecraft.
     *
     * @deprecated Minecraft.class exposes a getter for a single entity renderer by entity type. This is here for parity reasons.
     */
    @Deprecated
    Map<EntityType<?>, EntityRenderer<?>> getEntityRenderers();

    Function<ResourceLocation, RenderType> getRenderTypeFunction();
}
