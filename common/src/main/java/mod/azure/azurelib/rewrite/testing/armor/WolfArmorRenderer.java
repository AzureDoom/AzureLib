package mod.azure.azurelib.rewrite.testing.armor;

import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererConfig;

public class WolfArmorRenderer extends AzArmorRenderer {

    private static final ResourceLocation MODEL = AzureLib.modResource("geo/armor/wolf_armor.geo.json");

    private static final ResourceLocation TEXTURE = AzureLib.modResource("textures/armor/wolf_armor.png");

    public WolfArmorRenderer() {
        super(
            AzArmorRendererConfig.builder(MODEL, TEXTURE)
                .build()
        );
    }
}
