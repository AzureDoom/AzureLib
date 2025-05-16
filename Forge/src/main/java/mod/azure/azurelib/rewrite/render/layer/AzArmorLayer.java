package mod.azure.azurelib.rewrite.render.layer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.netty.util.internal.StringUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.armor.AzArmorModel;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererPipeline;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.rewrite.render.armor.bone.AzArmorBoneContext;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.UUID;

/**
 * Builtin class for handling dynamic armor rendering on AzureLib entities.<br>
 * Supports {@link ArmorItem Vanilla} armor models.<br>
 * Unlike a traditional armor renderer, this renderer renders per-bone, giving much more flexible armor rendering.
 */
public class AzArmorLayer<T extends LivingEntity> implements AzRenderLayer<T> {

    protected static final Map<String, ResourceLocation> ARMOR_PATH_CACHE = new Object2ObjectOpenHashMap<>();

    protected static final BipedModel<LivingEntity> INNER_ARMOR_MODEL = new BipedModel<>(0.5F);

    protected static final BipedModel<LivingEntity> OUTER_ARMOR_MODEL = new BipedModel<>(1.0F);

    protected ItemStack mainHandStack;

    protected ItemStack offhandStack;

    protected ItemStack helmetStack;

    protected ItemStack chestplateStack;

    protected ItemStack leggingsStack;

    protected ItemStack bootsStack;

