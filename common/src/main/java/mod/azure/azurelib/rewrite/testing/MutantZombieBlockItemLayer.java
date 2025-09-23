package mod.azure.azurelib.rewrite.testing;

import com.mojang.math.Axis;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.layer.AzBlockAndItemLayer;

public class MutantZombieBlockItemLayer extends AzBlockAndItemLayer<MutantZombieEntity> {

    private static final String LEFT_HAND = "bipedHandLeft";

    private static final String RIGHT_HAND = "bipedHandRight";

    @Override
    public ItemStack itemStackForBone(AzBone bone, MutantZombieEntity animatable) {
        if (bone.getName().equals(LEFT_HAND)) {
            return animatable.getItemBySlot(EquipmentSlot.MAINHAND);
        }
        return null;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(
        AzBone bone,
        ItemStack stack,
        MutantZombieEntity animatable
    ) {
        return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderItemForBone(
        AzRendererPipelineContext<MutantZombieEntity> context,
        AzBone bone,
        ItemStack itemStack,
        MutantZombieEntity animatable
    ) {
        context.poseStack().mulPose(Axis.XP.rotationDegrees(270));
        context.poseStack().mulPose(Axis.YP.rotationDegrees(0));
        context.poseStack().mulPose(Axis.ZP.rotationDegrees(0f));
        context.poseStack().translate(0.0D, 0.1D, -0.5D);
        super.renderItemForBone(context, bone, itemStack, animatable);
    }
}
