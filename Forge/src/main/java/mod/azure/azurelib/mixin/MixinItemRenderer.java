package mod.azure.azurelib.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Render hook to inject AzureLib's ISTER rendering callback
 */
@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"), cancellable = true)
	public void azurelib$itemModelHook(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean p_115146_, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int p_115150_, BakedModel p_115151_, CallbackInfo ci) {
//		var item = itemStack.getItem();
//		var renderer = AzItemRendererRegistry.getOrNull(item);
//
//		if (renderer != null) {
//			switch (transformType) {
//				case GUI -> renderer.renderByGui(itemStack, poseStack, multiBufferSource, i);
//				default -> renderer.renderByItem(itemStack, poseStack, multiBufferSource, i);
//			}
//		}
	}
}
