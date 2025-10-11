package mod.azure.azurelib.render.layer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Builtin class for handling dynamic armor rendering on AzureLib entities.<br>
 * Supports {@link ArmorItem Vanilla} armor models.<br>
 * Unlike a traditional armor renderer, this renderer renders per-bone, giving much more flexible armor rendering.
 */
public class AzArmorLayer<T extends LivingEntity> implements AzRenderLayer<UUID, T> {

    protected static final Map<String, ResourceLocation> ARMOR_PATH_CACHE = new Object2ObjectOpenHashMap<>();

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
    public void preRender(AzRendererPipelineContext<UUID, T> context) {
        this.mainHandStack = context.animatable().getItemBySlot(EquipmentSlot.MAINHAND);
        this.offhandStack = context.animatable().getItemBySlot(EquipmentSlot.OFFHAND);
        this.helmetStack = context.animatable().getItemBySlot(EquipmentSlot.HEAD);
        this.chestplateStack = context.animatable().getItemBySlot(EquipmentSlot.CHEST);
        this.leggingsStack = context.animatable().getItemBySlot(EquipmentSlot.LEGS);
        this.bootsStack = context.animatable().getItemBySlot(EquipmentSlot.FEET);
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

        context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));

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
        var renderer = AzArmorRendererRegistry.getOrNull(armorStack.getItem());
        var model = getModelForItem(armorStack, slot);
        var modelPart = getModelPartForBone(context, bone, model);

        if (!modelPart.cubes.isEmpty()) {
            context.poseStack().pushPose();
            context.poseStack().scale(-1, -1, 1);

            if (renderer != null) {
                prepModelPartForRender(context, bone, modelPart);
                renderAzArmorPiece(context, slot, armorStack, renderer, context.animatable(), model, modelPart);
            } else if (armorStack.getItem() instanceof ArmorItem) {
                prepModelPartForRender(context, bone, modelPart);
                renderVanillaArmorPiece(context, bone, slot, armorStack, modelPart);
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
            var isHumanoidArmorSlotType = slot.getType() == EquipmentSlot.Type.ARMOR;

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

    protected void renderAzArmorPiece(
        AzRendererPipelineContext<UUID, T> context,
        EquipmentSlot slot,
        ItemStack armorStack,
        AzArmorRenderer renderer,
        LivingEntity entity,
        HumanoidModel<T> model,
        ModelPart modelPart
    ) {
        var renderPipelines = renderer.rendererPipeline();
        var boneContext = renderPipelines.context().boneContext();
        var armorModel = renderPipelines.armorModel();

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
     * Renders an individual armor piece base on the given {@link AzBone} and {@link ItemStack}
     */
    protected <I extends Item> void renderVanillaArmorPiece(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        EquipmentSlot slot,
        ItemStack armorStack,
        ModelPart modelPart
    ) {
        var buffer = getVanillaArmorBuffer(
            context,
            armorStack,
            slot,
            bone,
            false
        );

        modelPart.render(context.poseStack(), buffer, context.packedLight(), context.packedOverlay());

        if (armorStack.hasFoil())
            modelPart.render(
                context.poseStack(),
                getVanillaArmorBuffer(
                    context,
                    armorStack,
                    slot,
                    bone,
                    true
                ),
                context.packedLight(),
                context.packedOverlay(),
                context.red(),
                context.green(),
                context.blue(),
                context.alpha()
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
     * @param forGlint A flag indicating whether the armor piece should render with a glint effect.
     * @return The {@link VertexConsumer} used to render the designated armor piece with the appropriate style and
     *         effect.
     */
    protected VertexConsumer getVanillaArmorBuffer(
        AzRendererPipelineContext<UUID, T> context,
        ItemStack stack,
        EquipmentSlot slot,
        AzBone bone,
        boolean forGlint
    ) {
        if (forGlint) {
            return context.multiBufferSource().getBuffer(RenderType.armorEntityGlint());
        }

        return context.multiBufferSource()
            .getBuffer(
                RenderType.armorCutoutNoCull(getVanillaArmorResource(context.animatable(), stack, slot, bone.getName()))
            );
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
    protected HumanoidModel<T> getModelForItem(ItemStack stack, EquipmentSlot slot) {
        var renderer = getRendererForItem(stack);

        if (renderer == null) {
            return (HumanoidModel<T>) (slot == EquipmentSlot.LEGS ? INNER_ARMOR_MODEL : OUTER_ARMOR_MODEL);
        }

        return (HumanoidModel<T>) renderer.rendererPipeline().armorModel();
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
        GameProfile skullProfile = null;
        CompoundTag stackTag = stack.getTag();

        if (stackTag != null) {
            Tag skullTag = stackTag.get(PlayerHeadItem.TAG_SKULL_OWNER);

            if (skullTag instanceof CompoundTag compoundTag) {
                skullProfile = NbtUtils.readGameProfile(compoundTag);
            } else if (skullTag instanceof StringTag tag) {
                String skullOwner = tag.getAsString();

                if (!skullOwner.isBlank()) {
                    CompoundTag profileTag = new CompoundTag();

                    SkullBlockEntity.updateGameprofile(
                        new GameProfile(null, skullOwner),
                        name -> stackTag.put(
                            PlayerHeadItem.TAG_SKULL_OWNER,
                            NbtUtils.writeGameProfile(profileTag, name)
                        )
                    );

                    skullProfile = NbtUtils.readGameProfile(profileTag);
                }
            }
        }
        var type = skullBlock.getType();
        var model = SkullBlockRenderer.createSkullRenderers(Minecraft.getInstance().getEntityModels())
            .get(type);
        var renderType = SkullBlockRenderer.getRenderType(type, skullProfile);

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
     * on the provided {@link AzBone}
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

    /**
     * Gets a cached resource path for the vanilla armor layer texture for this armor piece.
     * <p>
     * Equivalent to {@link net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#getArmorLocation
     * HumanoidArmorLayer.getArmorLocation}
     */
    public ResourceLocation getVanillaArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, String type) {
        String domain = "minecraft";
        String path = ((ArmorItem) stack.getItem()).getMaterial().getName();
        String[] materialNameSplit = path.split(":", 2);

        if (materialNameSplit.length > 1) {
            domain = materialNameSplit[0];
            path = materialNameSplit[1];
        }

        String texture = String.format(
            "%s:textures/models/armor/%s_layer_%d.png",
            domain,
            path,
            (slot == EquipmentSlot.LEGS ? 2 : 1)
        );
        return ARMOR_PATH_CACHE.computeIfAbsent(texture, ResourceLocation::new);
    }
}
