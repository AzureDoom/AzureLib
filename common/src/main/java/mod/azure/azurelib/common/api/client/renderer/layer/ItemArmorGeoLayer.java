/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.api.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.api.client.renderer.GeoArmorRenderer;
import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.internal.client.RenderProvider;
import mod.azure.azurelib.common.internal.client.renderer.GeoRenderer;
import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mod.azure.azurelib.common.internal.common.cache.object.GeoBone;
import mod.azure.azurelib.common.internal.common.cache.object.GeoCube;
import mod.azure.azurelib.core.animatable.GeoAnimatable;

/**
 * Builtin class for handling dynamic armor rendering on AzureLib entities.<br>
 * Supports both {@link GeoItem AzureLib} and {@link net.minecraft.world.item.ArmorItem Vanilla} armor models.<br>
 * Unlike a traditional armor renderer, this renderer renders per-bone, giving much more flexible armor rendering.
 */
@Deprecated(forRemoval = true)
public class ItemArmorGeoLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {

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

    public ItemArmorGeoLayer(GeoRenderer<T> geoRenderer) {
        super(geoRenderer);
    }

    /**
     * Return an EquipmentSlot for a given {@link ItemStack} and animatable instance.<br>
     * This is what determines the base model to use for rendering a particular stack
     */
    @NotNull
    protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, T animatable) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                if (stack == animatable.getItemBySlot(slot))
                    return slot;
            }
        }

        return EquipmentSlot.CHEST;
    }

    /**
     * Return a ModelPart for a given {@link GeoBone}.<br>
     * This is then transformed into position for the final render
     */
    @NotNull
    protected ModelPart getModelPartForBone(
        GeoBone bone,
        EquipmentSlot slot,
        ItemStack stack,
        T animatable,
        HumanoidModel<?> baseModel
    ) {
        return baseModel.body;
    }

    /**
     * Get the {@link ItemStack} relevant to the bone being rendered.<br>
     * Return null if this bone should be ignored
     */
    @Nullable
    protected ItemStack getArmorItemForBone(GeoBone bone, T animatable) {
        return null;
    }

    /**
     * This method is called by the {@link GeoRenderer} before rendering, immediately after
     * {@link GeoRenderer#preRender} has been called.<br>
     * This allows for RenderLayers to perform pre-render manipulations such as hiding or showing bones
     */
    @Override
    public void preRender(
        PoseStack poseStack,
        T animatable,
        BakedGeoModel bakedModel,
        RenderType renderType,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        float partialTick,
        int packedLight,
        int packedOverlay
    ) {
        this.mainHandStack = animatable.getItemBySlot(EquipmentSlot.MAINHAND);
        this.offhandStack = animatable.getItemBySlot(EquipmentSlot.OFFHAND);
        this.helmetStack = animatable.getItemBySlot(EquipmentSlot.HEAD);
        this.chestplateStack = animatable.getItemBySlot(EquipmentSlot.CHEST);
        this.leggingsStack = animatable.getItemBySlot(EquipmentSlot.LEGS);
        this.bootsStack = animatable.getItemBySlot(EquipmentSlot.FEET);
    }

    /**
     * This method is called by the {@link GeoRenderer} for each bone being rendered.<br>
     * This is a more expensive call, particularly if being used to render something on a different buffer.<br>
     * It does however have the benefit of having the matrix translations and other transformations already applied from
     * render-time.<br>
     * It's recommended to avoid using this unless necessary.<br>
     * <br>
     * The {@link GeoBone} in question has already been rendered by this stage.<br>
     * <br>
     * If you <i>do</i> use it, and you render something that changes the {@link VertexConsumer buffer}, you need to
     * reset it back to the previous buffer using {@link MultiBufferSource#getBuffer} before ending the method
     */
    @Override
    public void renderForBone(
        PoseStack poseStack,
        T animatable,
        GeoBone bone,
        RenderType renderType,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        float partialTick,
        int packedLight,
        int packedOverlay
    ) {
        ItemStack armorStack = getArmorItemForBone(bone, animatable);

        if (armorStack == null)
            return;

        if (
            armorStack.getItem() instanceof BlockItem blockItem && blockItem
                .getBlock() instanceof AbstractSkullBlock skullBlock
        ) {
            renderSkullAsArmor(poseStack, bone, armorStack, skullBlock, bufferSource, packedLight);
        } else {
            EquipmentSlot slot = getEquipmentSlotForBone(bone, armorStack, animatable);
            HumanoidModel<?> model = getModelForItem(bone, slot, armorStack, animatable);
            ModelPart modelPart = getModelPartForBone(bone, slot, armorStack, animatable, model);

            if (!modelPart.cubes.isEmpty()) {
                poseStack.pushPose();
                poseStack.scale(-1, -1, 1);

                if (model instanceof GeoArmorRenderer<?> geoArmorRenderer) {
                    prepModelPartForRender(poseStack, bone, modelPart);
                    geoArmorRenderer.prepForRender(animatable, armorStack, slot, model);
                    geoArmorRenderer.applyBoneVisibilityByPart(slot, modelPart, model);
                    geoArmorRenderer.renderToBuffer(
                        poseStack,
                        null,
                        packedLight,
                        packedOverlay,
                        armorStack.is(
                            ItemTags.DYEABLE
                        ) ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(armorStack, -6265536)) : -1
                    );
                } else if (armorStack.getItem() instanceof ArmorItem) {
                    prepModelPartForRender(poseStack, bone, modelPart);
                    renderVanillaArmorPiece(
                        poseStack,
                        animatable,
                        bone,
                        slot,
                        armorStack,
                        modelPart,
                        bufferSource,
                        partialTick,
                        packedLight,
                        packedOverlay
                    );
                }

                poseStack.popPose();
            }
        }
    }

    /**
     * Renders an individual armor piece base on the given {@link GeoBone} and {@link ItemStack}
     */
    protected <I extends Item & GeoItem> void renderVanillaArmorPiece(
        PoseStack poseStack,
        T animatable,
        GeoBone bone,
        EquipmentSlot slot,
        ItemStack armorStack,
        ModelPart modelPart,
        MultiBufferSource bufferSource,
        float partialTick,
        int packedLight,
        int packedOverlay
    ) {
        Holder<ArmorMaterial> material = ((ArmorItem) armorStack.getItem()).getMaterial();

        for (ArmorMaterial.Layer layer : material.value().layers()) {
            int color = armorStack.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(armorStack, -6265536) : 0xFFFFFF;
            VertexConsumer buffer = getVanillaArmorBuffer(
                bufferSource,
                animatable,
                armorStack,
                slot,
                bone,
                layer,
                packedLight,
                packedOverlay,
                false
            );

            modelPart.render(poseStack, buffer, packedLight, packedOverlay);
        }

        ArmorTrim trim = armorStack.get(DataComponents.TRIM);

        if (trim != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance()
                .getModelManager()
                .getAtlas(
                    Sheets.ARMOR_TRIMS_SHEET
                )
                .getSprite(
                    slot == EquipmentSlot.LEGS ? trim.innerTexture(material) : trim.outerTexture(material)
                );
            VertexConsumer buffer = sprite.wrap(
                bufferSource.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal()))
            );
            modelPart.render(poseStack, buffer, packedLight, packedOverlay);
        }

        if (armorStack.hasFoil())
            modelPart.render(
                poseStack,
                getVanillaArmorBuffer(
                    bufferSource,
                    animatable,
                    armorStack,
                    slot,
                    bone,
                    null,
                    packedLight,
                    packedOverlay,
                    true
                ),
                packedLight,
                packedOverlay,
                1
            );
    }

    protected VertexConsumer getVanillaArmorBuffer(
        MultiBufferSource bufferSource,
        T animatable,
        ItemStack stack,
        EquipmentSlot slot,
        GeoBone bone,
        @Nullable ArmorMaterial.Layer layer,
        int packedLight,
        int packedOverlay,
        boolean forGlint
    ) {
        if (forGlint)
            return bufferSource.getBuffer(RenderType.armorEntityGlint());

        return bufferSource.getBuffer(RenderType.armorCutoutNoCull(layer.texture(slot == EquipmentSlot.LEGS)));
    }

    /**
     * Returns a cached instance of a base HumanoidModel that is used for rendering/modelling the provided
     * {@link ItemStack}
     */
    @NotNull
    protected HumanoidModel<?> getModelForItem(GeoBone bone, EquipmentSlot slot, ItemStack stack, T animatable) {
        HumanoidModel<LivingEntity> defaultModel = slot == EquipmentSlot.LEGS ? INNER_ARMOR_MODEL : OUTER_ARMOR_MODEL;

        return RenderProvider.of(stack).getHumanoidArmorModel(animatable, stack, slot, defaultModel);
    }

    /**
     * Render a given {@link AbstractSkullBlock} as a worn armor piece in relation to a given {@link GeoBone}
     */
    protected void renderSkullAsArmor(
        PoseStack poseStack,
        GeoBone bone,
        ItemStack stack,
        AbstractSkullBlock skullBlock,
        MultiBufferSource bufferSource,
        int packedLight
    ) {
        SkullBlock.Type type = skullBlock.getType();
        SkullModelBase model = SkullBlockRenderer.createSkullRenderers(Minecraft.getInstance().getEntityModels())
            .get(
                type
            );
        RenderType renderType = SkullBlockRenderer.getRenderType(type, stack.get(DataComponents.PROFILE));

        poseStack.pushPose();
        RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);
        poseStack.scale(1.1875f, 1.1875f, 1.1875f);
        poseStack.translate(-0.5f, 0, -0.5f);
        SkullBlockRenderer.renderSkull(null, 0, 0, poseStack, bufferSource, packedLight, model, renderType);
        poseStack.popPose();
    }

    /**
     * Prepares the given {@link ModelPart} for render by setting its translation, position, and rotation values based
     * on the provided {@link GeoBone}
     *
     * @param poseStack  The PoseStack being used for rendering
     * @param bone       The GeoBone to base the translations on
     * @param sourcePart The ModelPart to translate
     */
    protected void prepModelPartForRender(PoseStack poseStack, GeoBone bone, ModelPart sourcePart) {
        final GeoCube firstCube = bone.getCubes().get(0);
        final Cube armorCube = sourcePart.cubes.get(0);
        final double armorBoneSizeX = firstCube.size().x();
        final double armorBoneSizeY = firstCube.size().y();
        final double armorBoneSizeZ = firstCube.size().z();
        final double actualArmorSizeX = Math.abs(armorCube.maxX - armorCube.minX);
        final double actualArmorSizeY = Math.abs(armorCube.maxY - armorCube.minY);
        final double actualArmorSizeZ = Math.abs(armorCube.maxZ - armorCube.minZ);
        float scaleX = (float) (armorBoneSizeX / actualArmorSizeX);
        float scaleY = (float) (armorBoneSizeY / actualArmorSizeY);
        float scaleZ = (float) (armorBoneSizeZ / actualArmorSizeZ);

        sourcePart.setPos(
            -(bone.getPivotX() - ((bone.getPivotX() * scaleX) - bone.getPivotX()) / scaleX),
            -(bone.getPivotY() - ((bone.getPivotY() * scaleY) - bone.getPivotY()) / scaleY),
            (bone.getPivotZ() - ((bone.getPivotZ() * scaleZ) - bone.getPivotZ()) / scaleZ)
        );

        sourcePart.xRot = -bone.getRotX();
        sourcePart.yRot = -bone.getRotY();
        sourcePart.zRot = bone.getRotZ();

        poseStack.scale(scaleX, scaleY, scaleZ);
    }
}
