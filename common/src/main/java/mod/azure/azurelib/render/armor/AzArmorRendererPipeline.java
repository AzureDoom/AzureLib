package mod.azure.azurelib.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.*;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;

public class AzArmorRendererPipeline extends AzRendererPipeline<UUID, ItemStack> {

    private final AzArmorModel<?> armorModel;

    private final AzArmorRenderer armorRenderer;

    protected Matrix4f entityRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public AzArmorRendererPipeline(AzRendererConfig<UUID, ItemStack> config, AzArmorRenderer armorRenderer) {
        super(config);
        this.armorModel = new AzArmorModel<>(this);
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
        var currentEntity = context().currentEntity();

        if (currentEntity != null) {
            AnimatableTexture.setAndUpdate(config.textureLocation(currentEntity, animatable));
        }
    }

    @Override
    public void preRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
        var armorContext = (AzArmorRendererPipelineContext) context;
        var baseModel = armorContext.baseModel();
        var boneContext = armorContext.boneContext();
        var config = config();
        var currentSlot = armorContext.currentSlot();
        var scaleWidth = config.scaleWidth(context.animatable());
        var scaleHeight = config.scaleHeight(context.animatable());

        var animatable = armorContext.animatable();
        var model = armorRenderer.provider().provideBakedModel(context().currentEntity(), animatable);
        var poseStack = armorContext.poseStack();

        this.entityRenderTranslations = new Matrix4f(poseStack.last().pose());

        armorModel.applyBaseModel(baseModel);
        boneContext.grabRelevantBones(model, config.boneProvider());
        boneContext.applyBaseTransformations(baseModel);
        scaleModelForBaby(armorContext, isReRender);
        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);
        scaleBoneWithModelPart(armorContext, boneContext, isReRender);

        if (AzAnimatorAccessor.getOrNull(context().currentEntity()) == null)
            boneContext.applyBoneVisibilityBySlot(currentSlot);

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
     * Scales the specified bone based on the model part associated with the current {@link EquipmentSlot}. This method
     * adjusts the scaling for various armor parts such as head, chest, legs, and feet during rendering. The scaling is
     * not performed if {@code isReRender} is set to true.
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
        HumanoidModel<?> baseModel = context.baseModel();
        EquipmentSlot currentSlot = context.currentSlot();

        if (isReRender) {
            return;
        }

        switch (currentSlot) {
            case HEAD -> {
                if (boneContext.head != null)
                    setBoneScale(boneContext.head, baseModel.head);
            }
            case CHEST -> {
                if (boneContext.head != null)
                    setBoneScale(boneContext.leftArm, baseModel.leftArm);
                if (boneContext.rightArm != null)
                    setBoneScale(boneContext.rightArm, baseModel.rightArm);
                if (boneContext.body != null)
                    setBoneScale(boneContext.body, baseModel.body);
                if (boneContext.waist != null)
                    setBoneScale(boneContext.waist, baseModel.body);
            }
            case FEET, LEGS -> {
                if (boneContext.leftLeg != null)
                    setBoneScale(boneContext.leftLeg, baseModel.leftLeg);
                if (boneContext.rightLeg != null)
                    setBoneScale(boneContext.rightLeg, baseModel.rightLeg);
            }
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
        bone.setScaleX(modelPart.xScale);
        bone.setScaleY(modelPart.yScale);
        bone.setScaleZ(modelPart.zScale);
    }

    /**
     * Apply custom scaling to account for {@link net.minecraft.client.model.AgeableListModel AgeableListModel} baby
     * models
     */
    public void scaleModelForBaby(AzArmorRendererPipelineContext context, boolean isReRender) {
        var currentEntity = context.currentEntity();
        if (!(currentEntity instanceof LivingEntity ageableMob && ageableMob.isBaby()) || isReRender) {
            return;
        }

        var baseModel = context.baseModel();
        var currentSlot = context.currentSlot();
        var poseStack = context.poseStack();

        if (currentSlot == EquipmentSlot.HEAD) {
            if (baseModel.scaleHead) {
                float headScale = 1.5f / baseModel.babyHeadScale;

                poseStack.scale(headScale, headScale, headScale);
            }

            poseStack.translate(0, baseModel.babyYHeadOffset / 16f, baseModel.babyZHeadOffset / 16f);
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
