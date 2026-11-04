package mod.azure.azurelib.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

@ApiStatus.Internal
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ForgeGeckoLibArmor {

    public static @Nullable HumanoidModel<?> getBaseModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    ) {
        if (!(stack.getItem() instanceof ArmorItem) || !(stack.getItem() instanceof IAnimatable)) {
            return null;
        }

        var model = ForgeHooksClient.getArmorModel(entity, stack, slot, fallback);

        return model instanceof GeoArmorRenderer<?> renderer ? renderer : null;
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
        if (!(baseModel instanceof GeoArmorRenderer<?> renderer)) {
            return;
        }

        GeoArmorRenderer raw = renderer;
        raw.setCurrentItem(entity, stack, slot);
        raw.applySlot(slot);

        var texture = raw.getTextureLocation((ArmorItem) stack.getItem());
        var buffer = ItemRenderer.getArmorFoilBuffer(
            bufferSource,
            RenderType.armorCutoutNoCull(texture),
            false,
            stack.hasFoil()
        );

        renderIsolated(
            renderer,
            slot,
            GeckoLibArmorSegment.of(baseModel, modelPart, slot),
            () -> raw.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 1, 1, 1, 1)
        );
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

    private ForgeGeckoLibArmor() { /* NO-OP */}
}
