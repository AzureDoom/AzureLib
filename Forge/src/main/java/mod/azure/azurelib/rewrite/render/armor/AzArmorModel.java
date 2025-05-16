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
    public void renderToBuffer(
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
        IRenderTypeBuffer bufferSource = Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource();

        boolean shouldOutline = Minecraft.getInstance().levelRenderer.shouldShowEntityOutlines() && mc
            .shouldEntityAppearGlowing(
                currentEntity
            );

        if (shouldOutline) {
            bufferSource = Minecraft.getInstance().levelRenderer.renderBuffers.outlineBufferSource();
        }

        AzArmorRendererConfig config = rendererPipeline.config();
        ItemStack animatable = context.animatable();
        float partialTick = mc.getFrameTime();
        ResourceLocation textureLocation = config.textureLocation(animatable);
        RenderType renderType = context.getDefaultRenderType(animatable, textureLocation, bufferSource, partialTick);
        buffer = ItemRenderer.getArmorFoilBuffer(bufferSource, renderType, false, currentStack.hasFoil());

        AzBakedModel model = rendererPipeline.renderer().provider().provideBakedModel(animatable);
        rendererPipeline.render(poseStack, model, animatable, bufferSource, null, buffer, 0, partialTick, packedLight);
    }

    /**
     * Applies settings and transformations pre-render based on the default model
     */
    public void applyBaseModel(BipedModel<?> baseModel) {
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
