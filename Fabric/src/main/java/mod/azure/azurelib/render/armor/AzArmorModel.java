package mod.azure.azurelib.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;

public class AzArmorModel<E extends LivingEntity> extends HumanoidModel<E> {

    private final AzArmorRendererPipeline rendererPipeline;

    public AzArmorModel(AzArmorRendererPipeline rendererPipeline) {
        super(0.5F);
        this.rendererPipeline = rendererPipeline;
    }

    @Override
    public void renderToBuffer(
        @NotNull PoseStack poseStack,
        @Nullable VertexConsumer buffer,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        Minecraft mc = Minecraft.getInstance();
        AzArmorRendererPipelineContext context = rendererPipeline.context();
        Entity currentEntity = context.currentEntity();
        ItemStack currentStack = context.currentStack();
        MultiBufferSource bufferSource = Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource();

        boolean shouldOutline = Minecraft.getInstance().levelRenderer.shouldShowEntityOutlines()
            && mc.crosshairPickEntity.isGlowing();

        if (shouldOutline) {
            bufferSource = Minecraft.getInstance().levelRenderer.renderBuffers.outlineBufferSource();
        }

        AzArmorRendererConfig config = rendererPipeline.config();
        ItemStack animatable = context.animatable();
        float partialTick = mc.getFrameTime();
        ResourceLocation textureLocation = config.textureLocation(animatable);
        RenderType renderType = context.getDefaultRenderType(animatable, textureLocation, bufferSource, partialTick);
        buffer = ItemRenderer.getFoilBuffer(bufferSource, renderType, false, currentStack.hasFoil());

        AzBakedModel model = rendererPipeline.renderer().provider().provideBakedModel(animatable);
        rendererPipeline.render(poseStack, model, animatable, bufferSource, null, buffer, 0, partialTick, packedLight);
    }

    /**
     * Applies settings and transformations pre-render based on the default model
     */
    public void applyBaseModel(HumanoidModel<?> baseModel) {
        this.young = baseModel.young;
        this.crouching = baseModel.crouching;
        this.riding = baseModel.riding;
        this.rightArmPose = baseModel.rightArmPose;
        this.leftArmPose = baseModel.leftArmPose;
    }

    @Override
    public void setAllVisible(boolean pVisible) {
        super.setAllVisible(pVisible);
        AzArmorBoneContext boneContext = rendererPipeline.context().boneContext();
        boneContext.setAllVisible(pVisible);
    }
}
