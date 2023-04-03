package mod.azure.azurelib.util;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.GeoReplacedEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.RenderProperties;

/**
 * Helper class for various methods and functions useful while rendering
 */
public final class RenderUtils {
	public static void translateMatrixToBone(MatrixStack poseStack, CoreGeoBone bone) {
		poseStack.translate(-bone.getPosX() / 16f, bone.getPosY() / 16f, bone.getPosZ() / 16f);
	}

	public static void rotateMatrixAroundBone(MatrixStack poseStack, CoreGeoBone bone) {
		if (bone.getRotZ() != 0)
			poseStack.mulPose(Vector3f.ZP.rotation(bone.getRotZ()));

		if (bone.getRotY() != 0)
			poseStack.mulPose(Vector3f.YP.rotation(bone.getRotY()));

		if (bone.getRotX() != 0)
			poseStack.mulPose(Vector3f.XP.rotation(bone.getRotX()));
	}

	public static void rotateMatrixAroundCube(MatrixStack poseStack, GeoCube cube) {
		Vector3d rotation = cube.rotation();

		poseStack.mulPose(new Quaternion(0, 0, (float) rotation.z(), false));
		poseStack.mulPose(new Quaternion(0, (float) rotation.y(), 0, false));
		poseStack.mulPose(new Quaternion((float) rotation.x(), 0, 0, false));
	}

	public static void scaleMatrixForBone(MatrixStack poseStack, CoreGeoBone bone) {
		poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
	}

	public static void translateToPivotPoint(MatrixStack poseStack, GeoCube cube) {
		Vector3d pivot = cube.pivot();
		poseStack.translate(pivot.x() / 16f, pivot.y() / 16f, pivot.z() / 16f);
	}

