package mod.azure.azurelib.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import mod.azure.azurelib.platform.services.AccessWidener;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.function.Function;

public class NeoForgeAccessWidener implements AccessWidener {
    private static final RenderStateShard.ShaderStateShard SHADER_STATE = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentEmissiveShader);
    private static final RenderStateShard.TransparencyStateShard TRANSPARENCY_STATE = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderStateShard.WriteMaskStateShard WRITE_MASK = new RenderStateShard.WriteMaskStateShard(true, true);
    private static final Function<ResourceLocation, RenderType> RENDER_TYPE_FUNCTION = Util.memoize(texture -> {
        RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(texture, false, false);

        return RenderType.create("geo_glowing_layer", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(SHADER_STATE).setTextureState(textureState).setTransparencyState(TRANSPARENCY_STATE).setWriteMaskState(WRITE_MASK).createCompositeState(false));
    });

    @Override
    public Map<EntityType<?>, EntityRenderer<?>> getEntityRenderers() {
        return Minecraft.getInstance().getEntityRenderDispatcher().renderers;
    }

    @Override
    public Function<ResourceLocation, RenderType> getRenderTypeFunction() {
        return RENDER_TYPE_FUNCTION;
    }

    @Override
    public RenderBuffers getRenderBuffers() {
        return Minecraft.getInstance().levelRenderer.renderBuffers;
    }

    @Override
    public Vec3 getLeashOffsetForMob(Entity entity) {
        return entity.getLeashOffset();
    }

    @Override
    public float getSpeedOld(LivingEntity livingEntity) {
        return livingEntity.walkAnimation.speedOld;
    }

    @Override
    public boolean shouldShowEntityOutlines() {
        return Minecraft.getInstance().levelRenderer.shouldShowEntityOutlines();
    }

    @Override
    public boolean scaleHead(HumanoidModel<?> humanoidModel) {
        return humanoidModel.scaleHead;
    }

    @Override
    public float babyHeadScale(HumanoidModel<?> humanoidModel) {
        return humanoidModel.babyHeadScale;
    }

    @Override
    public float babyYHeadOffset(HumanoidModel<?> humanoidModel) {
        return humanoidModel.babyYHeadOffset;
    }

    @Override
    public float babyZHeadOffset(HumanoidModel<?> humanoidModel) {
        return humanoidModel.babyZHeadOffset;
    }

    @Override
    public float babyBodyScale(HumanoidModel<?> humanoidModel) {
        return humanoidModel.babyBodyScale;
    }

    @Override
    public float bodyYOffset(HumanoidModel<?> humanoidModel) {
        return humanoidModel.bodyYOffset;
    }
}
