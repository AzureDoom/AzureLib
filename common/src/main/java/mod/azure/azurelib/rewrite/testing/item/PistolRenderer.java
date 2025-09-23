package mod.azure.azurelib.rewrite.testing.item;

import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.rewrite.render.item.AzItemRenderer;
import mod.azure.azurelib.rewrite.render.item.AzItemRendererConfig;

public class PistolRenderer extends AzItemRenderer {

    private static final ResourceLocation MODEL = AzureLib.modResource("geo/item/pistol.geo.json");

    private static final ResourceLocation TEXTURE = AzureLib.modResource("textures/item/pistol.png");

    public PistolRenderer() {
        super(
            AzItemRendererConfig.builder(itemStack -> MODEL, itemStack -> TEXTURE)
                .setAnimatorProvider(PistolAnimator::new)
                .build()
        );
    }
}
