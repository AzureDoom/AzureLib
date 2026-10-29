package mod.azure.azurelib.socket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.Objects;
import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemDisplayContext;
import mod.azure.azurelib.render.item.AzItemDisplayTransforms;
import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
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

            if (item == null || item.getItem() == null || item.stackSize <= 0) {
                return;
            }

            // Match the legacy layer's vanilla fallback, which requires a living entity.
            if (!(animatable instanceof EntityLivingBase)) {
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
                    RenderManager.instance.itemRenderer.renderItem(
                        (EntityLivingBase) animatable,
                        item,
                        0
                    );
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
                    Minecraft.getMinecraft()
                        .getTextureManager()
                        .bindTexture(TextureMap.locationBlocksTexture);
                    new RenderBlocks().renderBlockAsItem(
                        blockState.getBlock(),
                        blockState.getMeta(),
                        1.0F
                    );
                } finally {
                    GlStateManager.popMatrix();
                }
            } finally {
                poseStack.popPose();
            }
        };
    }

    /** Flushes AzureLib geometry and positions vanilla GL rendering at the current socket. */
    static void beginVanillaRender(AzRendererPipelineContext<?, ?> context) {
        if (context.multiBufferSource() instanceof AzBufferSource) {
            ((AzBufferSource) context.multiBufferSource()).endBatch();
        }

        int packedLight = context.packedLight();
        OpenGlHelper.setLightmapTextureCoords(
            OpenGlHelper.lightmapTexUnit,
            packedLight & 0xFFFF,
            packedLight >> 16
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        try {
            RenderUtils.applyPoseToGl(context.poseStack().last());
        } catch (RuntimeException ex) {
            GlStateManager.popMatrix();
            throw ex;
        }
    }
}
