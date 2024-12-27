package mod.azure.azurelib.fabric.core2.example.entities.ovamorph;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.core2.render.entity.AzEntityRenderer;
import mod.azure.azurelib.core2.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class OvamorphRenderer extends AzEntityRenderer<Ovamorph> {

    private static final ResourceLocation MODEL = AzureLib.modResource("geo/entity/ovamorph.geo.json");

    private static final ResourceLocation TEXTURE = AzureLib.modResource("textures/entity/ovamorph.png");

    public OvamorphRenderer(EntityRendererProvider.Context context) {
        super(AzEntityRendererConfig.<Ovamorph>builder(MODEL, TEXTURE).setAnimatorProvider(OvamorphAnimator::new).build(), context);
        this.shadowRadius = 0.4F;
    }

    @Override
    public void render(
        @NotNull Ovamorph entity,
        float entityYaw,
        float partialTick,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource bufferSource,
        int packedLight
    ) {
        var maxSpawnCount = entity.hatchManager().maximumSpawnCount();
        var additiveScale = 0.35F * (maxSpawnCount - 1);
        var scale = 1.4F + Math.max(additiveScale, 0);

        poseStack.scale(scale, scale, scale);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    // TODO:
//    @Override
//    public RenderType getRenderType(Ovamorph animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
//        return RenderType.entityTranslucent(texture);
//    }
}