package mod.azure.azurelib.rewrite.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

/**
 * Utility class for rendering entity leash visuals within the Minecraft rendering engine. This class provides static
 * methods to handle leash rendering logic, enabling flexible re-use and separation from the default rendering behavior.
 * <br>
 * This utility replicates the leash rendering logic from {@link net.minecraft.client.renderer.entity.MobRenderer} to
 * provide enhanced customization for entity rendering purposes.
 */
public class AzEntityLeashRenderUtil {

    /**
     * Static rendering code for rendering a leash segment.<br>
     * It's a like-for-like from {@link net.minecraft.client.renderer.entity.MobRenderer#renderLeash} that had to be
     * duplicated here for flexible usage
     */
    public static <T extends Entity, E extends Entity, M extends MobEntity> void renderLeash(
        AzEntityRenderer<T> azEntityRenderer,
        M mob,
        float partialTick,
        MatrixStack poseStack,
        IRenderTypeBuffer bufferSource,
        E leashHolder
    ) {
        double lerpBodyAngle = (MathHelper.lerp(partialTick, mob.prevRenderYawOffset, mob.renderYawOffset)
            * 0.017453292f) + 1.5707964f;
        Vector3d leashOffset = new Vector3d(0.0D, mob.getEyeHeight(), (mob.getWidth() * 0.4F));
        double xAngleOffset = Math.cos(lerpBodyAngle) * leashOffset.z + Math.sin(lerpBodyAngle) * leashOffset.x;
        double zAngleOffset = Math.sin(lerpBodyAngle) * leashOffset.z - Math.cos(lerpBodyAngle) * leashOffset.x;
        double lerpOriginX = MathHelper.lerp(partialTick, mob.prevPosX, mob.getPosX()) + xAngleOffset;
        double lerpOriginY = MathHelper.lerp(partialTick, mob.prevPosY, mob.getPosY()) + leashOffset.y;
        double lerpOriginZ = MathHelper.lerp(partialTick, mob.prevPosZ, mob.getPosZ()) + zAngleOffset;
        Vec3d ropeGripPosition = leashHolder.getPositionVector().add(0.0D, leashHolder.getEyeHeight() * 0.7D, 0.0D);
        float xDif = (float) (ropeGripPosition.x - lerpOriginX);
        float yDif = (float) (ropeGripPosition.y - lerpOriginY);
        float zDif = (float) (ropeGripPosition.z - lerpOriginZ);
        float offsetMod = MathHelper.fastInvSqrt(xDif * xDif + zDif * zDif) * 0.025f / 2f;
        float xOffset = zDif * offsetMod;
        float zOffset = xDif * offsetMod;
        IVertexBuilder vertexConsumer = bufferSource.getBuffer(RenderType.getLeash());
        BlockPos entityEyePos = new BlockPos(mob.getEyePosition(partialTick));
        BlockPos holderEyePos = new BlockPos(leashHolder.getEyePosition(partialTick));
        int entityBlockLight = azEntityRenderer.getBlockLight((T) mob, entityEyePos.getY());
        int holderBlockLight = leashHolder.isBurning()
            ? 15
            : leashHolder.world
                .getLightFor(
                    LightType.BLOCK,
                    holderEyePos
                );
        int entitySkyLight = mob.world.getLightFor(LightType.SKY, entityEyePos);
        int holderSkyLight = mob.world.getLightFor(LightType.SKY, holderEyePos);

        poseStack.push();
        poseStack.translate(xAngleOffset, leashOffset.y, zAngleOffset);

        Matrix4f posMatrix = new Matrix4f(poseStack.getLast().getMatrix());

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

        poseStack.pop();
    }

    /**
     * Static rendering code for rendering a leash segment.<br>
     * It's a like-for-like from {@link net.minecraft.client.renderer.entity.MobRenderer#addVertexPair} that had to be
     * duplicated here for flexible usage
     */
    private static void renderLeashPiece(
        IVertexBuilder buffer,
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
        float piecePosPercent = segment / 24f;
        int lerpBlockLight = (int) MathHelper.lerp(piecePosPercent, entityBlockLight, holderBlockLight);
        int lerpSkyLight = (int) MathHelper.lerp(piecePosPercent, entitySkyLight, holderSkyLight);
        int packedLight = LightTexture.packLight(lerpBlockLight, lerpSkyLight);
        float knotColourMod = segment % 2 == (isLeashKnot ? 1 : 0) ? 0.7f : 1f;
        float red = 0.5f * knotColourMod;
        float green = 0.4f * knotColourMod;
        float blue = 0.3f * knotColourMod;
        float x = xDif * piecePosPercent;
        float y = yDif > 0.0f
            ? yDif * piecePosPercent * piecePosPercent
            : yDif - yDif * (1.0f - piecePosPercent) * (1.0f - piecePosPercent);
        float z = zDif * piecePosPercent;

        buffer.pos(positionMatrix, x - xOffset, y + yOffset, z + zOffset)
            .color(red, green, blue, 1)
            .lightmap(
                packedLight
            )
            .endVertex();
        buffer.pos(positionMatrix, x + xOffset, y + width - yOffset, z - zOffset)
            .color(red, green, blue, 1)
            .lightmap(
                packedLight
            )
            .endVertex();
    }

    private AzEntityLeashRenderUtil() {
        throw new UnsupportedOperationException();
    }
}
