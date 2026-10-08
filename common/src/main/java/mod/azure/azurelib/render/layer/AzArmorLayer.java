package mod.azure.azurelib.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Builtin class for handling dynamic armor rendering on AzureLib entities.<br>
 * Unlike a traditional armor renderer, this renderer renders per-bone, giving much more flexible armor rendering.
 */
public class AzArmorLayer<T extends LivingEntity> implements AzRenderLayer<UUID, T> {

    @SuppressWarnings("DataFlowIssue")
    protected static final Function<SkullBlock.Type, @Nullable SkullModelBase> SKULL_MODELS = Util.memoize(
        type -> SkullBlockRenderer.createModel(Minecraft.getInstance().getEntityModels(), type)
    );

    protected static ArmorModelSet<HumanoidModel<HumanoidRenderState>> createArmorModels() {
        return ModelLayers.PLAYER_ARMOR.map(
            layer -> new HumanoidModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(layer)
            )
        );
    }

    private final Map<AzBone, ArmorModelSet<HumanoidModel<HumanoidRenderState>>> armorModels =
        new IdentityHashMap<>();

    protected final EquipmentLayerRenderer equipmentRenderer;

    protected final PlayerSkinRenderCache skinCache;

    public AzArmorLayer(EntityRendererProvider.Context context) {
        this.equipmentRenderer = context.getEquipmentRenderer();
        this.skinCache = context.getPlayerSkinRenderCache();
    }

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

    /**
     * Prepares the necessary item stacks for rendering by accessing the relevant equipment slots of the animatable
     * instance. If the animatable instance is not a LivingEntity, the method returns without action.
     *
     * @param context The rendering context containing the animatable instance and other necessary data for rendering.
     */
    @Override
    public void preRender(AzRendererPipelineContext<UUID, T> context) {
        var animatable = context.animatable();

        this.mainHandStack = animatable.getItemBySlot(EquipmentSlot.MAINHAND);
        this.offhandStack = animatable.getItemBySlot(EquipmentSlot.OFFHAND);
        this.helmetStack = animatable.getItemBySlot(EquipmentSlot.HEAD);
        this.chestplateStack = animatable.getItemBySlot(EquipmentSlot.CHEST);
        this.leggingsStack = animatable.getItemBySlot(EquipmentSlot.LEGS);
        this.bootsStack = animatable.getItemBySlot(EquipmentSlot.FEET);
    }

    @Override
    public void render(AzRendererPipelineContext<UUID, T> context) {}

    /**
     * Renders the given armor or skull block for the specified bone using the provided rendering context. Depending on
     * the type of item, it delegates rendering to appropriate methods.
     *
     * @param context The rendering context containing necessary parameters for rendering, like pose stack, light level,
     *                etc.
     * @param bone    The specific bone of the model where the armor or skull block will be rendered.
     */
    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone) {
        var armorStack = getArmorItemForBone(context, bone);

        if (armorStack == null) {
            return;
        }

        context.poseStack().pushPose();
        if (
            armorStack.getItem() instanceof BlockItem blockItem && blockItem
                .getBlock() instanceof AbstractSkullBlock skullBlock
        ) {
            renderSkullAsArmor(context, bone, armorStack, skullBlock);
        } else {
            renderArmor(context, bone, armorStack);
        }

        context.poseStack().popPose();
    }

    /**
     * Renders armor items on a given bone within the render cycle of a model. This method determines the appropriate
     * equipment slot, renderer, and model for the armor item and handles the rendering process accordingly.
     *
     * @param context    The rendering context containing the animatable instance and other data essential for
     *                   rendering.
     * @param bone       The specific bone of the model where the armor piece will be rendered.
     * @param armorStack The ItemStack representing the armor item to render.
     */
    public void renderArmor(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ItemStack armorStack
    ) {
        var slot = getEquipmentSlotForBone(context, bone, armorStack);
        var model = getModelForItem(bone, slot);
        var modelPart = getModelPartForBone(context, bone, model);
        var renderer = AzArmorRendererRegistry.getOrNull(armorStack);

        if (!modelPart.cubes.isEmpty()) {
            context.poseStack().pushPose();
            RenderUtils.transformToBone(context.poseStack(), bone);
            RenderUtils.translateAwayFromPivotPoint(context.poseStack(), bone);
            context.poseStack().scale(-1, -1, 1);

            if (armorStack.has(DataComponents.EQUIPPABLE)) {
                prepModelPartForRender(context, bone, modelPart);
                if (renderer != null) {
                    renderAzArmorPiece(renderer, context, bone, slot, armorStack, modelPart, model);
                } else if (HumanoidArmorLayer.shouldRender(armorStack, slot)) {
                    renderArmorPiece(context, bone, slot, armorStack, modelPart, model);
                }
            }

            context.poseStack().popPose();
        }
    }

    /**
     * Return an EquipmentSlot for a given {@link ItemStack} and animatable instance.<br>
     * This is what determines the base model to use for rendering a particular stack
     */
    protected @NotNull EquipmentSlot getEquipmentSlotForBone(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ItemStack stack
    ) {
        var animatable = context.animatable();

        for (var slot : EquipmentSlot.values()) {
            var isHumanoidArmorSlotType = slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;

            if (isHumanoidArmorSlotType && stack == animatable.getItemBySlot(slot)) {
                return slot;
            }
        }

        return EquipmentSlot.CHEST;
    }

    /**
     * Return a ModelPart for a given {@link AzBone}.<br>
     * This is then transformed into position for the final render
     */
    @NotNull
    protected ModelPart getModelPartForBone(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        HumanoidModel<?> baseModel
    ) {
        var stack = getArmorItemForBone(context, bone);
        var slot = getEquipmentSlotForBone(context, bone, stack);

        return switch (slot) {
            case HEAD -> baseModel.head;
            case FEET -> baseModel.leftLeg;
            default -> baseModel.body;
        };
    }

    /**
     * Get the {@link ItemStack} relevant to the bone being rendered.<br>
     * Return null if this bone should be ignored
     */
    @Nullable
    protected ItemStack getArmorItemForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone) {
        return null;
    }

    protected <I extends Item> void renderAzArmorPiece(
        AzArmorRenderer renderer,
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        EquipmentSlot slot,
        ItemStack armorStack,
        ModelPart modelPart,
        HumanoidModel<?> baseModel
    ) {
        renderer.renderForBone(
            context.poseStack(),
            context.multiBufferSource(),
            context.animatable(),
            armorStack,
            slot,
            baseModel,
            modelPart,
            context.packedLight()
        );
    }

    /**
     * Renders an individual armor piece base on the given {@link AzBone} and {@link ItemStack}
     */
    protected void renderArmorPiece(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        EquipmentSlot slot,
        ItemStack armorStack,
        ModelPart modelPart,
        HumanoidModel<HumanoidRenderState> model
    ) {
        var equippable = armorStack.get(DataComponents.EQUIPPABLE);

        if (equippable == null || equippable.assetId().isEmpty()) {
            return;
        }

        ResourceKey<EquipmentAsset> assetId = equippable.assetId().get();
        var layerType = slot == EquipmentSlot.LEGS
            ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
            : EquipmentClientInfo.LayerType.HUMANOID;
        var renderState = new HumanoidRenderState();
        var packedLight = context.packedLight();
        var poseStack = context.poseStack();

        setOnlyPartVisible(model, modelPart);

        poseStack.pushPose();
        bakePartPose(poseStack, modelPart);

        context.multiBufferSource()
            .defer(
                poseStack,
                (deferredPoseStack, collector) -> equipmentRenderer.renderLayers(
                    layerType,
                    assetId,
                    model,
                    renderState,
                    armorStack,
                    deferredPoseStack,
                    collector,
                    packedLight,
                    renderState.outlineColor
                )
            );

        poseStack.popPose();
    }

    /**
     * Vanilla's model submission calls {@code setupAnim} when the batch is drawn, which resets every part to its
     * initial pose and discards the pose set by {@link #prepModelPartForRender}. Bake that pose into the pose stack
     * instead, and cancel out the initial pose the part will re-apply, so the drawn result matches the prepared part.
     */
    protected void bakePartPose(PoseStack poseStack, ModelPart part) {
        var initial = part.getInitialPose();

        poseStack.translate(part.x / 16f, part.y / 16f, part.z / 16f);

        if (part.xRot != 0 || part.yRot != 0 || part.zRot != 0)
            poseStack.mulPose(new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot));

        if (initial.xRot() != 0 || initial.yRot() != 0 || initial.zRot() != 0)
            poseStack.mulPose(new Quaternionf().rotationZYX(initial.zRot(), initial.yRot(), initial.xRot()).invert());

        poseStack.translate(-initial.x() / 16f, -initial.y() / 16f, -initial.z() / 16f);
    }

    protected void setOnlyPartVisible(HumanoidModel<?> model, ModelPart usedPart) {
        model.head.skipDraw = model.head != usedPart;
        model.hat.skipDraw = model.hat != usedPart;
        model.body.skipDraw = model.body != usedPart;
        model.leftArm.skipDraw = model.leftArm != usedPart;
        model.rightArm.skipDraw = model.rightArm != usedPart;
        model.leftLeg.skipDraw = model.leftLeg != usedPart;
        model.rightLeg.skipDraw = model.rightLeg != usedPart;
    }

    protected void restorePartVisibility(HumanoidModel<?> model) {
        model.head.skipDraw = false;
        model.hat.skipDraw = false;
        model.body.skipDraw = false;
        model.leftArm.skipDraw = false;
        model.rightArm.skipDraw = false;
        model.leftLeg.skipDraw = false;
        model.rightLeg.skipDraw = false;
    }

    /**
     * Retrieves the appropriate {@link AzArmorRenderer} for the given {@link ItemStack}. This method uses the
     * {@link AzArmorRendererRegistry} to fetch a renderer if one is registered for the specified item's class or
     * instance.
     *
     * @param stack The {@link ItemStack} for which the renderer is to be obtained.
     * @return The {@link AzArmorRenderer} associated with the item in the stack, or null if no renderer exists.
     */
    protected @Nullable AzArmorRenderer getRendererForItem(ItemStack stack) {
        return AzArmorRendererRegistry.getOrNull(stack);
    }

    /**
     * Returns a cached instance of a base HumanoidModel that is used for rendering/modelling the provided
     * {@link ItemStack}
     */
    protected HumanoidModel<HumanoidRenderState> getModelForItem(
        AzBone bone,
        EquipmentSlot slot
    ) {
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
            return null;

        return armorModels
            .computeIfAbsent(bone, ignored -> createArmorModels())
            .get(slot);
    }

    /**
     * Render a given {@link AbstractSkullBlock} as a worn armor piece in relation to a given {@link AzBone}
     */
    protected void renderSkullAsArmor(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ItemStack stack,
        AbstractSkullBlock skullBlock
    ) {
        var type = skullBlock.getType();
        var model = SKULL_MODELS.apply(type);

        if (model == null) {
            AzureLib.LOGGER.error(
                "Cannot render skull block that doesn't have a registered model! Skull type: {}",
                type
            );
            return;
        }

        var renderType = getSkullRenderType(type, stack);
        var packedLight = context.packedLight();
        var poseStack = context.poseStack();

        poseStack.pushPose();

        RenderUtils.transformToBone(poseStack, bone);
        poseStack.scale(-CustomHeadLayer.SKULL_SCALE, -CustomHeadLayer.SKULL_SCALE, CustomHeadLayer.SKULL_SCALE);

        context.multiBufferSource()
            .defer(
                poseStack,
                (deferredPoseStack, collector) -> SkullBlockRenderer.submitSkull(
                    0,
                    deferredPoseStack,
                    collector,
                    packedLight,
                    model,
                    renderType,
                    0,
                    null
                )
            );

        poseStack.popPose();
    }

    /**
     * Get the {@link RenderType} for a worn skull, using the head's profile skin for player heads
     */
    protected RenderType getSkullRenderType(SkullBlock.Type type, ItemStack stack) {
        if (type == SkullBlock.Types.PLAYER) {
            var profile = stack.get(DataComponents.PROFILE);

            if (profile != null)
                return this.skinCache.getOrDefault(profile).renderType();
        }

        return SkullBlockRenderer.getSkullRenderType(type, null);
    }

    /**
     * Prepares the given {@link ModelPart} for render by setting its translation, position, and rotation values based
     * on the provided {@link AzBone}. <br>
     * This implementation uses the <b><u>FIRST</u></b> cube in the source part to determine the scale and position of
     * the GeoArmor to be rendered
     *
     * @param context
     * @param bone       The AzBone to base the translations on
     * @param sourcePart The ModelPart to translate
     */
    protected void prepModelPartForRender(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ModelPart sourcePart
    ) {
        var firstCube = bone.getCubes().getFirst();
        var armorCube = sourcePart.cubes.getFirst();
        var armorBoneSizeX = firstCube.size().x();
        var armorBoneSizeY = firstCube.size().y();
        var armorBoneSizeZ = firstCube.size().z();
        var actualArmorSizeX = Math.abs(armorCube.maxX - armorCube.minX);
        var actualArmorSizeY = Math.abs(armorCube.maxY - armorCube.minY);
        var actualArmorSizeZ = Math.abs(armorCube.maxZ - armorCube.minZ);
        var scaleX = (float) (armorBoneSizeX / actualArmorSizeX);
        var scaleY = (float) (armorBoneSizeY / actualArmorSizeY);
        var scaleZ = (float) (armorBoneSizeZ / actualArmorSizeZ);

        sourcePart.setPos(
            -(bone.getPivotX() - ((bone.getPivotX() * scaleX) - bone.getPivotX()) / scaleX),
            -(bone.getPivotY() - ((bone.getPivotY() * scaleY) - bone.getPivotY()) / scaleY),
            (bone.getPivotZ() - ((bone.getPivotZ() * scaleZ) - bone.getPivotZ()) / scaleZ)
        );

        sourcePart.xRot = -bone.getRotX();
        sourcePart.yRot = -bone.getRotY();
        sourcePart.zRot = bone.getRotZ();

        context.poseStack().scale(scaleX, scaleY, scaleZ);
    }
}
