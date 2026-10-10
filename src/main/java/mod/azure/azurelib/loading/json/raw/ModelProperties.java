/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.GsonHelper;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for model property information, only used in deserialization at startup
 */
public final class ModelProperties {

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
        return this.animationArmsDown;
    }

    public Boolean animationArmsOutFront() {
        return this.animationArmsOutFront;
    }

    public Boolean animationDontShowArmor() {
        return this.animationDontShowArmor;
    }

    public Boolean animationInvertedCrouch() {
        return this.animationInvertedCrouch;
    }

    public Boolean animationNoHeadBob() {
        return this.animationNoHeadBob;
    }

    public Boolean animationSingleArmAnimation() {
        return this.animationSingleArmAnimation;
    }

    public Boolean animationSingleLegAnimation() {
        return this.animationSingleLegAnimation;
    }

    public Boolean animationStationaryLegs() {
        return this.animationStationaryLegs;
    }

    public Boolean animationStatueOfLibertyArms() {
        return this.animationStatueOfLibertyArms;
    }

    public Boolean animationUpsideDown() {
        return this.animationUpsideDown;
    }

    public String identifier() {
        return this.identifier;
    }

    public Boolean preserveModelPose() {
        return this.preserveModelPose;
    }

    public double textureHeight() {
        return this.textureHeight;
    }

    public double textureWidth() {
        return this.textureWidth;
    }

    public Double visibleBoundsHeight() {
        return this.visibleBoundsHeight;
    }

    public double[] visibleBoundsOffset() {
        return this.visibleBoundsOffset;
    }

    public Double visibleBoundsWidth() {
        return this.visibleBoundsWidth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ModelProperties))
            return false;
        ModelProperties other = (ModelProperties) o;
        return java.util.Objects.equals(this.animationArmsDown, other.animationArmsDown)
            && java.util.Objects.equals(this.animationArmsOutFront, other.animationArmsOutFront)
            && java.util.Objects.equals(this.animationDontShowArmor, other.animationDontShowArmor)
            && java.util.Objects.equals(this.animationInvertedCrouch, other.animationInvertedCrouch)
            && java.util.Objects.equals(this.animationNoHeadBob, other.animationNoHeadBob)
            && java.util.Objects.equals(this.animationSingleArmAnimation, other.animationSingleArmAnimation)
            && java.util.Objects.equals(this.animationSingleLegAnimation, other.animationSingleLegAnimation)
            && java.util.Objects.equals(this.animationStationaryLegs, other.animationStationaryLegs)
            && java.util.Objects.equals(this.animationStatueOfLibertyArms, other.animationStatueOfLibertyArms)
            && java.util.Objects.equals(this.animationUpsideDown, other.animationUpsideDown)
            && java.util.Objects.equals(this.identifier, other.identifier)
            && java.util.Objects.equals(this.preserveModelPose, other.preserveModelPose)
            && Double.compare(this.textureHeight, other.textureHeight) == 0
            && Double.compare(this.textureWidth, other.textureWidth) == 0
            && java.util.Objects.equals(this.visibleBoundsHeight, other.visibleBoundsHeight)
            && java.util.Objects.equals(this.visibleBoundsOffset, other.visibleBoundsOffset)
            && java.util.Objects.equals(this.visibleBoundsWidth, other.visibleBoundsWidth);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.animationArmsDown);
        result = 31 * result + java.util.Objects.hashCode(this.animationArmsOutFront);
        result = 31 * result + java.util.Objects.hashCode(this.animationDontShowArmor);
        result = 31 * result + java.util.Objects.hashCode(this.animationInvertedCrouch);
        result = 31 * result + java.util.Objects.hashCode(this.animationNoHeadBob);
        result = 31 * result + java.util.Objects.hashCode(this.animationSingleArmAnimation);
        result = 31 * result + java.util.Objects.hashCode(this.animationSingleLegAnimation);
        result = 31 * result + java.util.Objects.hashCode(this.animationStationaryLegs);
        result = 31 * result + java.util.Objects.hashCode(this.animationStatueOfLibertyArms);
        result = 31 * result + java.util.Objects.hashCode(this.animationUpsideDown);
        result = 31 * result + java.util.Objects.hashCode(this.identifier);
        result = 31 * result + java.util.Objects.hashCode(this.preserveModelPose);
        result = 31 * result + Double.hashCode(this.textureHeight);
        result = 31 * result + Double.hashCode(this.textureWidth);
        result = 31 * result + java.util.Objects.hashCode(this.visibleBoundsHeight);
        result = 31 * result + java.util.Objects.hashCode(this.visibleBoundsOffset);
        result = 31 * result + java.util.Objects.hashCode(this.visibleBoundsWidth);
        return result;
    }

    @Override
    public String toString() {
        return "ModelProperties[animationArmsDown=" + this.animationArmsDown + ", animationArmsOutFront="
            + this.animationArmsOutFront + ", animationDontShowArmor=" + this.animationDontShowArmor
            + ", animationInvertedCrouch=" + this.animationInvertedCrouch + ", animationNoHeadBob="
            + this.animationNoHeadBob + ", animationSingleArmAnimation=" + this.animationSingleArmAnimation
            + ", animationSingleLegAnimation=" + this.animationSingleLegAnimation + ", animationStationaryLegs="
            + this.animationStationaryLegs + ", animationStatueOfLibertyArms=" + this.animationStatueOfLibertyArms
            + ", animationUpsideDown=" + this.animationUpsideDown + ", identifier=" + this.identifier
            + ", preserveModelPose=" + this.preserveModelPose + ", textureHeight=" + this.textureHeight
            + ", textureWidth=" + this.textureWidth + ", visibleBoundsHeight=" + this.visibleBoundsHeight
            + ", visibleBoundsOffset=" + this.visibleBoundsOffset + ", visibleBoundsWidth=" + this.visibleBoundsWidth
            + "]";
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
            String identifier = GsonHelper.getAsString(obj, "identifier", null);
            Boolean preserveModelPose = JsonUtil.getOptionalBoolean(obj, "preserve_model_pose");
            double textureHeight = GsonHelper.getAsDouble(obj, "texture_height");
            double textureWidth = GsonHelper.getAsDouble(obj, "texture_width");
            Double visibleBoundsHeight = JsonUtil.getOptionalDouble(obj, "visible_bounds_height");
            double[] visibleBoundsOffset = JsonUtil.jsonArrayToDoubleArray(
                GsonHelper.getAsJsonArray(obj, "visible_bounds_offset", null)
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
