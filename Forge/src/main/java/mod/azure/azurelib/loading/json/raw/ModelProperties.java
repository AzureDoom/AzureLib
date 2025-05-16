/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JSONUtils;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for model property information, only used in deserialization at startup
 */
public class ModelProperties {

    private final Boolean animationArmsDown;

    private final Boolean animationArmsOutFront;

    private final Boolean animationDontShowArmor;

    private final Boolean animationInvertedCrouch;

    private final Boolean animationNoHeadBob;

    private final Boolean animationSingleArmAnimation;

    private final Boolean animationSingleLegAnimation;

    private final Boolean animationStationaryLegs;

    private final Boolean animationStatueOfLibertyArms;

    private final Boolean animationUpsideDown;

    private final String identifier;

    private final Boolean preserveModelPose;

    private final double textureHeight;

    private final double textureWidth;

    private final Double visibleBoundsHeight;

    private final double[] visibleBoundsOffset;

    private final Double visibleBoundsWidth;

    public ModelProperties(
        Boolean animationArmsDown,
        Boolean animationArmsOutFront,
        Boolean animationDontShowArmor,
        Boolean animationInvertedCrouch,
        Boolean animationNoHeadBob,
        Boolean animationSingleArmAnimation,
        Boolean animationSingleLegAnimation,
        Boolean animationStationaryLegs,
        Boolean animationStatueOfLibertyArms,
        Boolean animationUpsideDown,
        String identifier,
        Boolean preserveModelPose,
        double textureHeight,
        double textureWidth,
        Double visibleBoundsHeight,
        double[] visibleBoundsOffset,
        Double visibleBoundsWidth
    ) {
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

    public Boolean animationArmsDown() {
        return animationArmsDown;
    }

    public Boolean animationArmsOutFront() {
        return animationArmsOutFront;
    }

    public Boolean animationDontShowArmor() {
        return animationDontShowArmor;
    }

    public Boolean animationInvertedCrouch() {
        return animationInvertedCrouch;
    }

    public Boolean animationNoHeadBob() {
        return animationNoHeadBob;
    }

    public Boolean animationSingleArmAnimation() {
        return animationSingleArmAnimation;
    }

    public Boolean animationSingleLegAnimation() {
        return animationSingleLegAnimation;
    }

    public Boolean animationStationaryLegs() {
        return animationStationaryLegs;
    }

    public Boolean animationStatueOfLibertyArms() {
        return animationStatueOfLibertyArms;
    }

    public Boolean animationUpsideDown() {
        return animationUpsideDown;
    }

    public String identifier() {
        return identifier;
    }

    public Boolean preserveModelPose() {
        return preserveModelPose;
    }

    public double textureHeight() {
        return textureHeight;
    }

    public double textureWidth() {
        return textureWidth;
    }

    public Double visibleBoundsHeight() {
        return visibleBoundsHeight;
    }

    public double[] visibleBoundsOffset() {
        return visibleBoundsOffset;
    }

    public Double visibleBoundsWidth() {
        return visibleBoundsWidth;
    }

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
            String identifier = JSONUtils.getAsString(obj, "identifier", null);
            Boolean preserveModelPose = JsonUtil.getOptionalBoolean(obj, "preserve_model_pose");
            double textureHeight = JsonUtil.getAsDouble(obj, "texture_height");
            double textureWidth = JsonUtil.getAsDouble(obj, "texture_width");
            Double visibleBoundsHeight = JsonUtil.getOptionalDouble(obj, "visible_bounds_height");
            double[] visibleBoundsOffset = JsonUtil.jsonArrayToDoubleArray(
                JSONUtils.getAsJsonArray(obj, "visible_bounds_offset", null)
            );
            Double visibleBoundsWidth = JsonUtil.getOptionalDouble(obj, "visible_bounds_width");

            return new ModelProperties(
                animationArmsDown,
                animationArmsOutFront,
                animationDontShowArmor,
                animationInvertedCrouch,
                animationNoHeadBob,
                animationSingleArmAnimation,
                animationSingleLegAnimation,
                animationStationaryLegs,
                animationStatueOfLibertyArms,
                animationUpsideDown,
                identifier,
                preserveModelPose,
                textureHeight,
                textureWidth,
                visibleBoundsHeight,
                visibleBoundsOffset,
                visibleBoundsWidth
            );
        };
    }
}
