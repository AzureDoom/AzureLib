package mod.azure.azurelib.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@ApiStatus.Internal
public final class GeckoLibArmorBridge {

    public static boolean isGeoArmorRenderer(@Nullable HumanoidModel<?> model) {
        return model instanceof GeoArmorRenderer<?>;
    }

    public static void renderArmorPiece(
        HumanoidModel<?> geoArmorModel,
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        ModelPart modelPart,
        PoseStack poseStack,
        int packedLight,
        int packedOverlay
    ) {
        if (!(geoArmorModel instanceof GeoArmorRenderer<?> renderer)) {
            return;
        }

        renderer.prepForRender(entity, stack, slot, renderer);
        renderer.applyBoneVisibilityByPart(slot, modelPart, renderer);
        renderer.renderToBuffer(poseStack, null, packedLight, packedOverlay, 1, 1, 1, 1);
    }

    private GeckoLibArmorBridge() { /* NO-OP */}
}
