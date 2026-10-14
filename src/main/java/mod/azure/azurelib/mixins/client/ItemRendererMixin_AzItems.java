package mod.azure.azurelib.mixins.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.render.item.AzItemRendererAdapter;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.util.client.AzRenderTick;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin_AzItems {

    @Inject(
        method = "render(Lnet/minecraft/entity/ItemEntity;DDDFF)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void azurelib$renderDroppedAzItem(
        ItemEntity entity,
        double x,
        double y,
        double z,
        float yaw,
        float tickDelta,
        CallbackInfo ci
    ) {
        ItemStack stack = Mc.itemEntityStack(entity);
        AzItemRendererAdapter adapter = AzItemRendererRegistry.adapterOrNull(Mc.item(stack));
        if (adapter == null || !adapter.handles(AzItemRendererAdapter.ItemRenderType.ENTITY)) {
            return;
        }
        AzRenderTick.set(tickDelta);

        // Beta's (and 1.7.10's) bob and spin, followed by the 0.5 scale Forge applied to flat custom items.
        float time = Mc.age(entity) + tickDelta;
        float hover = Mc.itemEntityHoverStart(entity);
        float bob = (float) Math.sin(time / 10.0F + hover) * 0.1F + 0.1F;
        float spin = (time / 20.0F + hover) * (180.0F / (float) Math.PI);

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate((float) x, (float) y + bob, (float) z);
            GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            adapter.renderItem(AzItemRendererAdapter.ItemRenderType.ENTITY, stack);
        } finally {
            GlStateManager.popMatrix();
        }
        ci.cancel();
    }

    @Inject(
        method = "renderGuiItem(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/client/texture/TextureManager;Lnet/minecraft/item/ItemStack;II)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void azurelib$renderGuiAzItem(
        TextRenderer textRenderer,
        TextureManager textureManager,
        ItemStack stack,
        int x,
        int y,
        CallbackInfo ci
    ) {
        AzItemRendererAdapter adapter = AzItemRendererRegistry.adapterOrNull(Mc.item(stack));
        if (adapter == null || !adapter.handles(AzItemRendererAdapter.ItemRenderType.INVENTORY)) {
            return;
        }
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate((float) x, (float) y, 50.0F);
            adapter.renderItem(AzItemRendererAdapter.ItemRenderType.INVENTORY, stack);
        } finally {
            GlStateManager.popMatrix();
        }
        AzItemRendererAdapter.resetGuiDepth(x, y);
        ci.cancel();
    }
}
