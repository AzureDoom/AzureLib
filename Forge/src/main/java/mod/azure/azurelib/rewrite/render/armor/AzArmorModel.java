package mod.azure.azurelib.rewrite.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.render.armor.bone.AzArmorBoneContext;

public class AzArmorModel<E extends LivingEntity> extends BipedModel<E> {

    private final AzArmorRendererPipeline rendererPipeline;

    public AzArmorModel(AzArmorRendererPipeline rendererPipeline) {
        super(0.5F);
        this.rendererPipeline = rendererPipeline;
    }

    @Override
    public void render(
        MatrixStack poseStack,
        IVertexBuilder buffer,
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
        IRenderTypeBuffer bufferSource = Minecraft.getInstance().worldRenderer.renderTypeTextures.getBufferSource();

        boolean shouldOutline = Minecraft.getInstance().worldRenderer.isRenderEntityOutlines() && mc
            .getRenderViewEntity()
            .isGlowing();

        if (shouldOutline) {
            bufferSource = Minecraft.getInstance().worldRenderer.renderTypeTextures.getOutlineBufferSource();
        }

        AzArmorRendererConfig config = rendererPipeline.config();
        ItemStack animatable = context.animatable();
        float partialTick = mc.getRenderPartialTicks();
        ResourceLocation textureLocation = config.textureLocation(animatable);
        RenderType renderType = context.getDefaultRenderType(animatable, textureLocation, bufferSource, partialTick);
        buffer = ItemRenderer.getBuffer(bufferSource, renderType, false, currentStack.hasEffect());

        AzBakedModel model = rendererPipeline.renderer().provider().provideBakedModel(animatable);
        rendererPipeline.render(poseStack, model, animatable, bufferSource, null, buffer, 0, partialTick, packedLight);
    }

    /**
     * Applies settings and transformations pre-render based on the default model
     */
    public void applyBaseModel(BipedModel<?> baseModel) {
        this.isChild = baseModel.isChild;
        this.isSneak = baseModel.isSneak;
        this.isSitting = baseModel.isSitting;
        this.rightArmPose = baseModel.rightArmPose;
        this.leftArmPose = baseModel.leftArmPose;
    }

    @Override
    public void setVisible(boolean pVisible) {
        super.setVisible(pVisible);
        AzArmorBoneContext boneContext = rendererPipeline.context().boneContext();
        boneContext.setAllVisible(pVisible);
    }
}
