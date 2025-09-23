package mod.azure.azurelib.rewrite.testing;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.layer.AzArmorLayer;

public class MutantZombieArmorLayer extends AzArmorLayer<MutantZombieEntity> {

    private static final String LEFT_BOOT = "armorBipedLeftFoot";

    private static final String RIGHT_BOOT = "armorBipedRightFoot";

    private static final String LEFT_BOOT_2 = "armorBipedLeftFoot2";

    private static final String RIGHT_BOOT_2 = "armorBipedRightFoot2";

    private static final String LEFT_ARMOR_LEG = "armorBipedLeftLeg";

    private static final String RIGHT_ARMOR_LEG = "armorBipedRightLeg";

    private static final String LEFT_ARMOR_LEG_2 = "armorBipedLeftLeg2";

    private static final String RIGHT_ARMOR_LEG_2 = "armorBipedRightLeg2";

    private static final String CHESTPLATE = "armorBipedBody";

    private static final String RIGHT_SLEEVE = "armorBipedRightArm";

    private static final String LEFT_SLEEVE = "armorBipedLeftArm";

    private static final String HELMET = "armorBipedHead";

    @Override
    protected ItemStack getArmorItemForBone(AzRendererPipelineContext<MutantZombieEntity> context, AzBone bone) {
        return switch (bone.getName()) {
            case LEFT_BOOT, RIGHT_BOOT, LEFT_BOOT_2, RIGHT_BOOT_2 -> this.bootsStack;
            case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG, LEFT_ARMOR_LEG_2, RIGHT_ARMOR_LEG_2 -> this.leggingsStack;
            case CHESTPLATE, RIGHT_SLEEVE, LEFT_SLEEVE -> this.chestplateStack;
            case HELMET -> this.helmetStack;
            default -> null;
        };
    }

    @Override
    protected @NotNull EquipmentSlot getEquipmentSlotForBone(
        AzRendererPipelineContext<MutantZombieEntity> context,
        AzBone bone,
        ItemStack stack
    ) {
        var animatable = context.animatable();
        return switch (bone.getName()) {
            case LEFT_BOOT, RIGHT_BOOT, LEFT_BOOT_2, RIGHT_BOOT_2 -> EquipmentSlot.FEET;
            case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG, LEFT_ARMOR_LEG_2, RIGHT_ARMOR_LEG_2 -> EquipmentSlot.LEGS;
            case RIGHT_SLEEVE -> !animatable.isLeftHanded() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            case LEFT_SLEEVE -> animatable.isLeftHanded() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case HELMET -> EquipmentSlot.HEAD;
            default -> super.getEquipmentSlotForBone(context, bone, stack);
        };
    }

    @Override
    protected @NotNull ModelPart getModelPartForBone(
        AzRendererPipelineContext<MutantZombieEntity> context,
        AzBone bone,
        HumanoidModel<?> baseModel
    ) {
        return switch (bone.getName()) {
            case LEFT_BOOT, LEFT_BOOT_2, LEFT_ARMOR_LEG, LEFT_ARMOR_LEG_2 -> baseModel.leftLeg;
            case RIGHT_BOOT, RIGHT_BOOT_2, RIGHT_ARMOR_LEG, RIGHT_ARMOR_LEG_2 -> baseModel.rightLeg;
            case RIGHT_SLEEVE -> baseModel.rightArm;
            case LEFT_SLEEVE -> baseModel.leftArm;
            case CHESTPLATE -> baseModel.body;
            case HELMET -> baseModel.head;
            default -> super.getModelPartForBone(context, bone, baseModel);
        };
    }
}
