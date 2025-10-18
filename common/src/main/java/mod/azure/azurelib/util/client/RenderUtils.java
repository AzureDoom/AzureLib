package mod.azure.azurelib.util.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.model.AzBone;

/**
 * Helper class for various methods and functions useful while rendering
 */
public final class RenderUtils {

    private static final Quaternion QX = new Quaternion(0, 0, 0, 1);

    private static final Quaternion QY = new Quaternion(0, 0, 0, 1);

    private static final Quaternion QZ = new Quaternion(0, 0, 0, 1);

    public static void translateMatrixToBone(PoseStack poseStack, AzBone bone) {
        poseStack.translate(-bone.getPosX() / 16f, bone.getPosY() / 16f, bone.getPosZ() / 16f);
    }

    public static void rotateMatrixAroundBone(PoseStack poseStack, AzBone bone) {
        var rz = bone.getRotZ();
        var ry = bone.getRotY();
        var rx = bone.getRotX();

        if (rz != 0f) {
            setQuatFromRotZ(QZ, rz);
            poseStack.mulPose(QZ);
        }
        if (ry != 0f) {
            setQuatFromRotY(QY, ry);
            poseStack.mulPose(QY);
        }
        if (rx != 0f) {
            setQuatFromRotX(QX, rx);
            poseStack.mulPose(QX);
        }
    }

    public static void rotateMatrixAroundCube(PoseStack poseStack, GeoCube cube) {
        Vec3 rotation = cube.rotation();

        if (rotation.z() != 0f) {
            setQuatFromRotZ(QZ, (float) rotation.z());
            poseStack.mulPose(QZ);
        }
        if (rotation.y() != 0f) {
            setQuatFromRotY(QY, (float) rotation.y());
            poseStack.mulPose(QY);
        }
        if (rotation.x() != 0f) {
            setQuatFromRotX(QX, (float) rotation.x());
            poseStack.mulPose(QX);
        }
    }

    public static void scaleMatrixForBone(PoseStack poseStack, AzBone bone) {
        poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static void translateToPivotPoint(PoseStack poseStack, GeoCube cube) {
        Vec3 pivot = cube.pivot();
        poseStack.translate(pivot.x() / 16f, pivot.y() / 16f, pivot.z() / 16f);
    }

    public static void translateToPivotPoint(PoseStack poseStack, AzBone bone) {
        poseStack.translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f);
    }

    public static void translateAwayFromPivotPoint(PoseStack poseStack, GeoCube cube) {
        Vec3 pivot = cube.pivot();

        poseStack.translate(-pivot.x() / 16f, -pivot.y() / 16f, -pivot.z() / 16f);
    }

    public static void translateAwayFromPivotPoint(PoseStack poseStack, AzBone bone) {
        poseStack.translate(-bone.getPivotX() / 16f, -bone.getPivotY() / 16f, -bone.getPivotZ() / 16f);
    }

    public static void translateAndRotateMatrixForBone(PoseStack poseStack, AzBone bone) {
        translateToPivotPoint(poseStack, bone);
        rotateMatrixAroundBone(poseStack, bone);
    }

    public static void prepMatrixForBone(PoseStack poseStack, AzBone bone) {
        translateMatrixToBone(poseStack, bone);
        translateToPivotPoint(poseStack, bone);
        rotateMatrixAroundBone(poseStack, bone);
        scaleMatrixForBone(poseStack, bone);
        translateAwayFromPivotPoint(poseStack, bone);
    }

    public static Matrix4f invertAndMultiplyMatrices(Matrix4f baseMatrix, Matrix4f inputMatrix) {
        inputMatrix = new Matrix4f(inputMatrix);

        inputMatrix.invert();
        inputMatrix.multiply(baseMatrix);

        return inputMatrix;
    }

