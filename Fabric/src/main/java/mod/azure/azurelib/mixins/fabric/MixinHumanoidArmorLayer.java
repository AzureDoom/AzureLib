package mod.azure.azurelib.mixins.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.azurelib.animatable.client.RenderProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = HumanoidArmorLayer.class, priority = 700)
public abstract class MixinHumanoidArmorLayer {
	@Unique
	private LivingEntity gl_storedEntity;
	@Unique
	private EquipmentSlot gl_storedSlot;
	@Unique
	private ItemStack gl_storedItemStack;

	@Inject(method = "renderArmorPiece", at = @At(value = "HEAD"))
	public void armorModelHook(PoseStack poseStack, MultiBufferSource multiBufferSource, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int i, HumanoidModel<LivingEntity> humanoidModel, CallbackInfo ci) {
		this.gl_storedEntity = livingEntity;
		this.gl_storedSlot = equipmentSlot;
		this.gl_storedItemStack = livingEntity.getItemBySlot(equipmentSlot);
	}

	@ModifyArg(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/HumanoidModel;ZFFFLjava/lang/String;)V"), index = 4)
	public HumanoidModel<LivingEntity> injectArmor(HumanoidModel<LivingEntity> humanoidModel) {
		return (HumanoidModel<LivingEntity>) RenderProvider.of(this.gl_storedItemStack).getGenericArmorModel(this.gl_storedEntity, this.gl_storedItemStack, this.gl_storedSlot, (HumanoidModel<LivingEntity>) humanoidModel);
	}
}
