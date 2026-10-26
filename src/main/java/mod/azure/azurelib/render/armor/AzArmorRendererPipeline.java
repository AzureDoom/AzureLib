package mod.azure.azurelib.render.armor;

import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.render.model.entity.HumanoidModel;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.*;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.client.ModelPartScaleCompat;
import mod.azure.azurelib.util.math.Matrix4f;

public class AzArmorRendererPipeline extends AzRendererPipeline<UUID, ItemStack> {

    private final AzArmorModel armorModel;

    private final AzArmorRenderer armorRenderer;

    protected Matrix4f entityRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public AzArmorRendererPipeline(AzRendererConfig<UUID, ItemStack> config, AzArmorRenderer armorRenderer) {
        super(config);
        this.armorModel = new AzArmorModel(this);
        this.armorRenderer = armorRenderer;
    }

    @Override
    protected AzRendererPipelineContext<UUID, ItemStack> createContext(
        AzRendererPipeline<UUID, ItemStack> rendererPipeline
    ) {
        return config.pipelineContext(this);
    }

    @Override
    protected AzModelRenderer<UUID, ItemStack> createModelRenderer(AzLayerRenderer<UUID, ItemStack> layerRenderer) {
        return config.modelRendererProvider(this, layerRenderer);
    }

    @Override
    protected AzLayerRenderer<UUID, ItemStack> createLayerRenderer(AzRendererConfig<UUID, ItemStack> config) {
        return new AzLayerRenderer<>(config::renderLayers);
    }

    @Override
    public void updateAnimatedTextureFrame(ItemStack animatable) {
        Entity currentEntity = context().currentEntity();

        if (currentEntity != null) {
            AnimatableTexture.setAndUpdate(config.textureLocation(currentEntity, animatable));
        }
    }

    @Override
    public void preRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
        AzArmorRendererPipelineContext armorContext = (AzArmorRendererPipelineContext) context;
        HumanoidModel baseModel = armorContext.baseModel();
        AzArmorBoneContext boneContext = armorContext.boneContext();
        AzArmorRendererConfig config = config();
        AzEquipmentSlot currentSlot = armorContext.currentSlot();
        float scaleWidth = config.scaleWidth(context.animatable());
        float scaleHeight = config.scaleHeight(context.animatable());

        ItemStack animatable = armorContext.animatable();
        AzBakedModel model = armorRenderer.provider().provideBakedModel(context().currentEntity(), animatable);
        PoseStack poseStack = armorContext.poseStack();

        if (!isReRender) {
            this.entityRenderTranslations.load(poseStack.last().pose());
        }

        armorModel.applyBaseModel(baseModel);
        boneContext.grabRelevantBones(model, config.boneProvider());
        boneContext.applyBaseTransformations(baseModel);
        scaleModelForBaby(armorContext, isReRender);
        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);
        scaleBoneWithModelPart(armorContext, boneContext, isReRender);

        ModelPart partOverride = armorContext.visiblePartOverride();
        if (partOverride != null) {
            boneContext.applyBoneVisibilityByPart(currentSlot, partOverride, baseModel);
        } else if (AzAnimatorAccessor.getOrNull(context().currentEntity()) == null) {
            boneContext.applyBoneVisibilityBySlot(currentSlot);
        }

        if (config.alpha(context.animatable()) < 1) {
            armorContext.setAlpha(config.alpha(context.animatable()));
            armorContext.setTranslucent(true);
        }
        config.preRenderEntry(context);
    }

    @Override
    public void postRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
        config.postRenderEntry(context);
    }

    /**
     * Scales the specified bone based on the model part associated with the current {@link AzEquipmentSlot}. This
     * method adjusts the scaling for various armor parts such as head, chest, legs, and feet during rendering. The
     * scaling is not performed if {@code isReRender} is set to true.
     *
     * @param context     The {@link AzArmorRendererPipelineContext} providing the rendering context, including the base
     *                    model, current slot, and other relevant information for the rendering pipeline.
     * @param boneContext The {@link AzArmorBoneContext} specifying the bones that correspond to the armor model parts.
     * @param isReRender  A boolean flag indicating if this is a re-rendering pass. When true, scaling logic is skipped
     *                    as it is generally unnecessary during re-rendering.
     */
    public void scaleBoneWithModelPart(
        AzArmorRendererPipelineContext context,
        AzArmorBoneContext boneContext,
        boolean isReRender
    ) {
        HumanoidModel baseModel = context.baseModel();
        AzEquipmentSlot currentSlot = context.currentSlot();

        if (isReRender) {
            return;
        }

        switch (currentSlot) {
            case HEAD:
                if (boneContext.head != null)
                    setBoneScale(boneContext.head, McClient.bipedHead(baseModel));
                break;
            case CHEST:
                if (boneContext.leftArm != null)
                    setBoneScale(boneContext.leftArm, McClient.bipedLeftArm(baseModel));
                if (boneContext.rightArm != null)
                    setBoneScale(boneContext.rightArm, McClient.bipedRightArm(baseModel));
                if (boneContext.body != null)
                    setBoneScale(boneContext.body, McClient.bipedBody(baseModel));
                if (boneContext.waist != null)
                    setBoneScale(boneContext.waist, McClient.bipedBody(baseModel));
                break;
            case FEET:
            case LEGS:
                if (boneContext.leftLeg != null)
                    setBoneScale(boneContext.leftLeg, McClient.bipedLeftLeg(baseModel));
                if (boneContext.rightLeg != null)
                    setBoneScale(boneContext.rightLeg, McClient.bipedRightLeg(baseModel));
                break;
            default:
                break;
        }
    }

    /**
     * Sets the scale of the specified bone based on the scaling parameters defined in the given model part.
     *
     * @param bone      The {@link AzBone} instance representing the bone to be scaled.
     * @param modelPart The {@link ModelPart} containing the scale values (xScale, yScale, zScale) that will be applied
     *                  to the bone.
     */
    private void setBoneScale(AzBone bone, ModelPart modelPart) {
        bone.setScaleX(ModelPartScaleCompat.getXScale(modelPart));
        bone.setScaleY(ModelPartScaleCompat.getYScale(modelPart));
        bone.setScaleZ(ModelPartScaleCompat.getZScale(modelPart));
    }

    public void scaleModelForBaby(AzArmorRendererPipelineContext context, boolean isReRender) {
        Entity currentEntity = context.currentEntity();
        if (
            true /* Beta has no baby mobs */ || isReRender
        ) {
            return;
        }

        HumanoidModel baseModel = context.baseModel();
        AzEquipmentSlot currentSlot = context.currentSlot();
        PoseStack poseStack = context.poseStack();

        // Same factors as 1.7.10's HumanoidModel#render for children.
        if (currentSlot == AzEquipmentSlot.HEAD) {
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.translate(0, 16 / 16f, 0);
        } else {
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(0, 24 / 16f, 0);
        }
    }

    public AzArmorModel armorModel() {
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
