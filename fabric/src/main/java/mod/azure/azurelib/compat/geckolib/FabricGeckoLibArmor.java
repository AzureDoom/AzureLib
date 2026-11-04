package mod.azure.azurelib.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

@ApiStatus.Internal
@SuppressWarnings({ "rawtypes" })
public final class FabricGeckoLibArmor {

    public static @Nullable HumanoidModel<?> getBaseModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    ) {
        return getRenderer(stack) != null ? fallback : null;
    }

    public static void renderArmorPiece(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> baseModel,
        ModelPart modelPart,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay
    ) {
        GeoArmorRenderer renderer = getRenderer(stack);

        if (renderer == null) {
            return;
        }

        renderer.setCurrentItem(entity, stack, slot, baseModel);

        renderIsolated(
            renderer,
            slot,
            GeckoLibArmorSegment.of(baseModel, modelPart, slot),
            () -> renderer.render(poseStack, bufferSource, packedLight)
        );
    }

    private static @Nullable GeoArmorRenderer getRenderer(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) {
            return null;
        }

        return GeoArmorRenderer.renderers.get(stack.getItem().getClass());
    }

    private static void renderIsolated(
        GeoArmorRenderer<?> renderer,
        EquipmentSlot slot,
        @Nullable GeckoLibArmorSegment keep,
        Runnable render
    ) {
        if (keep == null) {
            render.run();
            return;
        }

        var segments = GeckoLibArmorSegment.forSlot(slot);
        var savedNames = new String[segments.size()];

        for (int i = 0; i < segments.size(); i++) {
            var segment = segments.get(i);

            if (segment == keep) {
                continue;
            }

            var boneName = getBoneName(renderer, segment);
            savedNames[i] = boneName;

            if (boneName != null) {
                renderer.getGeoModelProvider().getBone(boneName).setHidden(true);
                setBoneName(renderer, segment, null);
            }
        }

        try {
            render.run();
        } finally {
            for (int i = 0; i < segments.size(); i++) {
                if (savedNames[i] != null) {
                    setBoneName(renderer, segments.get(i), savedNames[i]);
                }
            }
        }
    }

    private static @Nullable String getBoneName(GeoArmorRenderer<?> renderer, GeckoLibArmorSegment segment) {
        return switch (segment) {
            case HEAD -> renderer.headBone;
            case BODY -> renderer.bodyBone;
            case RIGHT_ARM -> renderer.rightArmBone;
            case LEFT_ARM -> renderer.leftArmBone;
            case RIGHT_LEG -> renderer.rightLegBone;
            case LEFT_LEG -> renderer.leftLegBone;
            case RIGHT_BOOT -> renderer.rightBootBone;
            case LEFT_BOOT -> renderer.leftBootBone;
        };
    }

    private static void setBoneName(GeoArmorRenderer<?> renderer, GeckoLibArmorSegment segment, @Nullable String name) {
        switch (segment) {
            case HEAD -> renderer.headBone = name;
            case BODY -> renderer.bodyBone = name;
            case RIGHT_ARM -> renderer.rightArmBone = name;
            case LEFT_ARM -> renderer.leftArmBone = name;
            case RIGHT_LEG -> renderer.rightLegBone = name;
            case LEFT_LEG -> renderer.leftLegBone = name;
            case RIGHT_BOOT -> renderer.rightBootBone = name;
            case LEFT_BOOT -> renderer.leftBootBone = name;
        }
    }

    private FabricGeckoLibArmor() { /* NO-OP */}
}
