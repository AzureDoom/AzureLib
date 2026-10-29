package mod.azure.azurelib.animation.pose;

import com.mojang.math.Vector3f;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Converts between an entity's model space (see {@link AzBoneChain}) and the world, the way the default entity renderer
 * places the model: at the entity's interpolated position, turned by its body yaw and scaled by its native scale and
 * the renderer's width and height scale.
 * <p>
 * Special render poses (dying, sleeping, spin attacks, upside-down names) are not accounted for. If your renderer
 * config scales the model ({@code AzEntityRendererConfig#setScale}), use {@link #scaled} with the same values.
 * </p>
 */
public final class AzEntityModelSpace {

    public static final AzEntityModelSpace DEFAULT = new AzEntityModelSpace(1, 1);

    private final float widthScale;

    private final float heightScale;

    private AzEntityModelSpace(float widthScale, float heightScale) {
        this.widthScale = widthScale;
        this.heightScale = heightScale;
    }

    public static AzEntityModelSpace scaled(float widthScale, float heightScale) {
        return widthScale == 1 && heightScale == 1 ? DEFAULT : new AzEntityModelSpace(widthScale, heightScale);
    }

    public static float renderYaw(Entity entity, float partialTick) {
        if (entity instanceof LivingEntity) {
            return Mth.rotLerp(partialTick, ((LivingEntity) entity).yBodyRotO, ((LivingEntity) entity).yBodyRot);
        }

        return Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
    }

    public Vector3f modelToEntity(Entity entity, float partialTick, Vector3f point) {
        float baseScale = entity instanceof LivingEntity ? ((LivingEntity) entity).getScale() : 1f;
        float theta = (180f - renderYaw(entity, partialTick)) * Mth.DEG_TO_RAD;
        float c = (float) Math.cos(theta);
        float s = (float) Math.sin(theta);
        float x = c * point.x + s * point.z;
        float z = -s * point.x + c * point.z;
        return AzVectors.set(
            point,
            x * baseScale * widthScale,
            point.y * baseScale * heightScale,
            z * baseScale * widthScale
        );
    }

    public Vector3f entityToModel(Entity entity, float partialTick, Vector3f offset) {
        float baseScale = entity instanceof LivingEntity ? ((LivingEntity) entity).getScale() : 1f;
        float sw = baseScale * widthScale;
        float sh = baseScale * heightScale;
        float x = sw == 0 ? 0 : offset.x / sw;
        float y = sh == 0 ? 0 : offset.y / sh;
        float z = sw == 0 ? 0 : offset.z / sw;
        float theta = -(180f - renderYaw(entity, partialTick)) * Mth.DEG_TO_RAD;
        float c = (float) Math.cos(theta);
        float s = (float) Math.sin(theta);
        return AzVectors.set(offset, c * x + s * z, y, -s * x + c * z);
    }

    public Vector3f worldToModel(Entity entity, float partialTick, double x, double y, double z, Vector3f dest) {
        Vec3 origin = entity.getPosition(partialTick);
        dest.set((float) (x - origin.x), (float) (y - origin.y), (float) (z - origin.z));
        return entityToModel(entity, partialTick, dest);
    }

    public Vec3 modelToWorld(Entity entity, float partialTick, Vector3f point) {
        Vector3f offset = modelToEntity(entity, partialTick, point.copy());
        return entity.getPosition(partialTick).add(offset.x, offset.y, offset.z);
    }
}
