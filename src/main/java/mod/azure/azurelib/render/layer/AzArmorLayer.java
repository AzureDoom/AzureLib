package mod.azure.azurelib.render.layer;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.AzArmorModel;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererPipeline;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.LightTexture;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.OverlayTexture;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.ResourceLocation;
import mod.azure.azurelib.util.client.RenderUtils;

public class AzArmorLayer<T extends LivingEntity> implements AzRenderLayer<UUID, T> {

    protected static final Map<String, ResourceLocation> ARMOR_PATH_CACHE = new HashMap<>();

    protected static final BipedEntityModel INNER_ARMOR_MODEL = new BipedEntityModel(0.5F);

    protected static final BipedEntityModel OUTER_ARMOR_MODEL = new BipedEntityModel(1.0F);

    private final Map<BipedEntityModel, float[][]> savedPoses = new IdentityHashMap<>();

    private final Map<Entity, Map<BipedEntityModel, float[][]>> entityPoses = new WeakHashMap<>();

    @Nullable
    private BipedEntityModel currentArmorModel;

    @Nullable
    protected ItemStack mainHandStack;

    @Nullable
    protected ItemStack offhandStack;

    @Nullable
    protected ItemStack helmetStack;

    @Nullable
    protected ItemStack chestplateStack;

    @Nullable
    protected ItemStack leggingsStack;

    @Nullable
    protected ItemStack bootsStack;

    @Override
    public void preRender(AzRendererPipelineContext<UUID, T> context) {
        T animatable = context.animatable();
        this.mainHandStack = AzEquipmentSlot.MAINHAND.getStack(animatable);
        this.offhandStack = AzEquipmentSlot.OFFHAND.getStack(animatable);
        this.helmetStack = AzEquipmentSlot.HEAD.getStack(animatable);
        this.chestplateStack = AzEquipmentSlot.CHEST.getStack(animatable);
        this.leggingsStack = AzEquipmentSlot.LEGS.getStack(animatable);
        this.bootsStack = AzEquipmentSlot.FEET.getStack(animatable);
    }

    @Override
    public void render(AzRendererPipelineContext<UUID, T> context) {
        restorePoses();
    }