	public static void translateToPivotPoint(MatrixStack poseStack, CoreGeoBone bone) {
		poseStack.translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f);
	}

	public static void translateAwayFromPivotPoint(MatrixStack poseStack, GeoCube cube) {
		Vector3d pivot = cube.pivot();

		poseStack.translate(-pivot.x() / 16f, -pivot.y() / 16f, -pivot.z() / 16f);
	}

	public static void translateAwayFromPivotPoint(MatrixStack poseStack, CoreGeoBone bone) {
		poseStack.translate(-bone.getPivotX() / 16f, -bone.getPivotY() / 16f, -bone.getPivotZ() / 16f);
	}

	public static void translateAndRotateMatrixForBone(MatrixStack poseStack, CoreGeoBone bone) {
		translateToPivotPoint(poseStack, bone);
		rotateMatrixAroundBone(poseStack, bone);
	}

	public static void prepMatrixForBone(MatrixStack poseStack, CoreGeoBone bone) {
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
	 * Translates the provided {@link MatrixStack} to face towards the given {@link Entity}'s rotation.<br>
	 * Usually used for rotating projectiles towards their trajectory, in an {@link GeoRenderer#preRender} override.<br>
	 */
	public static void faceRotation(MatrixStack poseStack, Entity animatable, float partialTick) {
		poseStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTick, animatable.yRotO, animatable.yRot) - 90));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTick, animatable.xRotO, animatable.xRot)));
	}

	/**
	 * Gets the actual dimensions of a texture resource from a given path.<br>
	 * Not performance-efficient, and should not be relied upon
	 * 
	 * @param texture The path of the texture resource to check
	 * @return The dimensions (width x height) of the texture, or null if unable to find or read the file
	 */
	@Nullable
	public static Tuple<Integer, Integer> getTextureDimensions(ResourceLocation texture) {
		if (texture == null)
			return null;

		Texture originalTexture = null;
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
			image = originalTexture instanceof DynamicTexture ? ((DynamicTexture) originalTexture).getPixels() : NativeImage.read(mc.getResourceManager().getResource(texture).getInputStream());
		} catch (Exception e) {
			AzureLib.LOGGER.error("Failed to read image for id {}", texture);
			e.printStackTrace();
		}

		return image == null ? null : new Tuple<Integer, Integer>(image.getWidth(), image.getHeight());
	}

	public static double getCurrentSystemTick() {
		return System.nanoTime() / 1E6 / 50d;
	}

	/**
	 * Returns the current time (in ticks) that the {@link org.lwjgl.glfw.GLFW GLFW} instance has been running. This is effectively a permanent timer that counts up since the game was launched.
	 */
	public static double getCurrentTick() {
		return NativeUtil.getTime() * 20d;
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
	public static Vector3d arrayToVec(double[] array) {
		return new Vector3d(array[0], array[1], array[2]);
	}

	/**
	 * Rotates a {@link CoreGeoBone} to match a provided {@link ModelPart}'s rotations.<br>
	 * Usually used for items or armor rendering to match the rotations of other non-geo model parts.
	 */
	public static void matchModelPartRot(ModelRenderer from, CoreGeoBone to) {
		to.updateRotation(-from.xRot, -from.yRot, from.zRot);
	}

	/**
	 * If a {@link GeoCube} is a 2d plane the {@link mod.azure.azurelib.cache.object.GeoQuad Quad's} normal is inverted in an intersecting plane,it can cause issues with shaders and other lighting tasks.<br>
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
		if (direction.equals(Direction.NORTH))
			return 270f;
		else if (direction.equals(Direction.SOUTH))
			return 90f;
		else if (direction.equals(Direction.EAST))
			return 180f;
		else
			return 0f;
	}

	/**
	 * Gets a {@link GeoModel} instance from a given {@link EntityType}.<br>
	 * This only works if you're calling this method for an EntityType known to be using a {@link GeoRenderer AzureLib Renderer}.<br>
	 * Generally speaking you probably shouldn't be calling this method at all.
	 * 
	 * @param entityType The {@code EntityType} to retrieve the GeoModel for
	 * @return The GeoModel, or null if one isn't found
	 */
	@Nullable
	public static GeoModel<?> getGeoModelForEntityType(EntityType<?> entityType) {
		EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entityType);

		return renderer instanceof GeoRenderer<?> ? ((GeoRenderer<?>) renderer).getGeoModel() : null;
	}

	/**
	 * Gets a GeoAnimatable instance that has been registered as the replacement renderer for a given {@link EntityType}
	 * 
	 * @param entityType The {@code EntityType} to retrieve the replaced {@link GeoAnimatable} for
	 * @return The {@code GeoAnimatable} instance, or null if one isn't found
	 */
	@Nullable
	public static GeoAnimatable getReplacedAnimatable(EntityType<?> entityType) {
		EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entityType);

		return renderer instanceof GeoReplacedEntityRenderer<?, ?> ? ((GeoReplacedEntityRenderer<?, ?>) renderer).getAnimatable() : null;
	}

	/**
	 * Gets a {@link GeoModel} instance from a given {@link Entity}.<br>
	 * This only works if you're calling this method for an Entity known to be using a {@link GeoRenderer AzureLib Renderer}.<br>
	 * Generally speaking you probably shouldn't be calling this method at all.
	 * 
	 * @param entity The {@code Entity} to retrieve the GeoModel for
	 * @return The GeoModel, or null if one isn't found
	 */
	@Nullable
	public static GeoModel<?> getGeoModelForEntity(Entity entity) {
		EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);

		return renderer instanceof GeoRenderer<?> ? ((GeoRenderer<?>) renderer).getGeoModel() : null;
	}

	/**
	 * Gets a {@link GeoModel} instance from a given {@link Item}.<br>
	 * This only works if you're calling this method for an Item known to be using a {@link GeoRenderer AzureLib Renderer}.<br>
	 * Generally speaking you probably shouldn't be calling this method at all.
	 * 
	 * @param item The {@code Item} to retrieve the GeoModel for
	 * @return The GeoModel, or null if one isn't found
	 */
	@Nullable
	public static GeoModel<?> getGeoModelForItem(Item item) {
		if (RenderProperties.get(item).getItemStackRenderer() instanceof GeoRenderer<?>)
			return RenderProperties.get(item).getItemStackRenderer().getGeoModel();

		return null;
	}

	/**
	 * Gets a {@link GeoModel} instance from a given {@link BlockEntity}.<br>
	 * This only works if you're calling this method for a BlockEntity known to be using a {@link GeoRenderer AzureLib Renderer}.<br>
	 * Generally speaking you probably shouldn't be calling this method at all.
	 * 
	 * @param blockEntity The {@code BlockEntity} to retrieve the GeoModel for
	 * @return The GeoModel, or null if one isn't found
	 */
	@Nullable
	public static GeoModel<?> getGeoModelForBlock(TileEntity blockEntity) {
		TileEntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(blockEntity);

		return renderer instanceof GeoRenderer<?> ? ((GeoRenderer<?>) renderer).getGeoModel() : null;
	}

	/**
	 * Gets a {@link GeoModel} instance from a given {@link Item}.<br>
	 * This only works if you're calling this method for an Item known to be using a {@link mod.azure.azurelib.renderer.GeoArmorRenderer GeoArmorRenderer}.<br>
	 * Generally speaking you probably shouldn't be calling this method at all.
	 * 
	 * @param stack The ItemStack to retrieve the GeoModel for
	 * @return The GeoModel, or null if one isn't found
	 */
	@Nullable
	public static GeoModel<?> getGeoModelForArmor(ItemStack stack) {
		if (RenderProperties.get(stack).getArmorModel(null, stack, null, null) instanceof GeoArmorRenderer<?>)
			return RenderProperties.get(stack).getArmorModel(null, stack, null, null).getGeoModel();

		return null;
	}
}
