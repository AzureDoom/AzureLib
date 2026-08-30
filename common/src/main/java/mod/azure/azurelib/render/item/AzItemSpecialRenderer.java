package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import mod.azure.azurelib.render.AzBufferSource;

public class AzItemSpecialRenderer implements SpecialModelRenderer<ItemStack> {

    public static final AzItemSpecialRenderer INSTANCE =
        new AzItemSpecialRenderer();

    @Override
    public void submit(
        @Nullable ItemStack stack,
        @NonNull PoseStack poseStack,
        @NonNull SubmitNodeCollector collector,
        int packedLight,
        int packedOverlay,
        boolean hasFoil,
        int outlineColor
    ) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        var renderer = AzItemRendererRegistry.getOrNull(stack.getItem());

        if (renderer == null) {
            return;
        }

        var bufferSource = new AzBufferSource();

        /*
         * IMPORTANT: AzureLib records transformed vertices into AzBufferSource. We therefore render the AzureLib model
         * against its own local PoseStack, and apply Minecraft's item pose when replaying.
         */
        var localPoseStack = new PoseStack();

        renderer.renderSpecial(
            stack,
            localPoseStack,
            bufferSource,
            packedLight,
            packedOverlay
        );

        bufferSource.submitAll(collector, poseStack);
    }

    @Override
    public void getExtents(@NonNull Consumer<Vector3fc> consumer) {}

    @Override
    public @Nullable ItemStack extractArgument(@NonNull ItemStack stack) {
        return stack;
    }

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<ItemStack> {

        public static final MapCodec<Unbaked> MAP_CODEC =
            MapCodec.unit(new Unbaked());

        @Override
        public @NonNull SpecialModelRenderer<ItemStack> bake(
            @NonNull BakingContext context
        ) {
            return INSTANCE;
        }

        @Override
        public @NonNull MapCodec<? extends SpecialModelRenderer.Unbaked<ItemStack>> type() {
            return MAP_CODEC;
        }
    }
}
