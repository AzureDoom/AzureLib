package mod.azure.azurelib.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererConfig;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;

public class AzArmorRendererPipeline extends AzRendererPipeline<UUID, ItemStack> {

    private final AzArmorRenderer armorRenderer;

    protected Matrix4f entityRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public AzArmorRendererPipeline(AzRendererConfig<UUID, ItemStack> config, AzArmorRenderer armorRenderer) {
        super(config);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void preRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
        var armorContext = (AzArmorRendererPipelineContext) context;
        var baseModel = armorContext.baseModel();
        var renderState = armorContext.renderState();
        var boneContext = armorContext.boneContext();
        var currentSlot = armorContext.currentSlot();
        var config = config();
        var scaleWidth = config.scaleWidth(context.animatable());
        var scaleHeight = config.scaleHeight(context.animatable());
        var currentEntity = armorContext.currentEntity();
        var animatable = armorContext.animatable();
        var model = armorRenderer.provider().provideBakedModel(currentEntity, animatable);
        var poseStack = armorContext.poseStack();

        this.entityRenderTranslations = new Matrix4f(poseStack.last().pose());

        if (armorContext.setupBaseModel()) {
            ((HumanoidModel) baseModel).setupAnim(renderState);
        }

        boneContext.grabRelevantBones(model, config.boneProvider());
        boneContext.applyBaseTransformations(baseModel);

        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);
        scaleBoneWithModelPart(armorContext, boneContext, isReRender);

        var modelPartOverride = armorContext.modelPartOverride();

        if (modelPartOverride != null) {
            boneContext.applyBoneVisibilityByPart(
                currentSlot,
                modelPartOverride,
                baseModel
            );
        } else if (
            currentEntity == null ||
                AzAnimatorAccessor.getOrNull(currentEntity) == null
        ) {
            boneContext.applyBoneVisibilityBySlot(currentSlot);
        }

        var alphaValue = config.alpha(context.animatable());
        if (alphaValue < 1.0F) {
            var alpha = (int) (alphaValue * 0xFF) << 24;
            var color = (armorContext.renderColor() & 0xFFFFFF) | alpha;
            armorContext.setRenderColor(color);
            armorContext.setTranslucent(true);
        }

        config.preRenderEntry(context);
    }

    @Override
    public void postRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
        config.postRenderEntry(context);
        context.setTextureOverride(null);
    }

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
                if (boneContext.head != null) {
                    setBoneScale(boneContext.head, baseModel.head);
                }
            }
            case CHEST -> {
                if (boneContext.leftArm != null) {
                    setBoneScale(boneContext.leftArm, baseModel.leftArm);
                }
                if (boneContext.rightArm != null) {
                    setBoneScale(boneContext.rightArm, baseModel.rightArm);
                }
                if (boneContext.body != null) {
                    setBoneScale(boneContext.body, baseModel.body);
                }
                if (boneContext.waist != null) {
                    setBoneScale(boneContext.waist, baseModel.body);
                }
            }
            case FEET, LEGS -> {
                if (boneContext.leftLeg != null) {
                    setBoneScale(boneContext.leftLeg, baseModel.leftLeg);
                }
                if (boneContext.rightLeg != null) {
                    setBoneScale(boneContext.rightLeg, baseModel.rightLeg);
                }
            }
            default -> {}
        }
    }

    private void setBoneScale(AzBone bone, ModelPart modelPart) {
        bone.setScaleX(modelPart.xScale);
        bone.setScaleY(modelPart.yScale);
        bone.setScaleZ(modelPart.zScale);
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
