package mod.azure.azurelib.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.rewrite.render.item.AzItemRenderer;
import mod.azure.azurelib.rewrite.render.item.AzItemRendererRegistry;

/**
 * Render hook to inject AzureLib's ISTER rendering callback
 */
@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(
        method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/tileentity/ItemStackTileEntityRenderer;renderByItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;II)V"
        ), cancellable = true
    )
    public void azurelib$itemModelHook(
        ItemStack itemStack,
        ItemCameraTransforms.TransformType transformType,
        boolean bl,
        MatrixStack poseStack,
        IRenderTypeBuffer multiBufferSource,
        int i,
        int j,
        IBakedModel bakedModel,
        CallbackInfo ci
    ) {
        if (itemStack.getItem() instanceof GeoItem)
            RenderProvider.of(itemStack)
                .getCustomRenderer()
                .renderByItem(itemStack, transformType, poseStack, multiBufferSource, i, j);

        Item item = itemStack.getItem();
        AzItemRenderer renderer = AzItemRendererRegistry.getOrNull(item);

        if (renderer != null) {
            if (transformType == ItemCameraTransforms.TransformType.GUI) {
                renderer.renderByGui(itemStack, poseStack, multiBufferSource, i);
            } else {
                renderer.renderByItem(itemStack, poseStack, multiBufferSource, i);
            }
        }
    }
}