    /**
     * Prepares the necessary item stacks for rendering by accessing the relevant equipment slots of the animatable
     * instance. If the animatable instance is not a LivingEntity, the method returns without action.
     *
     * @param context The rendering context containing the animatable instance and other necessary data for rendering.
     */
    @Override
    public void preRender(AzRendererPipelineContext<T> context) {
        this.mainHandStack = context.animatable().getItemStackFromSlot(EquipmentSlotType.MAINHAND);
        this.offhandStack = context.animatable().getItemStackFromSlot(EquipmentSlotType.OFFHAND);
        this.helmetStack = context.animatable().getItemStackFromSlot(EquipmentSlotType.HEAD);
        this.chestplateStack = context.animatable().getItemStackFromSlot(EquipmentSlotType.CHEST);
        this.leggingsStack = context.animatable().getItemStackFromSlot(EquipmentSlotType.LEGS);
        this.bootsStack = context.animatable().getItemStackFromSlot(EquipmentSlotType.FEET);
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
        ItemStack armorStack = getArmorItemForBone(context, bone);

        if (armorStack == null) {
            return;
        }

        if (
            armorStack.getItem() instanceof BlockItem && ((BlockItem) armorStack.getItem())
                .getBlock() instanceof AbstractSkullBlock
        ) {
            AbstractSkullBlock skullBlock = (AbstractSkullBlock) ((BlockItem) armorStack.getItem()).getBlock();
            renderSkullAsArmor(context, bone, armorStack, skullBlock);
        } else {
            renderArmor(context, bone, armorStack);
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
     */
    public void renderArmor(
        AzRendererPipelineContext<T> context,
        AzBone bone,
        ItemStack armorStack
    ) {
        EquipmentSlotType slot = getEquipmentSlotForBone(context, bone, armorStack);
        AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(armorStack.getItem());
        BipedModel<T> model = getModelForItem(armorStack, slot);

        ModelRenderer modelPart = getModelPartForBone(context, bone, model);

        if (!modelPart.cubeList.isEmpty()) {
            context.poseStack().push();
            context.poseStack().scale(-1, -1, 1);

            if (renderer != null) {
                prepModelPartForRender(context, bone, modelPart);
                renderAzArmorPiece(context, slot, armorStack, renderer, context.animatable(), model, modelPart);
            } else if (armorStack.getItem() instanceof ArmorItem) {
                prepModelPartForRender(context, bone, modelPart);
                renderVanillaArmorPiece(context, bone, slot, armorStack, modelPart);
            }

            context.poseStack().pop();
        }
    }

    /**
     * Return an EquipmentSlotType for a given {@link ItemStack} and animatable instance.<br>
     * This is what determines the base model to use for rendering a particular stack
     */
    protected EquipmentSlotType getEquipmentSlotForBone(
        AzRendererPipelineContext<T> context,
        AzBone bone,
        ItemStack stack
    ) {
        T animatable = context.animatable();

        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            boolean isHumanoidArmorSlotType = slot.getSlotType() == EquipmentSlotType.Group.ARMOR;

            if (isHumanoidArmorSlotType && stack == animatable.getItemStackFromSlot(slot)) {
                return slot;
            }
        }

        return EquipmentSlotType.CHEST;
    }

    /**
     * Return a ModelPart for a given {@link AzBone}.<br>
     * This is then transformed into position for the final render
     */

    protected ModelRenderer getModelPartForBone(
        AzRendererPipelineContext<T> context,
        AzBone bone,
        BipedModel<?> baseModel
    ) {
        return baseModel.bipedBody;
    }

    /**
     * Get the {@link ItemStack} relevant to the bone being rendered.<br>
     * Return null if this bone should be ignored
     */

    protected ItemStack getArmorItemForBone(AzRendererPipelineContext<T> context, AzBone bone) {
        return null;
    }

    protected void renderAzArmorPiece(
        AzRendererPipelineContext<T> context,
        EquipmentSlotType slot,
        ItemStack armorStack,
        AzArmorRenderer renderer,
        LivingEntity entity,
        BipedModel<T> model,
        ModelRenderer modelPart
    ) {
        AzArmorRendererPipeline renderPipelines = renderer.rendererPipeline();
        AzArmorBoneContext boneContext = renderPipelines.context().boneContext();
        AzArmorModel<?> armorModel = renderPipelines.armorModel();

        renderer.prepForRender(entity, armorStack, slot, model);
        boneContext.applyBoneVisibilityByPart(slot, modelPart, model);
        armorModel.render(
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
        AzRendererPipelineContext<T> context,
        AzBone bone,
        EquipmentSlotType slot,
        ItemStack armorStack,
        ModelRenderer modelPart
    ) {
        IVertexBuilder buffer = getVanillaArmorBuffer(
            context,
            armorStack,
            slot,
            bone,
            false
        );

        modelPart.render(context.poseStack(), buffer, context.packedLight(), context.packedOverlay());

        if (armorStack.hasEffect())
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
     * Retrieves a {@link IVertexBuilder} for rendering vanilla-styled armor. The method determines whether the armor
     * should apply a glint effect or not and selects the appropriate render type accordingly.
     *
     * @param context  The rendering context providing necessary data for rendering, including the animatable instance
     *                 and the buffer source.
     * @param stack    The armor {@link ItemStack} being rendered.
     * @param slot     The {@link EquipmentSlotType} the armor piece occupies.
     * @param bone     The model bone associated with the armor piece.
     * @param forGlint A flag indicating whether the armor piece should render with a glint effect.
     * @return The {@link IVertexBuilder} used to render the designated armor piece with the appropriate style and
     *         effect.
     */
    protected IVertexBuilder getVanillaArmorBuffer(
        AzRendererPipelineContext<T> context,
        ItemStack stack,
        EquipmentSlotType slot,
        AzBone bone,
        boolean forGlint
    ) {
        if (forGlint) {
            return context.multiBufferSource().getBuffer(RenderType.getEntityGlint());
        }

        return context.multiBufferSource()
            .getBuffer(
                RenderType.getEntityCutout(getVanillaArmorResource(context.animatable(), stack, slot, bone.getName()))
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
    protected AzArmorRenderer getRendererForItem(ItemStack stack) {
        Item item = stack.getItem();
        return AzArmorRendererRegistry.getOrNull(item);
    }

    /**
     * Returns a cached instance of a base BipedModel that is used for rendering/modelling the provided
     * {@link ItemStack}
     */
    protected BipedModel<T> getModelForItem(ItemStack stack, EquipmentSlotType slot) {
        AzArmorRenderer renderer = getRendererForItem(stack);

        if (renderer == null) {
            return (BipedModel<T>) (slot == EquipmentSlotType.LEGS ? INNER_ARMOR_MODEL : OUTER_ARMOR_MODEL);
        }

        return (BipedModel<T>) renderer.rendererPipeline().armorModel();
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
        GameProfile skullProfile = null;

        if (stack.hasTag()) {
            CompoundNBT compoundnbt = stack.getTag();
            if (compoundnbt.contains("SkullOwner", 10)) {
                skullProfile = NBTUtil.readGameProfile(compoundnbt.getCompound("SkullOwner"));
            } else if (compoundnbt.contains("SkullOwner", 8)) {
                String s = compoundnbt.getString("SkullOwner");
                if (!StringUtil.isNullOrEmpty(s)) {
                    skullProfile = SkullTileEntity.updateGameProfile(new GameProfile(null, s));
                    compoundnbt.put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), skullProfile));
                }
            }
        }

        context.poseStack().push();
        RenderUtils.translateAndRotateMatrixForBone(context.poseStack(), bone);
        context.poseStack().scale(1.1875f, 1.1875f, 1.1875f);
        context.poseStack().translate(-0.5f, 0, -0.5f);
        SkullTileEntityRenderer.render(
            (Direction) null,
            0.0F,
            ((AbstractSkullBlock) ((BlockItem) stack.getItem()).getBlock()).getSkullType(),
            skullProfile,
            0F /* limbswing, controls rotation */,
            context.poseStack(),
            context.multiBufferSource(),
            context.packedLight()
        );
        context.poseStack().pop();
    }

    /**
     * Prepares the given {@link ModelRenderer} for render by setting its translation, position, and rotation values
     * based on the provided {@link AzBone}
     *
     * @param context
     * @param bone       The AzBone to base the translations on
     * @param sourcePart The ModelRenderer to translate
     */
    protected void prepModelPartForRender(AzRendererPipelineContext<T> context, AzBone bone, ModelRenderer sourcePart) {
        GeoCube firstCube = bone.getCubes().get(0);
        ModelRenderer.ModelBox armorCube = sourcePart.cubeList.get(0);
        double armorBoneSizeX = firstCube.size().getX();
        double armorBoneSizeY = firstCube.size().getY();
        double armorBoneSizeZ = firstCube.size().getZ();
        float actualArmorSizeX = Math.abs(armorCube.posX2 - armorCube.posX1);
        float actualArmorSizeY = Math.abs(armorCube.posY2 - armorCube.posY1);
        float actualArmorSizeZ = Math.abs(armorCube.posZ2 - armorCube.posZ1);
        float scaleX = (float) (armorBoneSizeX / actualArmorSizeX);
        float scaleY = (float) (armorBoneSizeY / actualArmorSizeY);
        float scaleZ = (float) (armorBoneSizeZ / actualArmorSizeZ);

        sourcePart.setRotationPoint(
            -(bone.getPivotX() - ((bone.getPivotX() * scaleX) - bone.getPivotX()) / scaleX),
            -(bone.getPivotY() - ((bone.getPivotY() * scaleY) - bone.getPivotY()) / scaleY),
            (bone.getPivotZ() - ((bone.getPivotZ() * scaleZ) - bone.getPivotZ()) / scaleZ)
        );

        sourcePart.rotateAngleX = -bone.getRotX();
        sourcePart.rotateAngleY = -bone.getRotY();
        sourcePart.rotateAngleZ = bone.getRotZ();

        context.poseStack().scale(scaleX, scaleY, scaleZ);
    }

    /**
     * Gets a cached resource path for the vanilla armor layer texture for this armor piece.
     * <p>
     * Equivalent to {@link net.minecraft.client.renderer.entity.layers.BipedArmorLayer#getArmorResource(Entity, ItemStack, EquipmentSlotType, String)}
     * HumanoidArmorLayer.getArmorLocation}
     */
    public ResourceLocation getVanillaArmorResource(
        Entity entity,
        ItemStack stack,
        EquipmentSlotType slot,
        String type
    ) {
        String domain = "minecraft";
        String path = ((ArmorItem) stack.getItem()).getArmorMaterial().getName();
        String[] materialNameSplit = path.split(":", 2);

        if (materialNameSplit.length > 1) {
            domain = materialNameSplit[0];
            path = materialNameSplit[1];
        }

        if (!type.isEmpty())
            type = "_" + type;

        String texture = String.format(
            "%s:textures/models/armor/%s_layer_%d%s.png",
            domain,
            path,
            (slot == EquipmentSlotType.LEGS ? 2 : 1),
            type
        );
        return ARMOR_PATH_CACHE.computeIfAbsent(texture, ResourceLocation::new);
    }
}
