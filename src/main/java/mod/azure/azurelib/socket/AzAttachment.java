package mod.azure.azurelib.socket;

import java.util.Objects;
import java.util.function.Function;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.client.RenderUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Something rendered at a socket by an {@link AzAttachmentLayer}: an item, a block,
 * or anything you draw yourself. When {@link #render} runs, the pose stack is at
 * the socket (its position, rotation and the bone's scale), one unit per block.
 *
 * @param <T> the animatable type
 */
@FunctionalInterface
public interface AzAttachment<T> {

    void render(AzRendererPipelineContext<?, T> context, AzBone bone);

    /** Renders an item stack at the socket; empty stacks and {@code null} render nothing. */
    static <T> AzAttachment<T> item(Function<T, ItemStack> stack, ItemCameraTransforms.TransformType displayContext) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(displayContext, "displayContext");

        return (context, bone) -> {
            T animatable = context.animatable();
            ItemStack item = stack.apply(animatable);

            if (item == null || item.isEmpty()) {
                return;
            }

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

                if (animatable instanceof EntityLivingBase) {
                    Minecraft.getMinecraft().getRenderItem().renderItem(
                            item,
                            (EntityLivingBase) animatable,
                            displayContext,
                            false
                    );
                } else {
                    Minecraft.getMinecraft().getRenderItem().renderItem(item, displayContext);
                }
            } finally {
                GlStateManager.popMatrix();
            }
        };
    }

    /** Renders a block state centered on the socket at half size; {@code null} renders nothing. */
    static <T> AzAttachment<T> block(Function<T, IBlockState> state) {
        Objects.requireNonNull(state, "state");

        return (context, bone) -> {
            IBlockState blockState = state.apply(context.animatable());
            if (blockState == null) {
                return;
            }

            PoseStack poseStack = context.poseStack();
            poseStack.pushPose();
            try {
                poseStack.translate(-0.25F, -0.25F, -0.25F);
                poseStack.scale(0.5F, 0.5F, 0.5F);

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
                    RenderUtils.applyPoseToGl(poseStack.last());
                    Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(blockState, 1.0F);
                } finally {
                    GlStateManager.popMatrix();
                }
            } finally {
                poseStack.popPose();
            }
        };
    }
}
