package mod.azure.azurelib.testing.armor;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererConfig;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class DoomicornArmorRenderer extends AzArmorRenderer {

    private static final ResourceLocation MODEL = AzureLib.modResource("geo/item/doomicorn.geo.json");

    private static final ResourceLocation TEXTURE = AzureLib.modResource("textures/item/doomicorn.png");

    public DoomicornArmorRenderer() {
        super(
            AzArmorRendererConfig.builder(MODEL, TEXTURE)
                .setAnimatorProvider(DoomicornArmorAnimator::new)
                .setBoneProvider(new DoomArmorBoneProvider())
                .setRenderType(RenderType.entityTranslucent(TEXTURE))
                .build()
        );
    }
}
