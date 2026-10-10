package mod.azure.azurelib.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.FloatBuffer;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.IntIntPair;
import mod.azure.azurelib.util.math.Direction;
import mod.azure.azurelib.util.math.Matrix4f;
import mod.azure.azurelib.util.math.Mth;
import mod.azure.azurelib.util.math.Quaternion;
import mod.azure.azurelib.util.math.Vec3;
import mod.azure.azurelib.util.math.Vector3f;

/**
 * Helper class for various methods and functions useful while rendering
 */
public final class RenderUtils {

    private static final Matrix4f INVERT_SCRATCH = new Matrix4f();

    private static final Quaternion QX = new Quaternion(0, 0, 0, 1);

    private static final Quaternion QY = new Quaternion(0, 0, 0, 1);

    private static final Quaternion QZ = new Quaternion(0, 0, 0, 1);

    public static void translateMatrixToBone(PoseStack poseStack, AzBone bone) {
        poseStack.translate(-bone.getPosX() / 16f, bone.getPosY() / 16f, bone.getPosZ() / 16f);
    }

    public static void rotateMatrixAroundBone(PoseStack poseStack, AzBone bone) {
        float rz = bone.getRotZ();
        float ry = bone.getRotY();
        float rx = bone.getRotX();

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

    /**
     * Inverts {@code inputMatrix} and multiplies it by {@code baseMatrix}, returning the result as a new
     * {@link Matrix4f}. Uses a static scratch buffer for the inversion to avoid per-call heap allocation.
     */
    public static Matrix4f invertAndMultiplyMatrices(Matrix4f baseMatrix, Matrix4f inputMatrix) {
        INVERT_SCRATCH.load(inputMatrix);
        INVERT_SCRATCH.invert();
        INVERT_SCRATCH.multiply(baseMatrix);
        return new Matrix4f(INVERT_SCRATCH);
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
            Vector3f.YP.rotationDegrees(Mth.lerp(partialTick, animatable.prevRotationYaw, animatable.rotationYaw) - 90)
        );
        poseStack.mulPose(
            Vector3f.ZP.rotationDegrees(Mth.lerp(partialTick, animatable.prevRotationPitch, animatable.rotationPitch))
        );
    }

    /**
     * Returns a new {@link Matrix4f} equal to {@code matrix} translated by {@code vector}. The original matrix is not
     * mutated.
     */
    public static Matrix4f translateMatrix(Matrix4f matrix, Vector3f vector) {
        Matrix4f copy = matrix.copy();
        copy.translate(vector);
        return copy;
    }

    /**
     * Translates {@code matrix} in-place by {@code vector} and returns it. Use this instead of {@link #translateMatrix}
     * when you already own the matrix and don't need the original preserved, to avoid the extra allocation.
     */
    public static Matrix4f translateMatrixInPlace(Matrix4f matrix, Vector3f vector) {
        matrix.translate(vector);
        return matrix;
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

        Minecraft mc = Minecraft.getMinecraft();

        try {
            // 1.7.10's IResource isn't Closeable; only its stream needs closing.
            IResource resource = mc.getResourceManager().getResource(texture);
            try (InputStream stream = resource.getInputStream()) {
                BufferedImage image = ImageIO.read(stream);
                return IntIntPair.of(image.getWidth(), image.getHeight());
            }
        } catch (Exception ignored) {
            // Not backed by a resource (e.g. a dynamic texture), fall back to asking GL for the uploaded size.
        }

        try {
            ITextureObject textureObject = mc.getTextureManager().getTexture(texture);
            if (textureObject == null) {
                mc.getTextureManager().bindTexture(texture);
                textureObject = mc.getTextureManager().getTexture(texture);
            }
            if (textureObject == null)
                return null;
            GlStateManager.bindTexture(textureObject.getGlTextureId());
            int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            return width > 0 && height > 0 ? IntIntPair.of(width, height) : null;
        } catch (Exception e) {
            AzureLib.LOGGER.error("Failed to read image dimensions for id {}", texture, e);
            return null;
        }
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
        return System.nanoTime() / 1E9 * 20d;
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
     * Rotates a {@link AzBone} to match a provided {@link ModelRenderer}'s rotations.<br>
     * Usually used for items or armor rendering to match the rotations of other non-geo model parts.
     */
    public static void matchModelPartRot(ModelRenderer from, AzBone to) {
        to.updateRotation(-from.rotateAngleX, -from.rotateAngleY, from.rotateAngleZ);
    }

    /**
     * If a {@link GeoCube} is a 2d plane the {@link mod.azure.azurelib.cache.object.GeoQuad Quad's} normal is inverted
     * in an intersecting plane,it can cause issues with shaders and other lighting tasks.<br>
     * This performs a pseudo-ABS function to help resolve some of those issues.
     */
    public static void fixInvertedFlatCube(GeoCube cube, Vector3f normal) {
        fixInvertedFlatCube(cube.normalFlips(), normal);
    }

    /**
     * {@link #fixInvertedFlatCube(GeoCube, Vector3f)} with the cube's flip flags already worked out; see
     * {@link GeoCube#normalFlips()}.
     */
    public static void fixInvertedFlatCube(int normalFlips, Vector3f normal) {
        if (normalFlips == 0)
            return;

        if (normal.x() < 0 && (normalFlips & GeoCube.FLIP_X) != 0)
            normal.mul(-1, 1, 1);

        if (normal.y() < 0 && (normalFlips & GeoCube.FLIP_Y) != 0)
            normal.mul(1, -1, 1);

        if (normal.z() < 0 && (normalFlips & GeoCube.FLIP_Z) != 0)
            normal.mul(1, 1, -1);
    }

    /**
     * Converts a {@link Direction} to a rotational float for rotation purposes
     */
    public static float getDirectionAngle(Direction direction) {
        switch (direction) {
            case SOUTH:
                return 90f;
            case NORTH:
                return 270f;
            case EAST:
                return 180f;
            default:
                return 0f;
        }
    }

    private static final FloatBuffer GL_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);

    /**
     * Multiplies the current OpenGL model-view matrix by the given pose, so vanilla 1.7.10 code that renders through
     * the GL matrix stack (model parts, items, blocks) lines up with AzureLib's software {@link PoseStack}. Callers
     * must wrap this in {@code GlStateManager.pushMatrix()}/{@code popMatrix()} and flush any pending AzureLib vertices
     * first.
     */
    public static void applyPoseToGl(PoseStack.Pose pose) {
        GL_MATRIX_BUFFER.clear();
        pose.pose().store(GL_MATRIX_BUFFER);
        GL_MATRIX_BUFFER.flip();
        GlStateManager.multMatrix(GL_MATRIX_BUFFER);
    }

    /**
     * The lightmap coordinates most recently set through {@link OpenGlHelper#setLightmapTextureCoords}, packed the same
     * way as {@code Entity#getBrightnessForRender()}. This is how 1.7.10 passes light to item and armor rendering,
     * which have no packed-light parameter.
     */
    public static int currentPackedLight() {
        return ((int) OpenGlHelper.lastBrightnessY & 0xFFFF) << 16 | (int) OpenGlHelper.lastBrightnessX & 0xFFFF;
    }

    private RenderUtils() {
        throw new UnsupportedOperationException();
    }
}
