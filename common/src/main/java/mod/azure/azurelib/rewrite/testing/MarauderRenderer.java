package mod.azure.azurelib.rewrite.testing;

import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.entity.AzEntityRenderer;
import mod.azure.azurelib.rewrite.render.entity.AzEntityRendererConfig;
import mod.azure.azurelib.rewrite.render.layer.AzAutoGlowingLayer;
import mod.azure.azurelib.rewrite.render.layer.AzBlockAndItemLayer;

public class MarauderRenderer extends AzEntityRenderer<MarauderEntity> {

    private static final ResourceLocation MODEL = AzureLib.modResource("geo/entity/marauder.geo.json");

    private static final ResourceLocation TEXTURE = AzureLib.modResource("textures/entity/marauder.png");

    public MarauderRenderer(EntityRendererProvider.Context context) {
        super(
            AzEntityRendererConfig.<MarauderEntity>builder(MODEL, TEXTURE)
                .addRenderLayer(new AzAutoGlowingLayer<>())
                .addRenderLayer(new AzBlockAndItemLayer<>() {

                    private static final String LEFT_HAND = "item_bone";

                    @Override
                    public ItemStack itemStackForBone(AzBone bone, MarauderEntity animatable) {
                        if (bone.getName().equals(LEFT_HAND)) {
                            return animatable.getItemBySlot(EquipmentSlot.MAINHAND);
                        }
                        return null;
                    }

                    @Override
                    protected ItemDisplayContext getTransformTypeForStack(
                        AzBone bone,
                        ItemStack stack,
                        MarauderEntity animatable
                    ) {
                        return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    }

                    @Override
                    protected void renderItemForBone(
                        AzRendererPipelineContext<MarauderEntity> context,
                        AzBone bone,
                        ItemStack itemStack,
                        MarauderEntity animatable
                    ) {
                        context.poseStack().mulPose(Axis.XP.rotationDegrees(270));
                        context.poseStack().mulPose(Axis.YP.rotationDegrees(0));
                        context.poseStack().mulPose(Axis.ZP.rotationDegrees(0f));
                        context.poseStack().translate(0.0D, 0.1D, -0.5D);
                        super.renderItemForBone(context, bone, itemStack, animatable);
                    }
                })
                .setRenderEntry(contextPipeline -> {
                    contextPipeline.animatable().updateAnimations();

                    return contextPipeline;
                })
                .setAnimatorProvider(MarauderAnimator::new)
                .setDeathMaxRotation(0F)
                .setShadowRadius(0.5F)
                .build(),
            context
        );
    }
}
