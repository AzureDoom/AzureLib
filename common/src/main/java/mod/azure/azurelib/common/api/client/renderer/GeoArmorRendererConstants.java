package mod.azure.azurelib.common.api.client.renderer;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * @author Boston Vanseghi
 */
public class GeoArmorRendererConstants {

    public static final String BONE_ARMOR_BODY_NAME = "armorBody";
    public static final String BONE_ARMOR_HEAD_NAME = "armorHead";
    public static final String BONE_ARMOR_LEFT_ARM_NAME = "armorLeftArm";
    public static final String BONE_ARMOR_RIGHT_ARM_NAME = "armorRightArm";
    public static final String BONE_ARMOR_LEFT_BOOT_NAME = "armorLeftBoot";
    public static final String BONE_ARMOR_RIGHT_BOOT_NAME = "armorRightBoot";
    public static final String BONE_ARMOR_LEFT_LEG_NAME = "armorLeftLeg";
    public static final String BONE_ARMOR_RIGHT_LEG_NAME = "armorRightLeg";

    public static final Vector3f ZERO = Vec3.ZERO.toVector3f();

    private GeoArmorRendererConstants() {
        throw new UnsupportedOperationException();
    }
}