    /**
     * Rotates the given pose stack to align with the facing direction and orientation of the provided animatable
     * entity.
     *
     * @param poseStack   The pose stack to apply the rotations to.
     * @param animatable  The entity whose rotation values will be used for transformation.
     * @param partialTick The partial tick time used to interpolate the entity's rotation.
     */
    public static void faceRotation(PoseStack poseStack, Entity animatable, float partialTick) {
        poseStack.mulPose(
            Vector3f.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90)
        );
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot())));
    }

    private static void setQuatFromRotX(Quaternion q, float angleRad) {
        float h = angleRad * 0.5f;
        float s = Mth.sin(h);
        float c = Mth.cos(h);
        q.set(s, 0f, 0f, c);
    }

    private static void setQuatFromRotY(Quaternion q, float angleRad) {
        float h = angleRad * 0.5f;
        float s = Mth.sin(h);
        float c = Mth.cos(h);
        q.set(0f, s, 0f, c);
    }

    private static void setQuatFromRotZ(Quaternion q, float angleRad) {
        float h = angleRad * 0.5f;
        float s = Mth.sin(h);
        float c = Mth.cos(h);
        q.set(0f, 0f, s, c);
    }

    /**
     * Gets the actual dimensions of a texture resource from a given path.<br>
     * Not performance-efficient, and should not be relied upon
     *
     * @param texture The path of the texture resource to check
     * @return The dimensions (width x height) of the texture, or null if unable to find or read the file
     */
    @Nullable
    public static IntIntPair getTextureDimensions(ResourceLocation texture) {
        if (texture == null)
            return null;

        AbstractTexture originalTexture = null;
        Minecraft mc = Minecraft.getInstance();

        try {
            originalTexture = mc.submit(() -> mc.getTextureManager().getTexture(texture)).get();
        } catch (Exception e) {
            AzureLib.LOGGER.warn("Failed to load image for id {}", texture);
            e.printStackTrace();
        }

        if (originalTexture == null)
            return null;

        NativeImage image = null;

        try {
            image = originalTexture instanceof DynamicTexture dynamicTexture
                ? dynamicTexture.getPixels()
                : NativeImage.read(mc.getResourceManager().getResource(texture).getInputStream());
        } catch (Exception e) {
            AzureLib.LOGGER.error("Failed to read image for id {}", texture);
            e.printStackTrace();
        }

        return image == null ? null : IntIntImmutablePair.of(image.getWidth(), image.getHeight());
    }

    public static double getCurrentSystemTick() {
        return System.nanoTime() / 1E6 / 50d;
    }

    /**
     * Calculates and retrieves the current game tick. The value is determined by multiplying the current rendering time
     * by 20.
     *
     * @return The current tick as a double value.
     */
    public static double getCurrentTick() {
        return Blaze3D.getTime() * 20d;
    }

    /**
     * Returns a float equivalent of a boolean.<br>
     * Output table:
     * <ul>
     * <li>true -> 1</li>
     * <li>false -> 0</li>
     * </ul>
     */
    public static float booleanToFloat(boolean input) {
        return input ? 1f : 0f;
    }

    /**
     * Converts a given double array to its {@link Vec3} equivalent
     */
    public static Vec3 arrayToVec(double[] array) {
        return new Vec3(array[0], array[1], array[2]);
    }

    /**
     * Rotates a {@link AzBone} to match a provided {@link ModelPart}'s rotations.<br>
     * Usually used for items or armor rendering to match the rotations of other non-geo model parts.
     */
    public static void matchModelPartRot(ModelPart from, AzBone to) {
        to.updateRotation(-from.xRot, -from.yRot, from.zRot);
    }

    /**
     * If a {@link GeoCube} is a 2d plane the {@link mod.azure.azurelib.cache.object.GeoQuad Quad's} normal is inverted
     * in an intersecting plane,it can cause issues with shaders and other lighting tasks.<br>
     * This performs a pseudo-ABS function to help resolve some of those issues.
     */
    public static void fixInvertedFlatCube(GeoCube cube, Vector3f normal) {
        if (normal.x() < 0 && (cube.size().y() == 0 || cube.size().z() == 0))
            normal.mul(-1, 1, 1);

        if (normal.y() < 0 && (cube.size().x() == 0 || cube.size().z() == 0))
            normal.mul(1, -1, 1);

        if (normal.z() < 0 && (cube.size().x() == 0 || cube.size().y() == 0))
            normal.mul(1, 1, -1);
    }

    /**
     * Converts a {@link Direction} to a rotational float for rotation purposes
     */
    public static float getDirectionAngle(Direction direction) {
        return switch (direction) {
            case SOUTH -> 90f;
            case NORTH -> 270f;
            case EAST -> 180f;
            default -> 0f;
        };
    }

    private RenderUtils() {
        throw new UnsupportedOperationException();
    }
}
