/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.mixins.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

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
            target = "Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
        ), cancellable = true
    )
    public void azurelib$itemModelHook(
        ItemStack itemStack,
        ItemTransforms.TransformType transformType,
        boolean bl,
        PoseStack poseStack,
        MultiBufferSource multiBufferSource,
        int i,
        int j,
        BakedModel bakedModel,
        CallbackInfo ci
    ) {
        // TODO: Remove this along with Geo-code.
        if (itemStack.getItem() instanceof GeoItem) {
            RenderProvider.of(itemStack)
                .getCustomRenderer()
                .renderByItem(itemStack, transformType, poseStack, multiBufferSource, i, j);
        }

        Item item = itemStack.getItem();
        AzItemRenderer renderer = AzItemRendererRegistry.getOrNull(item);

        if (renderer != null) {
            if (Objects.requireNonNull(transformType) == ItemTransforms.TransformType.GUI) {
                renderer.renderByGui(itemStack, poseStack, multiBufferSource, i);
            } else {
                renderer.renderByItem(itemStack, poseStack, multiBufferSource, i);
            }
        }
    }
}
