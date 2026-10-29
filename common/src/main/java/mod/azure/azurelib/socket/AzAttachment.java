package mod.azure.azurelib.socket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            if (animatable instanceof LivingEntity living) {
                itemRenderer.renderStatic(
                    living,
                    item,
                    displayContext,
                    false,
                    context.poseStack(),
                    context.multiBufferSource(),
                    living.level(),
                    context.packedLight(),
                    context.packedOverlay(),
                    living.getId()
                );
            } else {
                itemRenderer.renderStatic(
                    item,
                    displayContext,
                    context.packedLight(),
                    context.packedOverlay(),
                    context.poseStack(),
                    context.multiBufferSource(),
                    Minecraft.getInstance().level,
                    animatable.hashCode()
                );
            }
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

            PoseStack poseStack = context.poseStack();
            poseStack.pushPose();
            poseStack.translate(-0.25f, -0.25f, -0.25f);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                    blockState,
                    poseStack,
                    context.multiBufferSource(),
                    context.packedLight(),
                    OverlayTexture.NO_OVERLAY
                );
            poseStack.popPose();
        };
    }
}
