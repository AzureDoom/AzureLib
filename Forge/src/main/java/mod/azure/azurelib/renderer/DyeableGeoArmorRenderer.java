/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * A dyeable armour renderer for AzureLib armor models.
 */
@Deprecated()
public abstract class DyeableGeoArmorRenderer<T extends Item & GeoItem> extends GeoArmorRenderer<T> {
    protected final Set<GeoBone> dyeableBones = new ObjectArraySet<>();
    protected BakedGeoModel lastModel = null;

    protected DyeableGeoArmorRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    public void preRender(
            MatrixStack poseStack,
            T animatable,
            BakedGeoModel model,
            @Nullable IRenderTypeBuffer bufferSource,
            @Nullable IVertexBuilder buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        super.preRender(
                poseStack,
                animatable,
                model,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        if (!isReRender)
            checkBoneDyeCache(model);
    }

    @Override
    public void renderCubesOfBone(
            MatrixStack poseStack,
            GeoBone bone,
            IVertexBuilder buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (this.dyeableBones.contains(bone)) {
            final Color color = getColorForBone(bone);

            red *= (float) color.getRed();
            green *= (float) color.getGreen();
            blue *= (float) color.getBlue();
            alpha *= (float) color.getAlpha();
        }
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red,
                green,
                blue,
                alpha);
    }

    /**
     * Whether the given GeoBone should be considered dyeable or not.
     * <p>
     * Note that values returned from here are cached for the last rendered {@link BakedGeoModel} and require a manual
     * reset if you intend to change these results.
     * </p>
     *
     * @return whether the bone should be dyed or not
     */
    protected abstract boolean isBoneDyeable(GeoBone bone);

    /**
     * What color the given GeoBone should be dyed as.
     * <p>
     * Only bones that were marked as 'dyeable' in {@link DyeableGeoArmorRenderer#isBoneDyeable(GeoBone)} are provided
     * here
     * </p>
     */
    protected abstract Color getColorForBone(GeoBone bone);

    /**
     * Check whether the dye cache should be considered dirty and recomputed.
     * <p>
     * The less this forces re-computation, the better for performance
     * </p>
     */
    protected void checkBoneDyeCache(BakedGeoModel model) {
        if (model != this.lastModel) {
            this.dyeableBones.clear();
            this.lastModel = model;
            collectDyeableBones(model.topLevelBones());
        }
    }

    /**
     * Recursively parse through the given bones collection, collecting and caching dyeable bones as applicable
     */
    protected void collectDyeableBones(Collection<GeoBone> bones) {
        for (GeoBone bone : bones) {
            if (isBoneDyeable(bone))
                this.dyeableBones.add(bone);

            collectDyeableBones(bone.getChildBones());
        }
    }
}
