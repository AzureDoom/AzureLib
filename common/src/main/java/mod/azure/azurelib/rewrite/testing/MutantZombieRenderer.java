package mod.azure.azurelib.rewrite.testing;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.rewrite.render.entity.AzEntityRenderer;
import mod.azure.azurelib.rewrite.render.entity.AzEntityRendererConfig;

public class MutantZombieRenderer extends AzEntityRenderer<MutantZombieEntity> {

    private static final ResourceLocation MODEL = AzureLib.modResource("geo/entity/mutant_zombie.geo.json");

    private static final ResourceLocation TEXTURE = AzureLib.modResource("textures/entity/mutant_zombie.png");

    public MutantZombieRenderer(EntityRendererProvider.Context context) {
        super(
            AzEntityRendererConfig.<MutantZombieEntity>builder(MODEL, TEXTURE)
                .addRenderLayer(new MutantZombieBlockItemLayer())
                .addRenderLayer(new MutantZombieArmorLayer())
                .setShadowRadius(0.5F)
                .build(),
            context
        );
    }
}
