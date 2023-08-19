package mod.azure.azurelib.renderer.layer;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.ModelRenderer.ModelBox;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.ForgeHooksClient;

/**
 * Builtin class for handling dynamic armor rendering on AzureLib entities.<br>
 * Supports both {@link mod.azure.azurelib.animatable.GeoItem AzureLib} and {@link net.minecraft.world.item.ArmorItem Vanilla} armor models.<br>
 * Unlike a traditional armor renderer, this renderer renders per-bone, giving much more flexible armor rendering.
 */
public class ItemArmorGeoLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {
	protected static final Map<String, ResourceLocation> ARMOR_PATH_CACHE = new Object2ObjectOpenHashMap<>();
	protected static final BipedModel<LivingEntity> INNER_ARMOR_MODEL = new BipedModel<>(0.5F);
	protected static final BipedModel<LivingEntity> OUTER_ARMOR_MODEL = new BipedModel<>(1.0F);

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
	@Nonnull
	protected EquipmentSlotType getEquipmentSlotForBone(GeoBone bone, ItemStack stack, T animatable) {
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			if (slot.getType() == EquipmentSlotType.Group.ARMOR) {
				if (stack == animatable.getItemBySlot(slot))
					return slot;
			}
		}

		return EquipmentSlotType.CHEST;
	}

	/**
	 * Return a ModelRenderer for a given {@link GeoBone}.<br>
	 * This is then transformed into position for the final render
	 */
	@Nonnull
	protected ModelRenderer getModelPartForBone(GeoBone bone, EquipmentSlotType slot, ItemStack stack, T animatable, BipedModel<?> baseModel) {
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
	 * This method is called by the {@link GeoRenderer} before rendering, immediately after {@link GeoRenderer#preRender} has been called.<br>
	 * This allows for RenderLayers to perform pre-render manipulations such as hiding or showing bones
	 */
	@Override
	public void preRender(MatrixStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay) {
		this.mainHandStack = animatable.getItemBySlot(EquipmentSlotType.MAINHAND);
		this.offhandStack = animatable.getItemBySlot(EquipmentSlotType.OFFHAND);
		this.helmetStack = animatable.getItemBySlot(EquipmentSlotType.HEAD);
		this.chestplateStack = animatable.getItemBySlot(EquipmentSlotType.CHEST);
		this.leggingsStack = animatable.getItemBySlot(EquipmentSlotType.LEGS);
		this.bootsStack = animatable.getItemBySlot(EquipmentSlotType.FEET);
	}

	/**
	 * This method is called by the {@link GeoRenderer} for each bone being rendered.<br>
	 * This is a more expensive call, particularly if being used to render something on a different buffer.<br>
	 * It does however have the benefit of having the matrix translations and other transformations already applied from render-time.<br>
	 * It's recommended to avoid using this unless necessary.<br>
	 * <br>
	 * The {@link GeoBone} in question has already been rendered by this stage.<br>
	 * <br>
	 * If you <i>do</i> use it, and you render something that changes the {@link IVertexBuilder buffer}, you need to reset it back to the previous buffer using {@link IRenderTypeBuffer #getBuffer} before ending the method
	 */
	@Override
	public void renderForBone(MatrixStack poseStack, T animatable, GeoBone bone, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay) {
		ItemStack armorStack = getArmorItemForBone(bone, animatable);

		if (armorStack == null)
			return;

		if (armorStack.getItem() instanceof BlockItem && ((BlockItem) armorStack.getItem()).getBlock() instanceof AbstractSkullBlock) {
			renderSkullAsArmor(poseStack, bone, armorStack, ((AbstractSkullBlock) ((BlockItem) armorStack.getItem()).getBlock()), bufferSource, packedLight);
		} else {
			EquipmentSlotType slot = getEquipmentSlotForBone(bone, armorStack, animatable);
			BipedModel<?> model = getModelForItem(bone, slot, armorStack, animatable);
			ModelRenderer modelPart = getModelPartForBone(bone, slot, armorStack, animatable, model);

			if (!modelPart.cubes.isEmpty()) {
				poseStack.pushPose();
				poseStack.scale(-1, -1, 1);

				if (model instanceof GeoArmorRenderer<?>) {
					prepModelPartForRender(poseStack, bone, modelPart);
					((GeoArmorRenderer<?>) model).prepForRender(animatable, armorStack, slot, model);
					((GeoArmorRenderer<?>) model).setAllVisible(false);
					((GeoArmorRenderer<?>) model).applyBoneVisibilityByPart(slot, modelPart, model);
					((GeoArmorRenderer<?>) model).renderToBuffer(poseStack, null, packedLight, packedOverlay, 1, 1, 1, 1);
				} else if (armorStack.getItem() instanceof ArmorItem) {
					prepModelPartForRender(poseStack, bone, modelPart);
					renderVanillaArmorPiece(poseStack, animatable, bone, slot, armorStack, modelPart, bufferSource, partialTick, packedLight, packedOverlay);
				}

				poseStack.popPose();
			}
		}

		buffer = bufferSource.getBuffer(renderType);
	}

	/**
	 * Renders an individual armor piece base on the given {@link GeoBone} and {@link ItemStack}
	 */
	protected <I extends Item & GeoItem> void renderVanillaArmorPiece(MatrixStack poseStack, T animatable, GeoBone bone, EquipmentSlotType slot, ItemStack armorStack, ModelRenderer modelPart, IRenderTypeBuffer bufferSource, float partialTick, int packedLight, int packedOverlay) {
		ResourceLocation texture = getVanillaArmorResource(animatable, armorStack, slot, "");
		IVertexBuilder buffer = getArmorBuffer(bufferSource, null, texture, armorStack.hasFoil());

		if (armorStack.getItem() instanceof DyeableArmorItem) {
			int color = ((DyeableArmorItem) armorStack.getItem()).getColor(armorStack);

			modelPart.render(poseStack, buffer, packedLight, packedOverlay, (color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, 1);

			texture = getVanillaArmorResource(animatable, armorStack, slot, "overlay");
			buffer = getArmorBuffer(bufferSource, null, texture, false);
		}

		modelPart.render(poseStack, buffer, packedLight, packedOverlay, 1, 1, 1, 1);
	}

	/**
	 * Returns the standard IVertexBuilder for armor rendering from the given buffer source.
	 * 
	 * @param bufferSource The BufferSource to draw the buffer from
	 * @param renderType   The RenderType to use for rendering, or null to use the default
	 * @param texturePath  The texture path for the render. May be null if renderType is not null
	 * @param enchanted    Whether the render should have an enchanted glint or not
	 * @return The buffer to draw to
	 */
	protected IVertexBuilder getArmorBuffer(IRenderTypeBuffer bufferSource, @Nullable RenderType renderType, @Nullable ResourceLocation texturePath, boolean enchanted) {
		if (renderType == null)
			renderType = RenderType.armorCutoutNoCull(texturePath);

		return ItemRenderer.getArmorFoilBuffer(bufferSource, renderType, false, enchanted);
	}

	/**
	 * Returns a cached instance of a base HumanoidModel that is used for rendering/modelling the provided {@link ItemStack}
	 */
	@Nonnull
	protected BipedModel<?> getModelForItem(GeoBone bone, EquipmentSlotType slot, ItemStack stack, T animatable) {
		BipedModel<?> defaultModel = slot == EquipmentSlotType.LEGS ? INNER_ARMOR_MODEL : OUTER_ARMOR_MODEL;

		return stack.getItem().getArmorModel(null, stack, null, defaultModel);
	}

	/**
	 * Gets a cached resource path for the vanilla armor layer texture for this armor piece.<br>
	 * Equivalent to {@link net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#getArmorLocation HumanoidArmorLayer.getArmorLocation}
	 */
	public ResourceLocation getVanillaArmorResource(Entity entity, ItemStack stack, EquipmentSlotType slot, String type) {
		String domain = "minecraft";
		String path = ((ArmorItem) stack.getItem()).getMaterial().getName();
		String[] materialNameSplit = path.split(":", 2);

		if (materialNameSplit.length > 1) {
			domain = materialNameSplit[0];
			path = materialNameSplit[1];
		}

		if (!type.isEmpty())
			type = "_" + type;

		String texture = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, path, (slot == EquipmentSlotType.LEGS ? 2 : 1), type);
		texture = ForgeHooksClient.getArmorTexture(entity, stack, texture, slot, type);

		return ARMOR_PATH_CACHE.computeIfAbsent(texture, ResourceLocation::new);
	}

	/**
	 * Render a given {@link AbstractSkullBlock} as a worn armor piece in relation to a given {@link GeoBone}
	 */
	protected void renderSkullAsArmor(MatrixStack poseStack, GeoBone bone, ItemStack stack, AbstractSkullBlock skullBlock, IRenderTypeBuffer bufferSource, int packedLight) {
		GameProfile skullProfile = null;

		if (stack.hasTag()) {
			CompoundNBT compoundnbt = stack.getTag();
			if (compoundnbt.contains("SkullOwner", 10)) {
				skullProfile = NBTUtil.readGameProfile(compoundnbt.getCompound("SkullOwner"));
			} else if (compoundnbt.contains("SkullOwner", 8)) {
				String s = compoundnbt.getString("SkullOwner");
				if (!StringUtils.isNullOrEmpty(s)) {
					skullProfile = SkullTileEntity.updateGameprofile(new GameProfile((UUID) null, s));
					compoundnbt.put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), skullProfile));
				}
			}
		}

		poseStack.pushPose();
		RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);
		poseStack.scale(1.1875f, 1.1875f, 1.1875f);
		poseStack.translate(-0.5f, 0, -0.5f);
		SkullTileEntityRenderer.renderSkull((Direction) null, 0.0F, ((AbstractSkullBlock) ((BlockItem) stack.getItem()).getBlock()).getType(), skullProfile, 0F /* limbswing, controls rotation */, poseStack, bufferSource, packedLight);
		poseStack.popPose();
	}

	/**
	 * Prepares the given {@link ModelRenderer} for render by setting its translation, position, and rotation values based on the provided {@link GeoBone}
	 * 
	 * @param poseStack  The MatrixStack being used for rendering
	 * @param bone       The GeoBone to base the translations on
	 * @param sourcePart The ModelRenderer to translate
	 */
	protected void prepModelPartForRender(MatrixStack poseStack, GeoBone bone, ModelRenderer sourcePart) {
		final GeoCube firstCube = bone.getCubes().get(0);
		final ModelBox armorCube = sourcePart.cubes.get(0);
		final double armorBoneSizeX = firstCube.size().x();
		final double armorBoneSizeY = firstCube.size().y();
		final double armorBoneSizeZ = firstCube.size().z();
		final double actualArmorSizeX = Math.abs(armorCube.maxX - armorCube.minX);
		final double actualArmorSizeY = Math.abs(armorCube.maxY - armorCube.minY);
		final double actualArmorSizeZ = Math.abs(armorCube.maxZ - armorCube.minZ);
		float scaleX = (float) (armorBoneSizeX / actualArmorSizeX);
		float scaleY = (float) (armorBoneSizeY / actualArmorSizeY);
		float scaleZ = (float) (armorBoneSizeZ / actualArmorSizeZ);

		sourcePart.setPos(-(bone.getPivotX() - ((bone.getPivotX() * scaleX) - bone.getPivotX()) / scaleX), -(bone.getPivotY() - ((bone.getPivotY() * scaleY) - bone.getPivotY()) / scaleY), (bone.getPivotZ() - ((bone.getPivotZ() * scaleZ) - bone.getPivotZ()) / scaleZ));

		sourcePart.xRot = -bone.getRotX();
		sourcePart.yRot = -bone.getRotY();
		sourcePart.zRot = bone.getRotZ();

		poseStack.scale(scaleX, scaleY, scaleZ);
	}
}
