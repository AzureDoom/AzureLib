package mod.azure.azurelib.testing.entity;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.render.entity.AzEntityRenderer;
import mod.azure.azurelib.rewrite.render.entity.AzEntityRendererConfig;
import mod.azure.azurelib.rewrite.render.layer.AzAutoGlowingLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MarauderRenderer extends AzEntityRenderer<MarauderEntity> {

    private static final ResourceLocation MODEL = AzureLib.modResource("geo/entity/marauder.geo.json");

    private static final ResourceLocation TEXTURE = AzureLib.modResource("textures/entity/marauder.png");

    public MarauderRenderer(EntityRendererProvider.Context context) {
        super(
            AzEntityRendererConfig.<MarauderEntity>builder(MODEL, TEXTURE)
                .addRenderLayer(new AzAutoGlowingLayer<>())
                .setAnimatorProvider(MarauderAnimator::new)
                .setDeathMaxRotation(0F)
                .build(),
            context
        );
    }
}