    /**
     * Renders an individual armor piece base on the given bone and its parent pipeline context
     */
    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone) {
        ItemStack armorStack = getArmorItemForBone(context, bone);

        if (armorStack == null) {
            return;
        }

        context.poseStack().pushPose();

        renderArmor(context, bone, armorStack);

        context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));
        context.poseStack().popPose();
    }

    public void renderArmor(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ItemStack armorStack
    ) {
        AzEquipmentSlot slot = getEquipmentSlotForBone(context, bone, armorStack);
        AzArmorRenderer renderer = AzArmorRendererRegistryHolder.get(armorStack);
        BipedEntityModel model = getModelForItem(armorStack, slot);
        this.currentArmorModel = model;
        ModelPart modelPart = getModelPartForBone(context, bone, model);

        context.poseStack().pushPose();
        context.poseStack().scale(-1, -1, 1);

        savePoseOnce(model);

        // Give every biped part this entity's pose, not whatever another entity left behind.
        float[][] entityPose = entityPoses
            .computeIfAbsent(context.animatable(), e -> new IdentityHashMap<>())
            .computeIfAbsent(model, AzArmorLayer::capture);
        apply(model, entityPose);

        int idx = partIndex(model, modelPart);

        if (renderer != null) {
            prepModelPartForRender(context, bone, modelPart);
            if (idx >= 0)
                entityPose[idx] = capturePart(modelPart);
            renderAzArmorPiece(context, slot, armorStack, renderer, context.animatable(), model, modelPart);
        } else if (Mc.armorType(Mc.item(armorStack)) >= 0) {
            prepModelPartForRender(context, bone, modelPart);
            if (idx >= 0)
                entityPose[idx] = capturePart(modelPart);
            renderVanillaArmorPiece(context, bone, slot, armorStack, modelPart);
        }

        context.poseStack().popPose();
    }

    /**
     * Return an EquipmentSlot for a given {@link ItemStack} and animatable instance.<br>
     * This is what determines the base model to use for rendering a particular stack
     */
    protected @Nonnull AzEquipmentSlot getEquipmentSlotForBone(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ItemStack stack
    ) {
        T animatable = context.animatable();

        for (AzEquipmentSlot slot : AzEquipmentSlot.values()) {
            if (
                slot.getSlotType() == AzEquipmentSlot.Type.ARMOR && stack == slot.getStack(animatable)
            ) {
                return slot;
            }
        }

        return AzEquipmentSlot.CHEST;
    }

    /**
     * Return a ModelPart for a given {@link AzBone}.<br>
     * This is then transformed into position for the final render
     */
    @Nonnull
    protected ModelPart getModelPartForBone(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        BipedEntityModel baseModel
    ) {
        return baseModel.body;
    }

    /**
     * Get the {@link ItemStack} relevant to the bone being rendered.<br>
     * Return null if this bone should be ignored
     */
    @Nullable
    protected ItemStack getArmorItemForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone) {
        return null;
    }

    /**
     * Renders an individual armor piece base on the given bone and its parent pipeline context
     */
    protected void renderAzArmorPiece(
        AzRendererPipelineContext<UUID, T> context,
        AzEquipmentSlot slot,
        ItemStack armorStack,
        AzArmorRenderer renderer,
        LivingEntity entity,
        BipedEntityModel model,
        ModelPart modelPart
    ) {
        AzArmorRendererPipeline renderPipelines = renderer.rendererPipeline();
        AzArmorBoneContext boneContext = renderPipelines.context().boneContext();
        AzArmorModel armorModel = renderPipelines.armorModel();

        renderer.prepForRender(entity, armorStack, slot, model);
        boneContext.applyBoneVisibilityByPart(slot, modelPart, model);
        armorModel.renderToBuffer(
            context.poseStack(),
            null,
            context.packedLight(),
            OverlayTexture.NO_OVERLAY,
            context.red(),
            context.green(),
            context.blue(),
            context.alpha()
        );
    }

    /**
     * Renders a vanilla (non-AzureLib) armor piece through 1.7.10's GL model rendering.
     */
    protected void renderVanillaArmorPiece(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        AzEquipmentSlot slot,
        ItemStack armorStack,
        ModelPart modelPart
    ) {
        flush(context.multiBufferSource());

        float light = Math.max(
            LightTexture.block(context.packedLight()),
            LightTexture.sky(context.packedLight())
        ) / 15.0F;

        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(context.poseStack().last());

        mod.azure.azurelib.render.texture.TextureManager.getInstance()
            .bindTexture(getVanillaArmorResource(context.animatable(), armorStack, slot, null));

        GlStateManager.color(
            context.red() * light,
            context.green() * light,
            context.blue() * light,
            context.alpha()
        );
        modelPart.render(0.0625F);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    /**
     * Returns a cached instance of a base BipedEntityModel that is used for rendering/modeling the provided
     * {@link ItemStack}
     */
    @Nullable
    protected AzArmorRenderer getRendererForItem(ItemStack stack) {
        return AzArmorRendererRegistryHolder.get(stack);
    }

    protected BipedEntityModel getModelForItem(ItemStack stack, AzEquipmentSlot slot) {
        AzArmorRenderer renderer = getRendererForItem(stack);

        if (renderer == null) {
            return slot == AzEquipmentSlot.LEGS ? INNER_ARMOR_MODEL : OUTER_ARMOR_MODEL;
        }

        return renderer.rendererPipeline().armorModel();
    }

    /**
     * Prepares the given {@link ModelPart} for render by setting its translation, rotation, and scale to the
     * {@link AzBone}
     */
    protected void prepModelPartForRender(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ModelPart sourcePart
    ) {
        GeoCube firstCube = bone.getCubes().get(0);
        float[] armorCube = armorCubeSize(this.currentArmorModel, sourcePart);
        double armorBoneSizeX = firstCube.size().x();
        double armorBoneSizeY = firstCube.size().y();
        double armorBoneSizeZ = firstCube.size().z();
        double actualArmorSizeX = armorCube[0];
        double actualArmorSizeY = armorCube[1];
        double actualArmorSizeZ = armorCube[2];
        float scaleX = (float) (armorBoneSizeX / actualArmorSizeX);
        float scaleY = (float) (armorBoneSizeY / actualArmorSizeY);
        float scaleZ = (float) (armorBoneSizeZ / actualArmorSizeZ);

        McClient.setPivot(
            sourcePart,
            -(bone.getPivotX() - ((bone.getPivotX() * scaleX) - bone.getPivotX()) / scaleX),
            -(bone.getPivotY() - ((bone.getPivotY() * scaleY) - bone.getPivotY()) / scaleY),
            (bone.getPivotZ() - ((bone.getPivotZ() * scaleZ) - bone.getPivotZ()) / scaleZ)
        );

        sourcePart.pitch = -bone.getRotX();
        sourcePart.yaw = -bone.getRotY();
        sourcePart.roll = bone.getRotZ();

        context.poseStack().scale(scaleX, scaleY, scaleZ);
    }

    /** Beta's armor texture prefixes, indexed by {@link Mc#armorTextureIndex}. */
    protected static final String[] ARMOR_PREFIXES = { "cloth", "chain", "iron", "diamond", "gold" };

    /**
     * Gets the texture for a vanilla (non-AzureLib) armor piece, following Beta's {@code PlayerEntityRenderer}:
     * {@code /armor/<material>_1.png} for everything but leggings, which use {@code _2}.
     *
     * @param type unused on Beta (there are no dyed overlays); kept for API parity
     */
    public ResourceLocation getVanillaArmorResource(
        Entity entity,
        ItemStack stack,
        AzEquipmentSlot slot,
        @Nullable String type
    ) {
        int index = Mc.armorTextureIndex(Mc.item(stack));
        String prefix = index >= 0 && index < ARMOR_PREFIXES.length ? ARMOR_PREFIXES[index] : "iron";
        String path = ARMOR_PATH_CACHE.computeIfAbsent(
            prefix + (slot == AzEquipmentSlot.LEGS ? "_2" : "_1"),
            key -> new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, "armor/" + key + ".png")
        ).getResourcePath();
        return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path);
    }

    private static void flush(MultiBufferSource bufferSource) {
        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }
    }

    private static ModelPart[] parts(BipedEntityModel m) {
        return new ModelPart[] {
            m.head,
            m.hat,
            m.body,
            m.rightArm,
            m.leftArm,
            m.rightLeg,
            m.leftLeg
        };
    }

    private void savePoseOnce(BipedEntityModel model) {
        savedPoses.computeIfAbsent(model, AzArmorLayer::capture);
    }

    private void restorePoses() {
        savedPoses.forEach(AzArmorLayer::apply);
        savedPoses.clear();
    }

    private static int partIndex(BipedEntityModel m, ModelPart part) {
        ModelPart[] p = parts(m);
        for (int i = 0; i < p.length; i++) {
            if (p[i] == part)
                return i;
        }
        return -1;
    }

    private static float[][] capture(BipedEntityModel m) {
        ModelPart[] p = parts(m);
        float[][] out = new float[p.length][];
        for (int i = 0; i < p.length; i++) {
            out[i] = capturePart(p[i]);
        }
        return out;
    }

    private static float[] capturePart(ModelPart r) {
        return new float[] {
            r.pivotX,
            r.pivotY,
            r.pivotZ,
            r.pitch,
            r.yaw,
            r.roll
        };
    }

    private static void apply(BipedEntityModel m, float[][] pose) {
        ModelPart[] p = parts(m);
        for (int i = 0; i < p.length; i++) {
            float[] s = pose[i];
            McClient.setPivot(p[i], s[0], s[1], s[2]);
            p[i].pitch = s[3];
            p[i].yaw = s[4];
            p[i].roll = s[5];
        }
    }

    /**
     * The size of a biped armor part's cube, as 1.7.10's {@code ModelBox#posX1..posZ2} report it: the box's declared
     * size <em>before</em> dilation (head 8x8x8, body 8x12x4, limbs 4x12x4). Beta keeps a part's cubes in a private
     * list, but biped parts always have these vanilla sizes. The dilation is deliberately left out, as in 1.7.10: the
     * bone-fitting maths divides the bone size by this, and including the dilation shrinks every piece.
     */
    protected static float[] armorCubeSize(@Nullable BipedEntityModel owner, ModelPart part) {
        int index = owner == null ? -1 : partIndex(owner, part);
        switch (index) {
            case 0:
            case 1:
                return new float[] { 8, 8, 8 };
            case 2:
                return new float[] { 8, 12, 4 };
            default:
                return new float[] { 4, 12, 4 };
        }
    }

    private static final class AzArmorRendererRegistryHolder {

        @Nullable
        static AzArmorRenderer get(ItemStack stack) {
            return AzArmorRendererRegistry.getOrNull(stack.getItem());
        }
    }
}
