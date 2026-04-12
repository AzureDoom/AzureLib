package mod.azure.azurelib.render.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

import java.util.UUID;
import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.AzArmorRendererPipelineContext;

/**
 * Represents a render layer for applying armor trim textures to an item stack during the rendering process. This layer
 * enables dynamic customization of armor visuals by applying trim patterns and materials based on the item's metadata
 * and associated {@link ArmorTrim}.
 *
 * @author ZsoltMolnarrr
 */
public class AzArmorTrimLayer implements AzRenderLayer<UUID, ItemStack> {

    public final ResourceLocation textureBaseLocation;

    public final Function<ArmorTrim, ResourceLocation> texturePermutations;

    public AzArmorTrimLayer(ResourceLocation baseTexture) {
        this(baseTexture, true);
    }

    public AzArmorTrimLayer(ResourceLocation baseTexture, boolean supportPatterns) {
        this(
            baseTexture,
            supportPatterns
                ? trim -> {
                    var pattern = trim.pattern().value();
                    var material = trim.material().value();
                    var patternName = pattern.assetId().getPath();
                    return new ResourceLocation(
                        baseTexture.getNamespace(),
                        baseTexture.getPath() + "_" + patternName + "_" + material.assetName()
                    );
                }
                : trim -> {
                    var material = trim.material().value();
                    return new ResourceLocation(
                        baseTexture.getNamespace(),
                        baseTexture.getPath() + "_" + material.assetName()
                    );
                }
        );
    }

    public AzArmorTrimLayer(
        ResourceLocation baseTexture,
        Function<ArmorTrim, ResourceLocation> textureLocationPermutations
    ) {
        this.textureBaseLocation = baseTexture;
        this.texturePermutations = textureLocationPermutations;
    }

    @Override
    public void preRender(AzRendererPipelineContext<UUID, ItemStack> context) {}

    @Override
    public void render(AzRendererPipelineContext<UUID, ItemStack> context) {
        var armorPipelineContext = (AzArmorRendererPipelineContext) context;
        var itemstack = armorPipelineContext.currentStack();
        if (itemstack == null) {
            return;
        }
        var currentEntity = armorPipelineContext.currentEntity();
        if (currentEntity == null) {
            return;
        }

        ArmorTrim.getTrim(currentEntity.level().registryAccess(), itemstack)
            .ifPresent(armorTrim -> {
                var bakery = Minecraft.getInstance().getModelManager();
                var armorTrimsAtlas = bakery.getAtlas(Sheets.ARMOR_TRIMS_SHEET);

                var renderPipeline = context.rendererPipeline();
                var trimLocation = texturePermutations.apply(armorTrim);

                var sprite = armorTrimsAtlas.getSprite(trimLocation);
                var renderType = Sheets.armorTrimsSheet();
                var vertexConsumer = sprite.wrap(
                    ItemRenderer.getArmorFoilBuffer(context.multiBufferSource(), renderType, false, itemstack.hasFoil())
                );

                var prevRenderType = context.renderType();
                var prevVertexConsumer = context.vertexConsumer();

                if (context.renderType() != null) {
                    context.setRenderType(renderType);
                    context.setVertexConsumer(vertexConsumer);
                    renderPipeline.reRender(context);
                }

                context.setRenderType(prevRenderType);
                context.setVertexConsumer(prevVertexConsumer);
            });
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, ItemStack> azRendererPipelineContext, AzBone azBone) {}
}
