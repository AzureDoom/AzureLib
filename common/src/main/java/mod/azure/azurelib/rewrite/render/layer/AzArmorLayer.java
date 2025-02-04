package mod.azure.azurelib.rewrite.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererRegistry;

/**
 * Builtin class for handling dynamic armor rendering on AzureLib entities.<br>
 * Supports {@link net.minecraft.world.item.ArmorItem Vanilla} armor models.<br>
 * Unlike a traditional armor renderer, this renderer renders per-bone, giving much more flexible armor rendering.
 */
public class AzArmorLayer<T extends LivingEntity> implements AzRenderLayer<T> {

    protected static final HumanoidModel<LivingEntity> INNER_ARMOR_MODEL = new HumanoidModel<>(
        Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)
    );

    protected static final HumanoidModel<LivingEntity> OUTER_ARMOR_MODEL = new HumanoidModel<>(
        Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)
    );

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
    public void preRender(AzRendererPipelineContext<T> context) {
        if (!(context.animatable() instanceof LivingEntity livingEntity)) {
            return;
        }

        this.mainHandStack = livingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
        this.offhandStack = livingEntity.getItemBySlot(EquipmentSlot.OFFHAND);
        this.helmetStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        this.chestplateStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        this.leggingsStack = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
        this.bootsStack = livingEntity.getItemBySlot(EquipmentSlot.FEET);
    }

    @Override
    public void render(AzRendererPipelineContext<T> context) {}

    /**
     * Renders the given armor or skull block for the specified bone using the provided rendering context. Depending on
     * the type of item, it delegates rendering to appropriate methods.
     *
     * @param context The rendering context containing necessary parameters for rendering, like pose stack, light level,
     *                etc.
     * @param bone    The specific bone of the model where the armor or skull block will be rendered.
     */
    @Override
    public void renderForBone(AzRendererPipelineContext<T> context, AzBone bone) {
        var poseStack = context.poseStack();
        var armorStack = getArmorItemForBone(context, bone);

        if (armorStack == null) {
            return;
        }

        if (
            armorStack.getItem() instanceof BlockItem blockItem && blockItem
                .getBlock() instanceof AbstractSkullBlock skullBlock
        ) {
            renderSkullAsArmor(context, bone, armorStack, skullBlock);
        } else {
            renderArmor(context, bone, armorStack, poseStack);
        }
    }

    /**
     * Renders armor items on a given bone within the render cycle of a model. This method determines the appropriate
     * equipment slot, renderer, and model for the armor item and handles the rendering process accordingly.
     *
     * @param context    The rendering context containing the animatable instance and other data essential for
     *                   rendering.
     * @param bone       The specific bone of the model where the armor piece will be rendered.
     * @param armorStack The ItemStack representing the armor item to render.
     * @param poseStack  The matrix stack used to apply transformations during rendering.
     */
    private void renderArmor(
        AzRendererPipelineContext<T> context,
        AzBone bone,
        ItemStack armorStack,
        PoseStack poseStack
    ) {
        var slot = getEquipmentSlotForBone(context, bone, armorStack);
        var renderer = getRendererForItem(armorStack);
        var model = getModelForItem(armorStack, slot);
        var modelPart = getModelPartForBone(context, model);

        if (!modelPart.cubes.isEmpty()) {
            poseStack.pushPose();
            poseStack.scale(-1, -1, 1);

            if (renderer != null && context.animatable() instanceof Entity entity) {
                var boneContext = renderer.rendererPipeline().context().boneContext();

                prepModelPartForRender(context, bone, modelPart);
                renderer.prepForRender(entity, armorStack, slot, model);
                boneContext.applyBoneVisibilityByPart(slot, modelPart, model);
                model.renderToBuffer(
                    poseStack,
                    null,
                    context.packedLight(),
                    context.packedOverlay(),
                    armorStack.is(
                        ItemTags.DYEABLE
                    ) ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(armorStack, -6265536)) : -1
                );
            } else if (armorStack.getItem() instanceof ArmorItem) {
                prepModelPartForRender(context, bone, modelPart);
                renderVanillaArmorPiece(
                    context,
                    bone,
                    slot,
                    armorStack,
                    modelPart
                );
            }

            poseStack.popPose();
        }
    }

    /**
     * Return an EquipmentSlot for a given {@link ItemStack} and animatable instance.<br>
     * This is what determines the base model to use for rendering a particular stack
     */
    protected @NotNull EquipmentSlot getEquipmentSlotForBone(
        AzRendererPipelineContext<T> context,
        AzBone bone,
        ItemStack stack
    ) {
        var animatable = context.animatable();

        if (animatable instanceof LivingEntity livingEntity) {
            for (var slot : EquipmentSlot.values()) {
                var isHumanoidArmorSlotType = slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;

                if (isHumanoidArmorSlotType && stack == livingEntity.getItemBySlot(slot)) {
                    return slot;
                }
            }
        }

        return EquipmentSlot.CHEST;
    }

    /**
     * Return a ModelPart for a given {@link AzBone}.<br>
     * This is then transformed into position for the final render
     */
    @NotNull
    protected ModelPart getModelPartForBone(AzRendererPipelineContext<T> context, HumanoidModel<?> baseModel) {
        return baseModel.body;
    }

    /**
     * Get the {@link ItemStack} relevant to the bone being rendered.<br>
     * Return null if this bone should be ignored
     */
    @Nullable
    protected ItemStack getArmorItemForBone(AzRendererPipelineContext<T> context, AzBone bone) {
        return null;
    }

    /**
     * Renders an individual armor piece base on the given {@link AzBone} and {@link ItemStack}
     */
    protected <I extends Item> void renderVanillaArmorPiece(
        AzRendererPipelineContext<T> context,
        AzBone bone,
        EquipmentSlot slot,
        ItemStack armorStack,
        ModelPart modelPart
    ) {
        var material = ((ArmorItem) armorStack.getItem()).getMaterial();

        for (var layer : material.value().layers()) {
            var buffer = getVanillaArmorBuffer(
                context,
                armorStack,
                slot,
                bone,
                layer,
                false
            );

            modelPart.render(context.poseStack(), buffer, context.packedLight(), context.packedOverlay());
        }

        var trim = armorStack.get(DataComponents.TRIM);

        if (trim != null) {
            var spriteLocation = slot == EquipmentSlot.LEGS ? trim.innerTexture(material) : trim.outerTexture(material);
            var consumer = context.multiBufferSource()
                .getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal()));
            var sprite = Minecraft.getInstance()
                .getModelManager()
                .getAtlas(Sheets.ARMOR_TRIMS_SHEET)
                .getSprite(spriteLocation);
            var buffer = sprite.wrap(consumer);
            modelPart.render(context.poseStack(), buffer, context.packedLight(), context.packedOverlay());
        }

        if (armorStack.hasFoil())
            modelPart.render(
                context.poseStack(),
                getVanillaArmorBuffer(
                    context,
                    armorStack,
                    slot,
                    bone,
                    null,
                    true
                ),
                context.packedLight(),
                context.packedOverlay(),
                1
            );
    }

    /**
     * Retrieves a {@link VertexConsumer} for rendering vanilla-styled armor. The method determines whether the armor
     * should apply a glint effect or not and selects the appropriate render type accordingly.
     *
     * @param context  The rendering context providing necessary data for rendering, including the animatable instance
     *                 and the buffer source.
     * @param stack    The armor {@link ItemStack} being rendered.
     * @param slot     The {@link EquipmentSlot} the armor piece occupies.
     * @param bone     The model bone associated with the armor piece.
     * @param layer    The optional {@link ArmorMaterial.Layer} providing texture resources for rendering the armor.
     * @param forGlint A flag indicating whether the armor piece should render with a glint effect.
     * @return The {@link VertexConsumer} used to render the designated armor piece with the appropriate style and
     *         effect.
     */
    protected VertexConsumer getVanillaArmorBuffer(
        AzRendererPipelineContext<T> context,
        ItemStack stack,
        EquipmentSlot slot,
        AzBone bone,
        @Nullable ArmorMaterial.Layer layer,
        boolean forGlint
    ) {
        if (forGlint) {
            return context.multiBufferSource().getBuffer(RenderType.armorEntityGlint());
        }

        return context.multiBufferSource()
            .getBuffer(RenderType.armorCutoutNoCull(layer.texture(slot == EquipmentSlot.LEGS)));
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
        var item = stack.getItem();
        return AzArmorRendererRegistry.getOrNull(item);
    }

    /**
     * Returns a cached instance of a base HumanoidModel that is used for rendering/modelling the provided
     * {@link ItemStack}
     */
    protected HumanoidModel<?> getModelForItem(ItemStack stack, EquipmentSlot slot) {
        var renderer = getRendererForItem(stack);

        if (renderer == null) {
            return slot == EquipmentSlot.LEGS ? INNER_ARMOR_MODEL : OUTER_ARMOR_MODEL;
        }

        return renderer.rendererPipeline().armorModel();
    }

    /**
     * Render a given {@link AbstractSkullBlock} as a worn armor piece in relation to a given {@link AzBone}
     */
    protected void renderSkullAsArmor(
        AzRendererPipelineContext<T> context,
        AzBone bone,
        ItemStack stack,
        AbstractSkullBlock skullBlock
    ) {
        var type = skullBlock.getType();
        var model = SkullBlockRenderer.createSkullRenderers(Minecraft.getInstance().getEntityModels())
            .get(type);
        var renderType = SkullBlockRenderer.getRenderType(type, stack.get(DataComponents.PROFILE));

        context.poseStack().pushPose();
        RenderUtils.translateAndRotateMatrixForBone(context.poseStack(), bone);
        context.poseStack().scale(1.1875f, 1.1875f, 1.1875f);
        context.poseStack().translate(-0.5f, 0, -0.5f);
        SkullBlockRenderer.renderSkull(
            null,
            0,
            0,
            context.poseStack(),
            context.multiBufferSource(),
            context.packedLight(),
            model,
            renderType
        );
        context.poseStack().popPose();
    }

    /**
     * Prepares the given {@link ModelPart} for render by setting its translation, position, and rotation values based
     * on the provided {@link AzBone}.
     * <br>
     * This implementation uses the <b><u>FIRST</u></b> cube in the source part
     * to determine the scale and position of the GeoArmor to be rendered
     *
     * @param context
     * @param bone       The AzBone to base the translations on
     * @param sourcePart The ModelPart to translate
     */
    protected void prepModelPartForRender(AzRendererPipelineContext<T> context, AzBone bone, ModelPart sourcePart) {
        var firstCube = bone.getCubes().get(0);
        var armorCube = sourcePart.cubes.get(0);
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
