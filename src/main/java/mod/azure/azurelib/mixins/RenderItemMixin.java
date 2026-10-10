package mod.azure.azurelib.mixins;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.render.item.AzItemRenderHooks;
import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;

/**
 * Render hook to inject AzureLib's item rendering. Every 1.12.2 item render (hand, GUI, ground, item frame, head)
 * funnels through {@code RenderItem#renderItem(ItemStack, IBakedModel)} after Forge has applied the model's camera
 * transforms, which is the same point 1.18 hands control to {@code BlockEntityWithoutLevelRenderer}.
 */
@Mixin(RenderItem.class)
public abstract class RenderItemMixin {

    @Inject(
        method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void azurelib$itemModelHook(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }

        AzItemRenderer renderer = AzItemRendererRegistry.getOrNull(stack.getItem());

        if (renderer == null) {
            return;
        }

        GlStateManager.pushMatrix();
        // Same centering vanilla applies before calling a TileEntityItemStackRenderer.
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        renderer.render(stack, AzItemRenderHooks.consumeTransformType());
        GlStateManager.popMatrix();
        ci.cancel();
    }
}
