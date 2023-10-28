package mod.azure.azurelib.platform.services;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

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

    RenderBuffers getRenderBuffers();

    Vec3 getLeashOffsetForMob(Entity entity);

    float getSpeedOld(LivingEntity livingEntity);

    boolean shouldShowEntityOutlines();

    boolean scaleHead(HumanoidModel<?> humanoidModel);

    float babyHeadScale(HumanoidModel<?> humanoidModel);

    float babyYHeadOffset(HumanoidModel<?> humanoidModel);

    float babyZHeadOffset(HumanoidModel<?> humanoidModel);

    float babyBodyScale(HumanoidModel<?> humanoidModel);

    float bodyYOffset(HumanoidModel<?> humanoidModel);
}
