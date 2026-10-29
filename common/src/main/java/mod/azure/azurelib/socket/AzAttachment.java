package mod.azure.azurelib.socket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;

/**
 * Something rendered at a socket by an {@link AzAttachmentLayer}: an item, a block, or anything you draw yourself. When
 * {@link #render} runs, the pose stack is at the socket (its position, rotation and the bone's scale), one unit per
 * block.
 *
 * @param <T> the animatable type
 */
@FunctionalInterface
public interface AzAttachment<T> {

    void render(AzRendererPipelineContext<?, T> context, AzBone bone);

    /** Renders an item stack at the socket; empty stacks and {@code null} render nothing. */
    static <T> AzAttachment<T> item(Function<T, ItemStack> stack, ItemDisplayContext displayContext) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(displayContext, "displayContext");

        return (context, bone) -> {
            T animatable = context.animatable();
            ItemStack item = stack.apply(animatable);

            if (item == null || item.isEmpty()) {
                return;
            }

            ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
            ItemStackRenderState itemState = new ItemStackRenderState();

            if (animatable instanceof LivingEntity living) {
                resolver.updateForLiving(
                    itemState,
                    item,
                    displayContext,
                    living
                );
            } else if (animatable instanceof Entity entity) {
                resolver.updateForNonLiving(
                    itemState,
                    item,
                    displayContext,
                    entity
                );
            } else {
                resolver.updateForTopItem(
                    itemState,
                    item,
                    displayContext,
                    Minecraft.getInstance().level,
                    null,
                    animatable.hashCode()
                );
            }

            int packedLight = context.packedLight();
            int packedOverlay = context.packedOverlay();

            context.multiBufferSource()
                .defer(
                    context.poseStack(),
                    (poseStack, collector) -> itemState.submit(
                        poseStack,
                        collector,
                        packedLight,
                        packedOverlay,
                        0
                    )
                );
        };
    }

    /** Renders a block state centered on the socket at half size; {@code null} renders nothing. */
    static <T> AzAttachment<T> block(Function<T, BlockState> state) {
        Objects.requireNonNull(state, "state");

        return (context, bone) -> {
            BlockState blockState = state.apply(context.animatable());

            if (blockState == null) {
                return;
            }

            var minecraft = Minecraft.getInstance();

            var resolver = new BlockModelResolver(minecraft.getModelManager());
            var renderState = new BlockModelRenderState();

            resolver.update(
                renderState,
                blockState,
                BlockDisplayContext.create()
            );

            if (renderState.isEmpty()) {
                return;
            }

            PoseStack poseStack = context.poseStack();

            poseStack.pushPose();

            try {
                poseStack.translate(-0.25f, -0.25f, -0.25f);
                poseStack.scale(0.5f, 0.5f, 0.5f);

                int packedLight = context.packedLight();

                context.multiBufferSource()
                    .defer(
                        poseStack,
                        (deferredPoseStack, collector) -> renderState.submit(
                            deferredPoseStack,
                            collector,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            0
                        )
                    );
            } finally {
                poseStack.popPose();
            }
        };
    }
}
