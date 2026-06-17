/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.common.util.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.cache.object.GeoCube;
import mod.azure.azurelib.common.cache.object.GeoQuad;
import mod.azure.azurelib.common.model.AzBone;

/**
 * Helper class for various methods and functions useful while rendering
 */
public class RenderUtils {

    private static final Matrix4f INVERT_SCRATCH = new Matrix4f();

    private static final Quaternionf X_QUATERNION_CACHE = new Quaternionf();

    private static final Quaternionf Y_QUATERNION_CACHE = new Quaternionf();

    private static final Quaternionf Z_QUATERNION_CACHE = new Quaternionf();

    public static void translateMatrixToBone(PoseStack poseStack, AzBone bone) {
        poseStack.translate(-bone.getPosX() / 16f, bone.getPosY() / 16f, bone.getPosZ() / 16f);
    }

    public static void rotateMatrixAroundBone(PoseStack poseStack, AzBone bone) {
        float rotX = bone.getRotX();
        float rotY = bone.getRotY();
        float rotZ = bone.getRotZ();

        if (rotZ != 0)
            poseStack.mulPose(Z_QUATERNION_CACHE.rotationXYZ(0f, 0f, rotZ));

        if (rotY != 0)
            poseStack.mulPose(Y_QUATERNION_CACHE.rotationXYZ(0f, rotY, 0f));

        if (rotX != 0)
            poseStack.mulPose(X_QUATERNION_CACHE.rotationXYZ(rotX, 0f, 0f));
    }

    public static void rotateMatrixAroundCube(PoseStack poseStack, GeoCube cube) {
        Vec3 rotation = cube.rotation();

        if (rotation.z() != 0f) {
            Z_QUATERNION_CACHE.identity().rotateZ((float) rotation.z());
            poseStack.mulPose(Z_QUATERNION_CACHE);
        }
        if (rotation.y() != 0f) {
            Y_QUATERNION_CACHE.identity().rotateY((float) rotation.y());
            poseStack.mulPose(Y_QUATERNION_CACHE);
        }
        if (rotation.x() != 0f) {
            X_QUATERNION_CACHE.identity().rotateX((float) rotation.x());
            poseStack.mulPose(X_QUATERNION_CACHE);
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

    /**
     * Inverts {@code inputMatrix} and multiplies it by {@code baseMatrix}, returning the result as a new
     * {@link Matrix4f}. Uses a static scratch buffer for the inversion to avoid per-call heap allocation.
     */
    public static Matrix4f invertAndMultiplyMatrices(Matrix4f baseMatrix, Matrix4f inputMatrix) {
        INVERT_SCRATCH.set(inputMatrix);
        INVERT_SCRATCH.invert();
        INVERT_SCRATCH.mul(baseMatrix);
        return new Matrix4f(INVERT_SCRATCH);
    }

    /**
     * Translates the provided {@link PoseStack} to face towards the given {@link Entity}'s rotation.<br>
     */
    public static void faceRotation(PoseStack poseStack, Entity animatable, float partialTick) {
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot())));
    }

    /**
     * Returns a new {@link Matrix4f} equal to {@code matrix} translated by {@code vector}. The original matrix is not
     * mutated.
     */
    public static Matrix4f translateMatrix(Matrix4f matrix, Vector3f vector) {
        return new Matrix4f(matrix).translate(vector);
    }

    /**
     * Translates {@code matrix} in-place by {@code vector} and returns it. Use this instead of {@link #translateMatrix}
     * when you already own the matrix and don't need the original preserved, to avoid the extra allocation.
     */
    public static Matrix4f translateMatrixInPlace(Matrix4f matrix, Vector3f vector) {
        return matrix.translate(vector);
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
        }

        if (originalTexture == null)
            return null;

        NativeImage image = null;

        try {
            image = originalTexture instanceof DynamicTexture dynamicTexture
                ? dynamicTexture.getPixels()
                : NativeImage.read(mc.getResourceManager().getResource(texture).get().open());
        } catch (Exception e) {
            AzureLib.LOGGER.error("Failed to read image for id {}", texture);
        }

        return image == null ? null : IntIntImmutablePair.of(image.getWidth(), image.getHeight());
    }

    public static double getCurrentSystemTick() {
        return System.nanoTime() / 1E6 / 50d;
    }

    /**
     * Returns the current time (in ticks) that the {@link org.lwjgl.glfw.GLFW GLFW} instance has been running. This is
     * effectively a permanent timer that counts up since the game was launched.
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
     * If a {@link GeoCube} is a 2d plane the {@link GeoQuad Quad's} normal is inverted in an intersecting plane,it can
     * cause issues with shaders and other lighting tasks.<br>
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
}
