package mod.azure.azurelib.loading.json.raw;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.util.GsonHelper;

/**
 * Container class for model property information, only used in deserialization at startup
 */
public class ModelProperties {

	@Nullable
	public Boolean animationArmsDown;
	@Nullable
	public Boolean animationArmsOutFront;
	@Nullable
	public Boolean animationDontShowArmor;
	@Nullable
	public Boolean animationInvertedCrouch;
	@Nullable
	public Boolean animationNoHeadBob;
	@Nullable
	public Boolean animationSingleArmAnimation;
	@Nullable
	public Boolean animationSingleLegAnimation;
	@Nullable
	public Boolean animationStationaryLegs;
	@Nullable
	public Boolean animationStatueOfLibertyArms;
	@Nullable
	public Boolean animationUpsideDown;
	@Nullable
	public String identifier;
	@Nullable
	public Boolean preserveModelPose;
	public double textureHeight;
	public double textureWidth;
	@Nullable
	public Double visibleBoundsHeight;
	public double[] visibleBoundsOffset;
	@Nullable
	public Double visibleBoundsWidth;

	public ModelProperties(@Nullable Boolean animationArmsDown, @Nullable Boolean animationArmsOutFront, @Nullable Boolean animationDontShowArmor, @Nullable Boolean animationInvertedCrouch, @Nullable Boolean animationNoHeadBob, @Nullable Boolean animationSingleArmAnimation, @Nullable Boolean animationSingleLegAnimation, @Nullable Boolean animationStationaryLegs, @Nullable Boolean animationStatueOfLibertyArms, @Nullable Boolean animationUpsideDown, @Nullable String identifier,
			@Nullable Boolean preserveModelPose, double textureHeight, double textureWidth, @Nullable Double visibleBoundsHeight, double[] visibleBoundsOffset, @Nullable Double visibleBoundsWidth) {
		this.animationArmsDown = animationArmsDown;
		this.animationArmsOutFront = animationArmsOutFront;
		this.animationDontShowArmor = animationDontShowArmor;
		this.animationInvertedCrouch = animationInvertedCrouch;
		this.animationNoHeadBob = animationNoHeadBob;
		this.animationSingleArmAnimation = animationSingleArmAnimation;
		this.animationSingleLegAnimation = animationSingleLegAnimation;
		this.animationStationaryLegs = animationStationaryLegs;
		this.animationStatueOfLibertyArms = animationStatueOfLibertyArms;
		this.animationUpsideDown = animationUpsideDown;
		this.identifier = identifier;
		this.preserveModelPose = preserveModelPose;
		this.textureHeight = textureHeight;
		this.textureWidth = textureWidth;
		this.visibleBoundsHeight = visibleBoundsHeight;
		this.visibleBoundsOffset = visibleBoundsOffset;
		this.visibleBoundsWidth = visibleBoundsWidth;
	}

	@Nullable
	public Boolean animationArmsDown() {
		return animationArmsDown;
	}

	@Nullable
	public Boolean animationArmsOutFront() {
		return animationArmsOutFront;
	}

	@Nullable
	public Boolean animationDontShowArmor() {
		return animationDontShowArmor;
	}

	@Nullable
	public Boolean animationInvertedCrouch() {
		return animationInvertedCrouch;
	}

	@Nullable
	public Boolean animationNoHeadBob() {
		return animationNoHeadBob;
	}

	@Nullable
	public Boolean animationSingleArmAnimation() {
		return animationSingleArmAnimation;
	}

	@Nullable
	public Boolean animationSingleLegAnimation() {
		return animationSingleLegAnimation;
	}

	@Nullable
	public Boolean animationStationaryLegs() {
		return animationStationaryLegs;
	}

	@Nullable
	public Boolean animationStatueOfLibertyArms() {
		return animationStatueOfLibertyArms;
	}

	@Nullable
	public Boolean animationUpsideDown() {
		return animationUpsideDown;
	}

	@Nullable
	public String identifier() {
		return identifier;
	}

	@Nullable
	public Boolean preserveModelPose() {
		return preserveModelPose;
	}

	public double textureHeight() {
		return textureHeight;
	}

	public double textureWidth() {
		return textureWidth;
	}

	@Nullable
	public Double visibleBoundsHeight() {
		return visibleBoundsHeight;
	}

	public double[] visibleBoundsOffset() {
		return visibleBoundsOffset;
	}

	@Nullable
	public Double visibleBoundsWidth() {
		return visibleBoundsWidth;
	}

	public static JsonDeserializer<ModelProperties> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			final JsonObject obj = json.getAsJsonObject();
			final Boolean animationArmsDown = JsonUtil.getOptionalBoolean(obj, "animationArmsDown");
			final Boolean animationArmsOutFront = JsonUtil.getOptionalBoolean(obj, "animationArmsOutFront");
			final Boolean animationDontShowArmor = JsonUtil.getOptionalBoolean(obj, "animationDontShowArmor");
			final Boolean animationInvertedCrouch = JsonUtil.getOptionalBoolean(obj, "animationInvertedCrouch");
			final Boolean animationNoHeadBob = JsonUtil.getOptionalBoolean(obj, "animationNoHeadBob");
			final Boolean animationSingleArmAnimation = JsonUtil.getOptionalBoolean(obj, "animationSingleArmAnimation");
			final Boolean animationSingleLegAnimation = JsonUtil.getOptionalBoolean(obj, "animationSingleLegAnimation");
			final Boolean animationStationaryLegs = JsonUtil.getOptionalBoolean(obj, "animationStationaryLegs");
			final Boolean animationStatueOfLibertyArms = JsonUtil.getOptionalBoolean(obj, "animationStatueOfLibertyArms");
			final Boolean animationUpsideDown = JsonUtil.getOptionalBoolean(obj, "animationUpsideDown");
			final String identifier = GsonHelper.getAsString(obj, "identifier", null);
			final Boolean preserveModelPose = JsonUtil.getOptionalBoolean(obj, "preserve_model_pose");
			final double textureHeight = GsonHelper.getAsFloat(obj, "texture_height");
			final double textureWidth = GsonHelper.getAsFloat(obj, "texture_width");
			final Double visibleBoundsHeight = JsonUtil.getOptionalDouble(obj, "visible_bounds_height");
			final double[] visibleBoundsOffset = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "visible_bounds_offset", null));
			final Double visibleBoundsWidth = JsonUtil.getOptionalDouble(obj, "visible_bounds_width");

			return new ModelProperties(animationArmsDown, animationArmsOutFront, animationDontShowArmor, animationInvertedCrouch, animationNoHeadBob, animationSingleArmAnimation, animationSingleLegAnimation, animationStationaryLegs, animationStatueOfLibertyArms, animationUpsideDown, identifier, preserveModelPose, textureHeight, textureWidth, visibleBoundsHeight, visibleBoundsOffset, visibleBoundsWidth);
		};
	}
}
