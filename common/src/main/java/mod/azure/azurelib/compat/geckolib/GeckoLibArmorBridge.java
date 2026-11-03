package mod.azure.azurelib.compat.geckolib;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiConsumer;

@ApiStatus.Internal
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class GeckoLibArmorBridge {

    public static boolean hasArmorRenderer(ItemStack stack, EquipmentSlot slot) {
        return getRenderer(stack, slot) != null;
    }

    public static @Nullable BiConsumer<PoseStack, SubmitNodeCollector> prepareArmorPiece(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> baseModel,
        ModelPart modelPart,
        float partialTick,
        int packedLight,
        int packedOverlay
    ) {
        GeoArmorRenderer renderer = getRenderer(stack, slot);

        if (renderer == null) {
            return null;
        }

        var humanoidState = createRenderState(entity, partialTick, packedLight);
        var renderState = (GeoRenderState) (Object) humanoidState;

        renderer.fillRenderState(
            (GeoAnimatable) stack.getItem(),
            new GeoArmorRenderer.RenderData(stack, slot, entity, baseModel),
            renderState,
            partialTick
        );
        renderState.addGeckolibData(DataTickets.PACKED_LIGHT, packedLight);
        renderState.addGeckolibData(DataTickets.PACKED_OVERLAY, packedOverlay);

        List<GeoArmorRenderer.ArmorSegment> segments = renderer.getSegmentsForSlot(humanoidState, slot);
        GeoArmorRenderer.ArmorSegment usedSegment = null;

        for (var segment : segments) {
            if (segment.modelPartGetter.apply(baseModel) == modelPart) {
                usedSegment = segment;
                break;
            }
        }

        if (usedSegment == null) {
            return null;
        }

        final var segment = usedSegment;
        final var bonePos = segment.modelPartMatcher.apply(new Vector3f(modelPart.x, modelPart.y, modelPart.z));
        final float rotX = -modelPart.xRot;
        final float rotY = -modelPart.yRot;
        final float rotZ = modelPart.zRot;
        final String usedBone = renderer.getBoneNameForSegment(humanoidState, segment);
        final String[] hiddenBones = segments.stream()
            .filter(other -> other != segment)
            .map(other -> renderer.getBoneNameForSegment(humanoidState, other))
            .toArray(String[]::new);

        RenderPassInfo.BoneUpdater updater = (renderPassInfo, snapshots) -> {
            snapshots.get(usedBone)
                .ifPresent(
                    snapshot -> snapshot.setRotX(rotX)
                        .setRotY(rotY)
                        .setRotZ(rotZ)
                        .setTranslateX(bonePos.x)
                        .setTranslateY(bonePos.y)
                        .setTranslateZ(bonePos.z)
                );

            for (var hidden : hiddenBones) {
                snapshots.get(hidden).ifPresent(snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
            }
        };
        final List<RenderPassInfo.BoneUpdater> updaters = List.of(updater);

        return (poseStack, collector) -> renderer.performRenderPass(
            renderState,
            poseStack,
            collector,
            Minecraft.getInstance().levelRenderer.levelRenderState.cameraRenderState,
            updaters
        );
    }

    private static @Nullable GeoArmorRenderer getRenderer(ItemStack stack, EquipmentSlot slot) {
        if (!(stack.getItem() instanceof GeoItem) || !HumanoidArmorLayer.shouldRender(stack, slot)) {
            return null;
        }

        var provider = GeoRenderProvider.of(stack);

        return provider == GeoRenderProvider.DEFAULT ? null : provider.getGeoArmorRenderer(stack, slot);
    }

    private static HumanoidRenderState createRenderState(LivingEntity entity, float partialTick, int packedLight) {
        var state = new HumanoidRenderState();

        state.entityType = entity.getType();
        state.x = entity.getX();
        state.y = entity.getY();
        state.z = entity.getZ();
        state.ageInTicks = entity.tickCount + partialTick;
        state.lightCoords = packedLight;
        state.walkAnimationPos = entity.walkAnimation.position(partialTick);
        state.walkAnimationSpeed = entity.walkAnimation.speed(partialTick);
        state.isBaby = entity.isBaby();
        state.isCrouching = entity.isCrouching();

        return state;
    }

    private GeckoLibArmorBridge() { /* NO-OP */}
}
