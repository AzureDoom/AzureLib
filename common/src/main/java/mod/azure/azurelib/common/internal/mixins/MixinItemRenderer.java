/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.common.render.item.AzItemRendererRegistry;

/**
 * Render hook to inject AzureLib's ISTER rendering callback
 */
@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(
        method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
        ), cancellable = true
    )
    public void itemModelHook(
        ItemStack itemStack,
        ItemDisplayContext transformType,
        boolean bl,
        PoseStack poseStack,
        MultiBufferSource multiBufferSource,
        int i,
        int j,
        BakedModel bakedModel,
        CallbackInfo ci
    ) {
        var item = itemStack.getItem();
        var renderer = AzItemRendererRegistry.getOrNull(item);

        if (renderer != null) {
            switch (transformType) {
                case GUI -> renderer.renderByGui(itemStack, transformType, poseStack, multiBufferSource, i);
                default -> renderer.renderByItem(itemStack, transformType, poseStack, multiBufferSource, i);
            }
        }
    }
}
