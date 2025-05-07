package mod.azure.azurelib.testing.block.be;

import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.render.block.AzBlockEntityRenderer;
import mod.azure.azurelib.rewrite.render.block.AzBlockEntityRendererConfig;

public class StargateBlockRenderer extends AzBlockEntityRenderer<StargateBlockEntity> {

    private static final ResourceLocation MODEL = AzureLib.modResource("geo/block/stargate.geo.json");

    private static final ResourceLocation TEXTURE = AzureLib.modResource("textures/block/stargate.png");

    public StargateBlockRenderer() {
        super(
            AzBlockEntityRendererConfig.<StargateBlockEntity>builder(MODEL, TEXTURE)
                .setAnimatorProvider(StargateBlockEntityAnimator::new)
                .build()
        );
    }
}
