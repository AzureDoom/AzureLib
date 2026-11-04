package mod.azure.azurelib.compat.geckolib;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum GeckoLibArmorSegment {

    HEAD,
    BODY,
    RIGHT_ARM,
    LEFT_ARM,
    RIGHT_LEG,
    LEFT_LEG,
    RIGHT_BOOT,
    LEFT_BOOT;

    private static final List<GeckoLibArmorSegment> HEAD_SEGMENTS = List.of(HEAD);

    private static final List<GeckoLibArmorSegment> CHEST_SEGMENTS = List.of(BODY, RIGHT_ARM, LEFT_ARM);

    private static final List<GeckoLibArmorSegment> LEGS_SEGMENTS = List.of(RIGHT_LEG, LEFT_LEG);

    private static final List<GeckoLibArmorSegment> FEET_SEGMENTS = List.of(RIGHT_BOOT, LEFT_BOOT);

    public static List<GeckoLibArmorSegment> forSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> HEAD_SEGMENTS;
            case CHEST -> CHEST_SEGMENTS;
            case LEGS -> LEGS_SEGMENTS;
            case FEET -> FEET_SEGMENTS;
            default -> List.of();
        };
    }

    public static @Nullable GeckoLibArmorSegment of(HumanoidModel<?> model, ModelPart part, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> part == model.head ? HEAD : null;
            case CHEST -> part == model.body
                ? BODY
                : part == model.rightArm ? RIGHT_ARM : part == model.leftArm ? LEFT_ARM : null;
            case LEGS -> part == model.rightLeg ? RIGHT_LEG : part == model.leftLeg ? LEFT_LEG : null;
            case FEET -> part == model.rightLeg ? RIGHT_BOOT : part == model.leftLeg ? LEFT_BOOT : null;
            default -> null;
        };
    }
}
