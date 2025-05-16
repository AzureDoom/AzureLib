/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import mod.azure.azurelib.util.JsonUtil;

import javax.annotation.Nullable;

/**
 * Container class for model property information, only used in deserialization at startup
 */
public record ModelProperties(Boolean animationArmsDown, Boolean animationArmsOutFront,
							  Boolean animationDontShowArmor, Boolean animationInvertedCrouch,
							  Boolean animationNoHeadBob, Boolean animationSingleArmAnimation,
							  Boolean animationSingleLegAnimation, Boolean animationStationaryLegs,
							  Boolean animationStatueOfLibertyArms, Boolean animationUpsideDown,
							  String identifier, Boolean preserveModelPose,
							  double textureHeight, double textureWidth,
							  Double visibleBoundsHeight, double[] visibleBoundsOffset,
							  Double visibleBoundsWidth) {
	public static JsonDeserializer<ModelProperties> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			Boolean animationArmsDown = JsonUtil.getOptionalBoolean(obj, "animationArmsDown");
			Boolean animationArmsOutFront = JsonUtil.getOptionalBoolean(obj, "animationArmsOutFront");
			Boolean animationDontShowArmor = JsonUtil.getOptionalBoolean(obj, "animationDontShowArmor");
			Boolean animationInvertedCrouch = JsonUtil.getOptionalBoolean(obj, "animationInvertedCrouch");
			Boolean animationNoHeadBob = JsonUtil.getOptionalBoolean(obj, "animationNoHeadBob");
			Boolean animationSingleArmAnimation = JsonUtil.getOptionalBoolean(obj, "animationSingleArmAnimation");
			Boolean animationSingleLegAnimation = JsonUtil.getOptionalBoolean(obj, "animationSingleLegAnimation");
			Boolean animationStationaryLegs = JsonUtil.getOptionalBoolean(obj, "animationStationaryLegs");
			Boolean animationStatueOfLibertyArms = JsonUtil.getOptionalBoolean(obj, "animationStatueOfLibertyArms");
			Boolean animationUpsideDown = JsonUtil.getOptionalBoolean(obj, "animationUpsideDown");
			String identifier = GsonHelper.getAsString(obj, "identifier", null);
			Boolean preserveModelPose = JsonUtil.getOptionalBoolean(obj, "preserve_model_pose");
			double textureHeight = GsonHelper.getAsDouble(obj, "texture_height");
			double textureWidth = GsonHelper.getAsDouble(obj, "texture_width");
			Double visibleBoundsHeight = JsonUtil.getOptionalDouble(obj, "visible_bounds_height");
			double[] visibleBoundsOffset = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "visible_bounds_offset", null));
			Double visibleBoundsWidth = JsonUtil.getOptionalDouble(obj, "visible_bounds_width");

			return new ModelProperties(animationArmsDown, animationArmsOutFront, animationDontShowArmor, animationInvertedCrouch,
					animationNoHeadBob, animationSingleArmAnimation, animationSingleLegAnimation, animationStationaryLegs,
					animationStatueOfLibertyArms, animationUpsideDown, identifier, preserveModelPose, textureHeight,
					textureWidth, visibleBoundsHeight, visibleBoundsOffset, visibleBoundsWidth);
		};
	}
}
