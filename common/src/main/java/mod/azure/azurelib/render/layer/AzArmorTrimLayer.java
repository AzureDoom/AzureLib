package mod.azure.azurelib.render.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.UUID;
import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.AzArmorRendererPipelineContext;

/**
 * Renders armor trims on AzureLib armor using vanilla's paletted textures (26.3+).
 * <p>
 * A trim texture is a grayscale image with a {@code palette} section in its {@code .png.mcmeta}. Vanilla's
 * {@link net.minecraft.client.resources.palette.PalettedTextureManager} recolors it with the trim material's palette at
 * runtime, so no atlas or permutation list is needed. The texture is sampled with the armor model's own UVs, so it
 * must have the same dimensions as the armor texture.
 * <p>
 * Texture ids are relative to {@code textures/} and have no {@code .png}, matching vanilla: {@code yourmod:trims/x}
 * means {@code assets/yourmod/textures/trims/x.png}.
 *
 * @author ZsoltMolnarrr
 */
public class AzArmorTrimLayer implements AzRenderLayer<UUID, ItemStack> {

    public static final Function<ArmorTrim, Identifier> MATERIAL_PALETTE = trim -> trim.material().value().paletteId();

    public final Function<ArmorTrim, Identifier> textureForTrim;

    public final Function<ArmorTrim, Identifier> paletteForTrim;

    /**
     * One texture per trim pattern: {@code <baseTexture>_<pattern>}, e.g. {@code yourmod:trims/your_armor_coast}.
     */
    public AzArmorTrimLayer(Identifier baseTexture) {
        this(baseTexture, true);
    }

    /**
     * @param supportPatterns {@code true} for one texture per pattern ({@code <baseTexture>_<pattern>}),
     *                        {@code false} to use {@code baseTexture} for every pattern
     */
    public AzArmorTrimLayer(Identifier baseTexture, boolean supportPatterns) {
        this(
                supportPatterns
                        ? trim -> baseTexture.withSuffix("_" + trim.pattern().value().assetId().getPath())
                        : trim -> baseTexture
        );
    }

    /**
     * Custom texture choice per trim, colored with the trim material's palette.
     */
    public AzArmorTrimLayer(Function<ArmorTrim, Identifier> textureForTrim) {
        this(textureForTrim, MATERIAL_PALETTE);
    }

    /**
     * Custom texture and palette choice per trim. Use the palette function for what vanilla does with
     * {@code trim_overrides}, such as a darker palette when the trim material matches the armor material.
     */
    public AzArmorTrimLayer(
            Function<ArmorTrim, Identifier> textureForTrim,
            Function<ArmorTrim, Identifier> paletteForTrim
    ) {
        this.textureForTrim = textureForTrim;
        this.paletteForTrim = paletteForTrim;
    }

    @Override
    public void preRender(AzRendererPipelineContext<UUID, ItemStack> context) {}

    @Override
    public void render(AzRendererPipelineContext<UUID, ItemStack> context) {
        var stack = ((AzArmorRendererPipelineContext) context).currentStack();
        var trim = stack == null ? null : stack.get(DataComponents.TRIM);

        if (trim == null || context.renderType() == null) {
            return;
        }

        var handle = Minecraft.getInstance()
                .getPalettedTextureManager()
                .getOrPrepare(textureForTrim.apply(trim), paletteForTrim.apply(trim));
        var renderType = RenderTypes.armorTrim(handle.textureLocation(), trim.pattern().value().decal());

        var prevRenderType = context.renderType();
        var prevVertexConsumer = context.vertexConsumer();

        try {
            context.setRenderType(renderType);
            context.setVertexConsumer(handle.wrap(context.multiBufferSource().getBuffer(renderType)));
            context.rendererPipeline().reRender(context);
        } finally {
            context.setRenderType(prevRenderType);
            context.setVertexConsumer(prevVertexConsumer);
        }
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, ItemStack> context, AzBone bone) {}
}