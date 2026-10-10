package mod.azure.azurelib.render.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.OverlayTexture;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.render.vertex.VertexConsumer;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * The {@link ModelBiped} handed to vanilla's armor layer for AzureLib armor.
 * <p>
 * 1.12.2's {@code LayerBipedArmor} obtains the model through {@code ForgeHooksClient#getArmorModel} (where AzureLib's
 * mixin supplies this model and records the stack and slot via {@link #prepare}), copies the body pose onto it and
 * calls {@link #render}. That call poses the vanilla parts, hands the result to AzureLib's armor pipeline, and draws
 * the AzureLib model in their place.
 */
public class AzArmorModel extends ModelBiped {

    private final AzArmorRendererPipeline rendererPipeline;

    @Nullable
    private AzArmorRenderer pendingRenderer;

    @Nullable
    private ItemStack pendingStack;

    @Nullable
    private EntityEquipmentSlot pendingSlot;

    public AzArmorModel(AzArmorRendererPipeline rendererPipeline) {
        super(1.0F);
        this.rendererPipeline = rendererPipeline;
        this.isChild = false;
    }

    /**
     * Records which stack and slot the next vanilla {@link #render} call is for.
     */
    public void prepare(AzArmorRenderer renderer, ItemStack stack, EntityEquipmentSlot slot) {
        this.pendingRenderer = renderer;
        this.pendingStack = stack;
        this.pendingSlot = slot;
    }

    @Override
    public void render(
        @Nonnull Entity entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scale
    ) {
        // LayerArmorBase renders the model a second time for its enchantment glint, with the depth function set to
        // GL_EQUAL. AzureLib draws its own glint, so that pass is skipped.
        if (GL11.glGetInteger(GL11.GL_DEPTH_FUNC) == GL11.GL_EQUAL) {
            return;
        }

        if (pendingRenderer == null || pendingStack == null || pendingSlot == null) {
            return;
        }

        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        pendingRenderer.prepForRender(entity, pendingStack, pendingSlot, this);

        AzBufferSource bufferSource = AzBufferSource.getInstance();
        bufferSource.endBatch();
        renderToBuffer(
            new PoseStack(),
            null,
            RenderUtils.currentPackedLight(),
            OverlayTexture.NO_OVERLAY,
            1,
            1,
            1,
            1
        );
        bufferSource.endBatch();
    }

    /**
     * Renders the armor into AzureLib's buffer source using the given pose. Used both from {@link #render} and from
     * {@link mod.azure.azurelib.render.layer.AzArmorLayer}.
     */
    public void renderToBuffer(
        @Nonnull PoseStack poseStack,
        @Nullable VertexConsumer buffer,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        Minecraft mc = Minecraft.getMinecraft();
        AzArmorRendererPipelineContext context = rendererPipeline.context();
        Entity currentEntity = context.currentEntity();

        if (currentEntity == null) {
            return;
        }

        ItemStack currentStack = context.currentStack();
        AzBufferSource bufferSource = AzBufferSource.getInstance();
        AzArmorRendererConfig config = rendererPipeline.config();
        ItemStack animatable = context.animatable();
        float partialTick = mc.getRenderPartialTicks();
        RenderType renderType = context.getDefaultRenderType(
            animatable,
            config.textureLocation(currentEntity, animatable),
            bufferSource,
            partialTick,
            config.getRenderType(currentEntity, animatable),
            config.alpha(animatable)
        );

        buffer = AzBufferSource.getArmorFoilBuffer(bufferSource, renderType, currentStack.hasEffect());
        AzBakedModel model = rendererPipeline.renderer().provider().provideBakedModel(currentEntity, animatable);
        rendererPipeline.render(poseStack, model, animatable, bufferSource, null, buffer, 0, partialTick, packedLight);
    }

    /**
     * Applies the base model's properties to this instance for use in rendering
     *
     * @param baseModel The base model
     */
    public void applyBaseModel(ModelBiped baseModel) {
        this.isChild = baseModel.isChild;
        this.isSneak = baseModel.isSneak;
        this.isRiding = baseModel.isRiding;
        this.rightArmPose = baseModel.rightArmPose;
        this.leftArmPose = baseModel.leftArmPose;
    }

    @Override
    public void setModelAttributes(@Nonnull ModelBase model) {
        super.setModelAttributes(model);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        rendererPipeline.context().boneContext().setAllVisible(visible);
    }
}
