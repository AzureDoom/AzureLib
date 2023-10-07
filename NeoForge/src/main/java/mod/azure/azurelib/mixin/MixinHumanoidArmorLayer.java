package mod.azure.azurelib.mixin;

import java.util.Map;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = HumanoidArmorLayer.class, priority = 700)
public abstract class MixinHumanoidArmorLayer<T extends LivingEntity, A extends HumanoidModel<T>> extends RenderLayer<T, A> {

	public MixinHumanoidArmorLayer(RenderLayerParent<T, A> p_117346_) {
		super(p_117346_);
	}

	@Shadow
	private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();

	@Shadow
	abstract A getArmorModel(EquipmentSlot equipmentSlot);

	@Shadow
	abstract net.minecraft.client.model.Model getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model);

	@Shadow
	abstract void setPartVisibility(A humanoidModel, EquipmentSlot slot);

	@Shadow
	public abstract ResourceLocation getArmorResource(net.minecraft.world.entity.Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type);

	@Shadow
	abstract void renderModel(PoseStack p_289664_, MultiBufferSource p_289689_, int p_289681_, ArmorItem p_289650_, net.minecraft.client.model.Model p_289658_, boolean p_289668_, float p_289678_, float p_289674_, float p_289693_, ResourceLocation armorResource);

	@Shadow
	abstract void renderGlint(PoseStack p_289673_, MultiBufferSource p_289654_, int p_289649_, net.minecraft.client.model.Model p_289659_);

	@Shadow
	abstract void renderTrim(ArmorMaterial p_289690_, PoseStack p_289687_, MultiBufferSource p_289643_, int p_289683_, ArmorTrim p_289692_, net.minecraft.client.model.Model p_289663_, boolean p_289651_);

	@Overwrite
	private void renderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel) {
		var itemstack = livingEntity.getItemBySlot(equipmentSlot);
		var item = itemstack.getItem();
		if (item instanceof ArmorItem armoritem) {
			if (armoritem.getEquipmentSlot() == equipmentSlot) {
				this.getParentModel().copyPropertiesTo(humanoidModel);
				this.setPartVisibility(humanoidModel, equipmentSlot);
				var model = getArmorModelHook(livingEntity, itemstack, equipmentSlot, humanoidModel);
				if (armoritem instanceof GeoItem)
					this.al_renderModel(poseStack, multiBufferSource, i, armoritem, (A) RenderProvider.of(itemstack).getGenericArmorModel(livingEntity, itemstack, equipmentSlot, (HumanoidModel<LivingEntity>) getArmorModelHook(livingEntity, livingEntity.getItemBySlot(equipmentSlot), equipmentSlot, humanoidModel)), equipmentSlot == EquipmentSlot.LEGS, 1.0F, 1.0F, 1.0F, null);
				else if (armoritem instanceof DyeableLeatherItem dyeItem) {
					var j = dyeItem.getColor(itemstack);
					var f = (float) (j >> 16 & 255) / 255.0F;
					var f1 = (float) (j >> 8 & 255) / 255.0F;
					var f2 = (float) (j & 255) / 255.0F;
					this.renderModel(poseStack, multiBufferSource, i, armoritem, model, equipmentSlot == EquipmentSlot.LEGS, f, f1, f2, this.getArmorResource(livingEntity, itemstack, equipmentSlot, null));
					this.renderModel(poseStack, multiBufferSource, i, armoritem, model, equipmentSlot == EquipmentSlot.LEGS, 1.0F, 1.0F, 1.0F, this.getArmorResource(livingEntity, itemstack, equipmentSlot, "overlay"));
				} else
					this.renderModel(poseStack, multiBufferSource, i, armoritem, model, equipmentSlot == EquipmentSlot.LEGS, 1.0F, 1.0F, 1.0F, this.getArmorResource(livingEntity, itemstack, equipmentSlot, null));

				ArmorTrim.getTrim(livingEntity.level().registryAccess(), itemstack).ifPresent((trim) -> {
					this.renderTrim(armoritem.getMaterial(), poseStack, multiBufferSource, i, trim, model, equipmentSlot == EquipmentSlot.LEGS);
				});
				if (itemstack.hasFoil())
					this.renderGlint(poseStack, multiBufferSource, i, model);
			}
		}
	}

	private void al_renderModel(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorItem armorItem, A humanoidModel, boolean bl, float f, float g, float h, @Nullable String string) {
		((AgeableListModel) humanoidModel).renderToBuffer(poseStack, null, i, OverlayTexture.NO_OVERLAY, f, g, h, 1.0f);
	}
}
