package mod.azure.azurelib.socket;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.Objects;
import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemDisplayContext;
import mod.azure.azurelib.render.item.AzItemDisplayTransforms;
import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.LightTexture;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.AzBlockState;
import mod.azure.azurelib.util.client.RenderUtils;

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
    static <T> AzAttachment<T> item(Function<T, ItemStack> stack, AzItemDisplayContext displayContext) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(displayContext, "displayContext");

        return (context, bone) -> {
            T animatable = context.animatable();
            ItemStack item = stack.apply(animatable);

            if (item == null || item.getItem() == null || !(animatable instanceof LivingEntity)) {
                return;
            }

            beginVanillaRender(context);
            try {
                AzItemRenderer renderer = AzItemRendererRegistry.getOrNull(item.getItem());

                if (renderer != null) {
                    // The GL matrix is already positioned at the socket.
                    AzItemDisplayTransforms.forItem(item.getItem()).apply(displayContext);
                    GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                    renderer.render(item, displayContext);
                } else {
                    // Match the Beta layer's biped hand placement for vanilla items.
                    applyVanillaHandTransforms(item);
                    McClient.renderVanillaItem((LivingEntity) animatable, item);
                }
            } finally {
                GlStateManager.popMatrix();
            }
        };
    }

    /** Renders a block centered on the socket at half size; {@code null} renders nothing. */
    static <T> AzAttachment<T> block(Function<T, AzBlockState> state) {
        Objects.requireNonNull(state, "state");

        return (context, bone) -> {
            AzBlockState blockState = state.apply(context.animatable());
            if (blockState == null) {
                return;
            }

            PoseStack poseStack = context.poseStack();
            poseStack.pushPose();
            try {
                poseStack.translate(-0.25F, -0.25F, -0.25F);
                poseStack.scale(0.5F, 0.5F, 0.5F);

                beginVanillaRender(context);
                try {
                    T animatable = context.animatable();
                    McClient.renderVanillaItem(
                        animatable instanceof LivingEntity ? (LivingEntity) animatable : null,
                        new ItemStack(Mc.blockId(blockState.getBlock()), 1, blockState.getMeta())
                    );
                } finally {
                    GlStateManager.popMatrix();
                }
            } finally {
                poseStack.popPose();
            }
        };
    }

    /** Matches the Beta layer's vanilla held-item transforms without adding a second socket transform. */
    static void applyVanillaHandTransforms(ItemStack stack) {
        if (McClient.isBlockItem3D(stack)) {
            // Arsenic handles block-item scale and third-person display transforms.
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        } else if (Mc.isHandheld(Mc.item(stack))) {
            float scale = 0.625F;
            GlStateManager.translate(0.0F, 0.1875F, 0.0F);
            GlStateManager.scale(scale, -scale, scale);
            GlStateManager.rotate(-100.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        } else {
            float scale = 0.375F;
            GlStateManager.translate(0.25F, 0.1875F, -0.1875F);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(20.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    /** Flushes AzureLib geometry, applies Beta lighting, and positions GL rendering at the socket. */
    static void beginVanillaRender(AzRendererPipelineContext<?, ?> context) {
        if (context.multiBufferSource() instanceof AzBufferSource) {
            ((AzBufferSource) context.multiBufferSource()).endBatch();
        }

        int packedLight = context.packedLight();
        float light = Math.max(LightTexture.block(packedLight), LightTexture.sky(packedLight)) / 15.0F;
        GlStateManager.color(light, light, light, 1.0F);
        GlStateManager.pushMatrix();
        try {
            RenderUtils.applyPoseToGl(context.poseStack().last());
        } catch (RuntimeException ex) {
            GlStateManager.popMatrix();
            throw ex;
        }
    }
}
