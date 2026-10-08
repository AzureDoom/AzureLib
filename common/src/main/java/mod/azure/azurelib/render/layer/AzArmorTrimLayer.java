package mod.azure.azurelib.render.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.AzArmorRendererPipelineContext;

public class AzArmorTrimLayer implements AzRenderLayer<UUID, ItemStack> {

    /**
     * The trim material's palette suffix, including vanilla's {@code _darker} override when the trim material matches
     * the armor's equipment asset (e.g. iron trim on iron armor).
     */
    public static final BiFunction<ArmorTrim, ItemStack, String> MATERIAL_PALETTE = (trim, stack) -> {
        MaterialAssetGroup assets = trim.material().value().assets();
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

        if (equippable != null && equippable.assetId().isPresent()) {
            return assets.assetId(equippable.assetId().get()).suffix();
        }

        return assets.base().suffix();
    };

    public final Function<ArmorTrim, Identifier> textureForTrim;

    public final BiFunction<ArmorTrim, ItemStack, String> paletteForTrim;

    /**
     * One texture per trim pattern: {@code <baseTexture>_<pattern>}, e.g. {@code yourmod:trims/your_armor_coast}.
     */
    public AzArmorTrimLayer(Identifier baseTexture) {
        this(baseTexture, true);
    }

    /**
     * @param supportPatterns {@code true} for one texture per pattern ({@code <baseTexture>_<pattern>}), {@code false}
     *                        to use {@code baseTexture} for every pattern
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
     * Custom texture and palette suffix choice per trim. The suffix must match a permutation key in your
     * {@code armor_trims.json} atlas source.
     */
    public AzArmorTrimLayer(
        Function<ArmorTrim, Identifier> textureForTrim,
        BiFunction<ArmorTrim, ItemStack, String> paletteForTrim
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

        var spriteId = textureForTrim.apply(trim).withSuffix("_" + paletteForTrim.apply(trim, stack));
        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ARMOR_TRIMS);
        var sprite = atlas.getSprite(spriteId);

        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            return;
        }

        var renderType = Sheets.armorTrimsSheet(trim.pattern().value().decal());

        var prevRenderType = context.renderType();
        var prevVertexConsumer = context.vertexConsumer();

        try {
            context.setRenderType(renderType);
            context.setVertexConsumer(sprite.wrap(context.multiBufferSource().getBuffer(renderType)));
            context.rendererPipeline().reRender(context);
        } finally {
            context.setRenderType(prevRenderType);
            context.setVertexConsumer(prevVertexConsumer);
        }
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, ItemStack> context, AzBone bone) {}
}
