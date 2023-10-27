package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.AccessWidener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public class NeoForgeAccessWidener implements AccessWidener {
    @Override
    public Map<EntityType<?>, EntityRenderer<?>> getEntityRenderers() {
        return Minecraft.getInstance().getEntityRenderDispatcher().renderers;
    }
}
