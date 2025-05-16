package mod.azure.azurelib.rewrite.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;

import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.render.*;
import mod.azure.azurelib.rewrite.render.armor.bone.AzArmorBoneContext;

public class AzArmorRendererPipeline extends AzRendererPipeline<ItemStack> {

    private final AzArmorModel<?> armorModel;

    private final AzArmorRenderer armorRenderer;

    protected Matrix4f entityRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public AzArmorRendererPipeline(AzRendererConfig<ItemStack> config, AzArmorRenderer armorRenderer) {
        super(config);
        this.armorModel = new AzArmorModel<>(this);
        this.armorRenderer = armorRenderer;
    }

    @Override
    protected AzRendererPipelineContext<ItemStack> createContext(AzRendererPipeline<ItemStack> rendererPipeline) {
        return new AzArmorRendererPipelineContext(rendererPipeline);
    }

    @Override
    protected AzModelRenderer<ItemStack> createModelRenderer(AzLayerRenderer<ItemStack> layerRenderer) {
        return new AzArmorModelRenderer(this, layerRenderer);
    }

    @Override
    protected AzLayerRenderer<ItemStack> createLayerRenderer(AzRendererConfig<ItemStack> config) {
        return new AzLayerRenderer<>(config::renderLayers);
    }

    @Override
    protected void updateAnimatedTextureFrame(ItemStack animatable) {
        Entity currentEntity = context().currentEntity();

        if (currentEntity != null) {
            AnimatableTexture.setAndUpdate(config.textureLocation(animatable));
        }
    }

    @Override
    public void preRender(AzRendererPipelineContext<ItemStack> context, boolean isReRender) {
        AzArmorRendererPipelineContext armorContext = (AzArmorRendererPipelineContext) context;
        BipedModel<?> baseModel = armorContext.baseModel();
        AzArmorBoneContext boneContext = armorContext.boneContext();
        AzArmorRendererConfig config = config();
        EquipmentSlotType currentSlot = armorContext.currentSlot();
        float scaleWidth = config.scaleWidth(context.animatable());
        float scaleHeight = config.scaleHeight(context.animatable());

        ItemStack animatable = armorContext.animatable();
        AzBakedModel model = armorRenderer.provider().provideBakedModel(animatable);
        MatrixStack poseStack = armorContext.poseStack();

        this.entityRenderTranslations = new Matrix4f(poseStack.last().pose());

        armorModel.applyBaseModel(baseModel);
        boneContext.grabRelevantBones(model, config.boneProvider());
        boneContext.applyBaseTransformations(baseModel);
        scaleModelForBaby(armorContext, isReRender);
        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);

        boneContext.applyBoneVisibilityBySlot(currentSlot);
        if (config.alpha(context.animatable()) < 1) {
            armorContext.setAlpha(config.alpha(context.animatable()));
            armorContext.setTranslucent(true);
        }
        config.preRenderEntry(context);
    }

    @Override
    public void postRender(AzRendererPipelineContext<ItemStack> context, boolean isReRender) {
        config.postRenderEntry(context);
    }

    /**
     * Apply custom scaling to account for {@link AgeableModel AgeableListModel} baby models
     */
    public void scaleModelForBaby(AzArmorRendererPipelineContext context, boolean isReRender) {
        if (!armorModel.young || isReRender) {
            return;
        }

        BipedModel<?> baseModel = context.baseModel();
        EquipmentSlotType currentSlot = context.currentSlot();
        MatrixStack poseStack = context.poseStack();

        if (currentSlot == EquipmentSlotType.HEAD) {
            if (baseModel.scaleHead) {
                float headScale = 1.5f / baseModel.babyHeadScale;

                poseStack.scale(headScale, headScale, headScale);
            }

            poseStack.translate(0, baseModel.yHeadOffset / 16f, baseModel.zHeadOffset / 16f);
        } else {
            float bodyScale = 1 / baseModel.babyBodyScale;

            poseStack.scale(bodyScale, bodyScale, bodyScale);
            poseStack.translate(0, baseModel.bodyYOffset / 16f, 0);
        }
    }

    public AzArmorModel<?> armorModel() {
        return armorModel;
    }

    @Override
    public AzArmorRendererConfig config() {
        return (AzArmorRendererConfig) super.config();
    }

    @Override
    public AzArmorRendererPipelineContext context() {
        return (AzArmorRendererPipelineContext) super.context();
    }

    public AzArmorRenderer renderer() {
        return armorRenderer;
    }
}
