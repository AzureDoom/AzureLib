package mod.azure.azurelib.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import mod.azure.azurelib.render.AzBufferSource;

/**
 * Utility class for rendering entity leash visuals within the Minecraft rendering engine. This class provides static
 * methods to handle leash rendering logic, enabling flexible re-use and separation from the default rendering behavior.
 * <br>
 * This utility replicates the leash rendering logic from {@link net.minecraft.client.renderer.entity.MobRenderer} to
 * provide enhanced customization for entity rendering purposes.
 */
public class AzEntityLeashRenderUtil {

    public static <M extends Mob, E extends Entity> void renderLeash(
        M mob,
        float partialTick,
        PoseStack poseStack,
        AzBufferSource bufferSource,
        E leashHolder
    ) {
        double lerpBodyAngle = (Mth.lerp(partialTick, mob.yBodyRotO, mob.yBodyRot) * Mth.DEG_TO_RAD) + Mth.HALF_PI;
        Vec3 leashOffset = mob.getLeashOffset();
        double xAngleOffset = Math.cos(lerpBodyAngle) * leashOffset.z + Math.sin(lerpBodyAngle) * leashOffset.x;
        double zAngleOffset = Math.sin(lerpBodyAngle) * leashOffset.z - Math.cos(lerpBodyAngle) * leashOffset.x;
        double lerpOriginX = Mth.lerp(partialTick, mob.xo, mob.getX()) + xAngleOffset;
        double lerpOriginY = Mth.lerp(partialTick, mob.yo, mob.getY()) + leashOffset.y;
        double lerpOriginZ = Mth.lerp(partialTick, mob.zo, mob.getZ()) + zAngleOffset;
        Vec3 ropeGripPosition = leashHolder.getRopeHoldPosition(partialTick);
        float xDif = (float) (ropeGripPosition.x - lerpOriginX);
        float yDif = (float) (ropeGripPosition.y - lerpOriginY);
        float zDif = (float) (ropeGripPosition.z - lerpOriginZ);
        float offsetMod = Mth.invSqrt(xDif * xDif + zDif * zDif) * 0.025f / 2f;
        float xOffset = zDif * offsetMod;
        float zOffset = xDif * offsetMod;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.leash());
        BlockPos entityEyePos = BlockPos.containing(mob.getEyePosition(partialTick));
        BlockPos holderEyePos = BlockPos.containing(leashHolder.getEyePosition(partialTick));
        int entityBlockLight = mob.isOnFire()
            ? 15
            : mob.level().getBrightness(LightLayer.BLOCK, entityEyePos);
        int holderBlockLight = leashHolder.isOnFire()
            ? 15
            : leashHolder.level().getBrightness(LightLayer.BLOCK, holderEyePos);
        int entitySkyLight = mob.level().getBrightness(LightLayer.SKY, entityEyePos);
        int holderSkyLight = mob.level().getBrightness(LightLayer.SKY, holderEyePos);

        poseStack.pushPose();
        poseStack.translate(xAngleOffset, leashOffset.y, zAngleOffset);

        Matrix4f posMatrix = new Matrix4f(poseStack.last().pose());

        for (int segment = 0; segment <= 24; ++segment) {
            renderLeashPiece(
                vertexConsumer,
                posMatrix,
                xDif,
                yDif,
                zDif,
                entityBlockLight,
                holderBlockLight,
                entitySkyLight,
                holderSkyLight,
                0.025f,
                0.025f,
                xOffset,
                zOffset,
                segment,
                false
            );
        }

        for (int segment = 24; segment >= 0; --segment) {
            renderLeashPiece(
                vertexConsumer,
                posMatrix,
                xDif,
                yDif,
                zDif,
                entityBlockLight,
                holderBlockLight,
                entitySkyLight,
                holderSkyLight,
                0.025f,
                0.0f,
                xOffset,
                zOffset,
                segment,
                true
            );
        }

        poseStack.popPose();
    }

    /**
     * Static rendering code for rendering a leash segment.<br>
     */
    private static void renderLeashPiece(
        VertexConsumer buffer,
        Matrix4f positionMatrix,
        float xDif,
        float yDif,
        float zDif,
        int entityBlockLight,
        int holderBlockLight,
        int entitySkyLight,
        int holderSkyLight,
        float width,
        float yOffset,
        float xOffset,
        float zOffset,
        int segment,
        boolean isLeashKnot
    ) {
        var piecePosPercent = segment / 24f;
        var lerpBlockLight = (int) Mth.lerp(piecePosPercent, entityBlockLight, holderBlockLight);
        var lerpSkyLight = (int) Mth.lerp(piecePosPercent, entitySkyLight, holderSkyLight);
        var packedLight = LightCoordsUtil.pack(lerpBlockLight, lerpSkyLight);
        var knotColourMod = segment % 2 == (isLeashKnot ? 1 : 0) ? 0.7f : 1f;
        var red = 0.5f * knotColourMod;
        var green = 0.4f * knotColourMod;
        var blue = 0.3f * knotColourMod;
        var x = xDif * piecePosPercent;
        var y = yDif > 0.0f
            ? yDif * piecePosPercent * piecePosPercent
            : yDif - yDif * (1.0f - piecePosPercent) * (1.0f - piecePosPercent);
        var z = zDif * piecePosPercent;

        putColorLight(buffer, positionMatrix, x - xOffset, y + yOffset, z + zOffset, red, green, blue, 1, packedLight);
        putColorLight(
            buffer,
            positionMatrix,
            x + xOffset,
            y + width - yOffset,
            z - zOffset,
            red,
            green,
            blue,
            1,
            packedLight
        );
    }

    /**
     * 26.2 removed the {@code VertexConsumer#addVertex(Matrix4f, float, float, float)} convenience overload, so the
     * matrix transform is done by hand here instead.
     */
    private static void putColorLight(
        VertexConsumer buffer,
        Matrix4f matrix,
        float x,
        float y,
        float z,
        float red,
        float green,
        float blue,
        float alpha,
        int packedLight
    ) {
        var position = matrix.transformPosition(x, y, z, new Vector3f());
        buffer.addVertex(position.x(), position.y(), position.z())
            .setColor(red, green, blue, alpha)
            .setLight(packedLight);
    }

    private AzEntityLeashRenderUtil() {
        throw new UnsupportedOperationException();
    }
}
