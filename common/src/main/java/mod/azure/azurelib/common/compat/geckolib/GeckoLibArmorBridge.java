package mod.azure.azurelib.common.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@ApiStatus.Internal
public final class GeckoLibArmorBridge {

    public static @Nullable HumanoidModel<?> getArmorModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<LivingEntity> defaultModel
    ) {
        if (!(stack.getItem() instanceof GeoItem)) {
            return null;
        }

        var model = GeoRenderProvider.of(stack).getGeoArmorRenderer(entity, stack, slot, defaultModel);

        return model instanceof GeoArmorRenderer<?> geoArmorRenderer ? geoArmorRenderer : null;
    }

    public static void renderArmorPiece(
        HumanoidModel<?> geoArmorModel,
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        ModelPart modelPart,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        float partialTick,
        int packedLight,
        int packedOverlay
    ) {
        if (!(geoArmorModel instanceof GeoArmorRenderer<?> renderer)) {
            return;
        }

        renderer.prepForRender(entity, stack, slot, renderer, bufferSource, partialTick, 0, 0, 0, 0);
        renderer.applyBoneVisibilityByPart(slot, modelPart, renderer);
        renderer.renderToBuffer(poseStack, null, packedLight, packedOverlay, -1);
    }

    private GeckoLibArmorBridge() { /* NO-OP */}
}
