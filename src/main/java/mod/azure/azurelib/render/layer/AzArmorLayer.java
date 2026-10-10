package mod.azure.azurelib.render.layer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.AzArmorModel;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererPipeline;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.OverlayTexture;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Builtin class for handling dynamic armor rendering on AzureLib entities.<br>
 * Supports both AzureLib and vanilla armor models.<br>
 * Unlike a traditional armor renderer, this renderer renders per-bone, giving much more flexible armor rendering.
 * <p>
 * Vanilla armor pieces and skulls are rendered by vanilla 1.12.2 code through the GL matrix stack, so pending AzureLib
 * vertices are flushed first and the bone's pose is pushed onto the GL matrix.
 */
public class AzArmorLayer<T extends EntityLivingBase> implements AzRenderLayer<UUID, T> {

    protected static final Map<String, ResourceLocation> ARMOR_PATH_CACHE = new HashMap<>();

    private static final ResourceLocation ENCHANTED_ITEM_GLINT = new ResourceLocation(
        "textures/misc/enchanted_item_glint.png"
    );

    protected static final ModelBiped INNER_ARMOR_MODEL = new ModelBiped(0.5F);

    protected static final ModelBiped OUTER_ARMOR_MODEL = new ModelBiped(1.0F);

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
        this.mainHandStack = animatable.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        this.offhandStack = animatable.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
        this.helmetStack = animatable.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        this.chestplateStack = animatable.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        this.leggingsStack = animatable.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        this.bootsStack = animatable.getItemStackFromSlot(EntityEquipmentSlot.FEET);
    }

    @Override
    public void render(AzRendererPipelineContext<UUID, T> context) {}

    /**
     * Renders an individual armor piece base on the given bone and its parent pipeline context
     */
    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, T> context, AzBone bone) {
        ItemStack armorStack = getArmorItemForBone(context, bone);

        if (armorStack == null || armorStack.isEmpty()) {
            return;
        }

        context.poseStack().pushPose();

        if (armorStack.getItem() instanceof ItemSkull) {
            renderSkullAsArmor(context, bone, armorStack);
        } else {
            renderArmor(context, bone, armorStack);
        }

        context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));
        context.poseStack().popPose();
    }

    public void renderArmor(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ItemStack armorStack
    ) {
        EntityEquipmentSlot slot = getEquipmentSlotForBone(context, bone, armorStack);
        AzArmorRenderer renderer = AzArmorRendererRegistryHolder.get(armorStack);
        ModelBiped model = getModelForItem(armorStack, slot);
        ModelRenderer modelPart = getModelPartForBone(context, bone, model);

        if (modelPart.cubeList.isEmpty()) {
            return;
        }

        context.poseStack().pushPose();
        context.poseStack().scale(-1, -1, 1);

        if (renderer != null) {
            prepModelPartForRender(context, bone, modelPart);
            renderAzArmorPiece(context, slot, armorStack, renderer, context.animatable(), model, modelPart);
        } else if (armorStack.getItem() instanceof ItemArmor) {
            prepModelPartForRender(context, bone, modelPart);
            renderVanillaArmorPiece(context, bone, slot, armorStack, modelPart);
        }

        context.poseStack().popPose();
    }

    /**
     * Return an EquipmentSlot for a given {@link ItemStack} and animatable instance.<br>
     * This is what determines the base model to use for rendering a particular stack
     */
    protected @Nonnull EntityEquipmentSlot getEquipmentSlotForBone(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ItemStack stack
    ) {
        T animatable = context.animatable();

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (
                slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && stack == animatable.getItemStackFromSlot(slot)
            ) {
                return slot;
            }
        }

        return EntityEquipmentSlot.CHEST;
    }

    /**
     * Return a ModelRenderer for a given {@link AzBone}.<br>
     * This is then transformed into position for the final render
     */
    @Nonnull
    protected ModelRenderer getModelPartForBone(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ModelBiped baseModel
    ) {
        return baseModel.bipedBody;
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
        EntityEquipmentSlot slot,
        ItemStack armorStack,
        AzArmorRenderer renderer,
        EntityLivingBase entity,
        ModelBiped model,
        ModelRenderer modelPart
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
     * Renders a vanilla (non-AzureLib) armor piece through 1.12.2's GL model rendering.
     */
    protected void renderVanillaArmorPiece(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        EntityEquipmentSlot slot,
        ItemStack armorStack,
        ModelRenderer modelPart
    ) {
        flush(context.multiBufferSource());

        Minecraft mc = Minecraft.getMinecraft();
        ItemArmor armorItem = (ItemArmor) armorStack.getItem();
        int packedLight = context.packedLight();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, packedLight & 0xFFFF, packedLight >> 16);
        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(context.poseStack().last());

        mc.getTextureManager()
            .bindTexture(getVanillaArmorResource(context.animatable(), armorStack, slot, null));

        if (armorItem.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
            int color = armorItem.getColor(armorStack);
            GlStateManager.color(
                (color >> 16 & 255) / 255.0F * context.red(),
                (color >> 8 & 255) / 255.0F * context.green(),
                (color & 255) / 255.0F * context.blue(),
                context.alpha()
            );
            modelPart.render(0.0625F);
            mc.getTextureManager()
                .bindTexture(getVanillaArmorResource(context.animatable(), armorStack, slot, "overlay"));
        }

        GlStateManager.color(context.red(), context.green(), context.blue(), context.alpha());
        modelPart.render(0.0625F);

        if (armorStack.hasEffect()) {
            renderVanillaGlint(context.animatable(), modelPart);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    /**
     * Same effect as 1.12.2's {@code LayerArmorBase#renderEnchantedGlint}, limited to a single model part.
     */
    protected void renderVanillaGlint(Entity entity, ModelRenderer modelPart) {
        Minecraft mc = Minecraft.getMinecraft();
        float time = entity.ticksExisted + mc.getRenderPartialTicks();

        mc.getTextureManager().bindTexture(ENCHANTED_ITEM_GLINT);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.depthMask(false);
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

        for (int pass = 0; pass < 2; ++pass) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotate(30.0F - pass * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, time * (0.001F + pass * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            modelPart.render(0.0625F);
        }

        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
    }

    /**
     * Returns a cached instance of a base ModelBiped that is used for rendering/modelling the provided
     * {@link ItemStack}
     */
    @Nullable
    protected AzArmorRenderer getRendererForItem(ItemStack stack) {
        return AzArmorRendererRegistryHolder.get(stack);
    }

    protected ModelBiped getModelForItem(ItemStack stack, EntityEquipmentSlot slot) {
        AzArmorRenderer renderer = getRendererForItem(stack);

        if (renderer == null) {
            return slot == EntityEquipmentSlot.LEGS ? INNER_ARMOR_MODEL : OUTER_ARMOR_MODEL;
        }

        return renderer.rendererPipeline().armorModel();
    }

    /**
     * Render a given {@link ItemSkull} as a worn armor piece in relation to a given {@link AzBone}, following 1.12.2's
     * {@code LayerCustomHead}.
     */
    protected void renderSkullAsArmor(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ItemStack stack
    ) {
        GameProfile skullProfile = null;
        NBTTagCompound stackTag = stack.getTagCompound();

        if (stackTag != null) {
            if (stackTag.hasKey("SkullOwner", 10)) {
                skullProfile = NBTUtil.readGameProfileFromNBT(stackTag.getCompoundTag("SkullOwner"));
            } else if (stackTag.hasKey("SkullOwner", 8)) {
                String skullOwner = stackTag.getString("SkullOwner");

                if (!StringUtils.isBlank(skullOwner)) {
                    skullProfile = TileEntitySkull.updateGameprofile(new GameProfile(null, skullOwner));
                    stackTag.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), skullProfile));
                }
            }
        }

        flush(context.multiBufferSource());

        context.poseStack().pushPose();
        RenderUtils.translateAndRotateMatrixForBone(context.poseStack(), bone);
        context.poseStack().scale(1.1875f, 1.1875f, 1.1875f);
        context.poseStack().translate(-0.5f, 0, -0.5f);

        int packedLight = context.packedLight();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, packedLight & 0xFFFF, packedLight >> 16);
        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(context.poseStack().last());
        TileEntitySkullRenderer.instance.renderSkull(
            0.0F,
            0.0F,
            0.0F,
            EnumFacing.UP,
            0.0F,
            stack.getMetadata(),
            skullProfile,
            -1,
            0.0F
        );
        GlStateManager.popMatrix();
        context.poseStack().popPose();
    }

    /**
     * Prepares the given {@link ModelRenderer} for render by setting its translation, rotation, and scale to the
     * {@link AzBone}
     */
    protected void prepModelPartForRender(
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone,
        ModelRenderer sourcePart
    ) {
        GeoCube firstCube = bone.getCubes().get(0);
        ModelBox armorCube = sourcePart.cubeList.get(0);
        double armorBoneSizeX = firstCube.size().x();
        double armorBoneSizeY = firstCube.size().y();
        double armorBoneSizeZ = firstCube.size().z();
        double actualArmorSizeX = Math.abs(armorCube.posX2 - armorCube.posX1);
        double actualArmorSizeY = Math.abs(armorCube.posY2 - armorCube.posY1);
        double actualArmorSizeZ = Math.abs(armorCube.posZ2 - armorCube.posZ1);
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
     * Gets the texture for a vanilla armor piece, going through Forge's {@code getArmorTexture} hook so modded armor
     * textures resolve correctly.
     *
     * @param type {@code null} for the base layer or {@code "overlay"} for the dyed overlay
     */
    public ResourceLocation getVanillaArmorResource(
        Entity entity,
        ItemStack stack,
        EntityEquipmentSlot slot,
        @Nullable String type
    ) {
        ItemArmor item = (ItemArmor) stack.getItem();
        String texture = item.getArmorMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');

        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }

        String path = String.format(
            "%s:textures/models/armor/%s_layer_%d%s.png",
            domain,
            texture,
            (slot == EntityEquipmentSlot.LEGS ? 2 : 1),
            type == null ? "" : String.format("_%s", type)
        );

        path = ForgeHooksClient.getArmorTexture(entity, stack, path, slot, type);

        ResourceLocation location = ARMOR_PATH_CACHE.get(path);

        if (location == null) {
            location = new ResourceLocation(path);
            ARMOR_PATH_CACHE.put(path, location);
        }

        return location;
    }

    private static void flush(MultiBufferSource bufferSource) {
        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }
    }

    /** Indirection so armor renderers are only looked up by item. */
    private static final class AzArmorRendererRegistryHolder {

        @Nullable
        static AzArmorRenderer get(ItemStack stack) {
            return mod.azure.azurelib.render.armor.AzArmorRendererRegistry.getOrNull(stack.getItem());
        }
    }
}
