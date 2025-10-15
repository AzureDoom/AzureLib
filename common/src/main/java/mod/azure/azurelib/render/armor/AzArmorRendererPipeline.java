package mod.azure.azurelib.render.armor;

import com.mojang.math.Matrix4f;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.render.*;

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
    protected void updateAnimatedTextureFrame(ItemStack animatable) {
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
     * Apply custom scaling to account for {@link net.minecraft.client.model.AgeableListModel AgeableListModel} baby
     * models
     */
    public void scaleModelForBaby(AzArmorRendererPipelineContext context, boolean isReRender) {
        var currentEntity = context.currentEntity();
        if (!(currentEntity instanceof AgeableMob ageableMob && ageableMob.isBaby()) || isReRender) {
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
