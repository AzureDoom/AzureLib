package mod.azure.azurelib.render.armor.bone;

import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.render.model.entity.HumanoidModel;

import javax.annotation.Nullable;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.client.RenderUtils;

public class AzArmorBoneContext {

    private AzBakedModel lastModel;

    public AzBone head;

    public AzBone body;

    public AzBone rightArm;

    public AzBone leftArm;

    public AzBone rightLeg;

    public AzBone leftLeg;

    public AzBone rightBoot;

    public AzBone leftBoot;

    public AzBone waist;

    public AzArmorBoneContext() {
        this.head = null;
        this.body = null;
        this.rightArm = null;
        this.leftArm = null;
        this.rightLeg = null;
        this.leftLeg = null;
        this.rightBoot = null;
        this.leftBoot = null;
        this.waist = null;
    }

    public void setAllVisible(boolean pVisible) {
        setBoneVisible(this.head, pVisible);
        setBoneVisible(this.body, pVisible);
        setBoneVisible(this.rightArm, pVisible);
        setBoneVisible(this.leftArm, pVisible);
        setBoneVisible(this.rightLeg, pVisible);
        setBoneVisible(this.leftLeg, pVisible);
        setBoneVisible(this.rightBoot, pVisible);
        setBoneVisible(this.leftBoot, pVisible);
        setBoneVisible(this.waist, pVisible);
    }

    /**
     * Gets and caches the relevant armor model bones for this baked model if it hasn't been done already
     */
    public void grabRelevantBones(AzBakedModel model, AzArmorBoneProvider boneProvider) {
        if (this.lastModel == model) {
            return;
        }

        this.lastModel = model;
        this.head = boneProvider.getHeadBone(model);
        this.body = boneProvider.getBodyBone(model);
        this.rightArm = boneProvider.getRightArmBone(model);
        this.leftArm = boneProvider.getLeftArmBone(model);
        this.rightLeg = boneProvider.getRightLegBone(model);
        this.leftLeg = boneProvider.getLeftLegBone(model);
        this.rightBoot = boneProvider.getRightBootBone(model);
        this.leftBoot = boneProvider.getLeftBootBone(model);
        this.waist = boneProvider.getWaistBone(model);
    }

    /**
     * Transform the currently rendering {@link AzBakedModel} to match the positions and rotations of the base model
     */
    public void applyBaseTransformations(HumanoidModel baseModel) {
        if (this.head != null) {
            ModelPart headPart = McClient.bipedHead(baseModel);

            RenderUtils.matchModelPartRot(headPart, this.head);
            this.head.updatePosition(McClient.pivotX(headPart), -McClient.pivotY(headPart), McClient.pivotZ(headPart));
        }

        if (this.body != null) {
            ModelPart bodyPart = McClient.bipedBody(baseModel);

            RenderUtils.matchModelPartRot(bodyPart, this.body);
            this.body.updatePosition(McClient.pivotX(bodyPart), -McClient.pivotY(bodyPart), McClient.pivotZ(bodyPart));
        }

        if (this.rightArm != null) {
            ModelPart rightArmPart = McClient.bipedRightArm(baseModel);

            RenderUtils.matchModelPartRot(rightArmPart, this.rightArm);
            this.rightArm.updatePosition(
                McClient.pivotX(rightArmPart) + 5,
                2 - McClient.pivotY(rightArmPart),
                McClient.pivotZ(rightArmPart)
            );
        }

        if (this.leftArm != null) {
            ModelPart leftArmPart = McClient.bipedLeftArm(baseModel);

            RenderUtils.matchModelPartRot(leftArmPart, this.leftArm);
            this.leftArm.updatePosition(
                McClient.pivotX(leftArmPart) - 5f,
                2f - McClient.pivotY(leftArmPart),
                McClient.pivotZ(leftArmPart)
            );
        }

        if (this.rightLeg != null) {
            ModelPart rightLegPart = McClient.bipedRightLeg(baseModel);

            RenderUtils.matchModelPartRot(rightLegPart, this.rightLeg);
            this.rightLeg.updatePosition(
                McClient.pivotX(rightLegPart) + 2,
                12 - McClient.pivotY(rightLegPart),
                McClient.pivotZ(rightLegPart)
            );

            if (this.rightBoot != null) {
                RenderUtils.matchModelPartRot(rightLegPart, this.rightBoot);
                this.rightBoot.updatePosition(
                    McClient.pivotX(rightLegPart) + 2,
                    12 - McClient.pivotY(rightLegPart),
                    McClient.pivotZ(rightLegPart)
                );
            }
            if (this.waist != null) {
                RenderUtils.matchModelPartRot(McClient.bipedBody(baseModel), this.waist);
                this.waist.updatePosition(
                    McClient.pivotX(McClient.bipedBody(baseModel)),
                    -(McClient.pivotY(McClient.bipedBody(baseModel))),
                    McClient.pivotZ(McClient.bipedBody(baseModel))
                );
            }
        }

        if (this.leftLeg != null) {
            ModelPart leftLegPart = McClient.bipedLeftLeg(baseModel);

            RenderUtils.matchModelPartRot(leftLegPart, this.leftLeg);
            this.leftLeg.updatePosition(
                McClient.pivotX(leftLegPart) - 2,
                12 - McClient.pivotY(leftLegPart),
                McClient.pivotZ(leftLegPart)
            );

            if (this.leftBoot != null) {
                RenderUtils.matchModelPartRot(leftLegPart, this.leftBoot);
                this.leftBoot.updatePosition(
                    McClient.pivotX(leftLegPart) - 2,
                    12 - McClient.pivotY(leftLegPart),
                    McClient.pivotZ(leftLegPart)
                );
            }
            if (this.waist != null) {
                RenderUtils.matchModelPartRot(McClient.bipedBody(baseModel), this.waist);
                this.waist.updatePosition(
                    McClient.pivotX(McClient.bipedBody(baseModel)),
                    -(McClient.pivotY(McClient.bipedBody(baseModel))),
                    McClient.pivotZ(McClient.bipedBody(baseModel))
                );
            }
        }
    }

    /**
     * Resets the bone visibility for the model based on the current {@link ModelPart} and {@link AzEquipmentSlot}, and
     * then sets the bones relevant to the current part as visible for rendering.<br>
     * <br>
     * If you are rendering a geo entity with armor, you should probably be calling this prior to rendering
     */
    public void applyBoneVisibilityByPart(
        AzEquipmentSlot currentSlot,
        ModelPart currentPart,
        HumanoidModel model
    ) {
        setAllVisible(false);

        McClient.setVisible(currentPart, true);
        AzBone bone = null;

        if (currentPart == McClient.bipedHat(model) || currentPart == McClient.bipedHead(model)) {
            bone = this.head;
        } else if (currentPart == McClient.bipedBody(model)) {
            bone = this.body;
        } else if (currentPart == McClient.bipedLeftArm(model)) {
            bone = this.leftArm;
        } else if (currentPart == McClient.bipedRightArm(model)) {
            bone = this.rightArm;
        } else if (currentPart == McClient.bipedLeftLeg(model)) {
            bone = currentSlot == AzEquipmentSlot.FEET ? this.leftBoot : this.leftLeg;
        } else if (currentPart == McClient.bipedRightLeg(model)) {
            bone = currentSlot == AzEquipmentSlot.FEET ? this.rightBoot : this.rightLeg;
        }

        if (bone != null) {
            bone.setHidden(false);
        }

        if (
            currentSlot == AzEquipmentSlot.LEGS &&
                (currentPart == McClient.bipedLeftLeg(model) || currentPart == McClient.bipedRightLeg(model)) &&
                this.waist != null
        ) {
            this.waist.setHidden(false);
        }
    }

    /**
     * Resets the bone visibility for the model based on the currently rendering slot, and then sets bones relevant to
     * the current slot as visible for rendering.<br>
     * <br>
     * This is only called by default for non-geo entities (I.E. players or vanilla mobs)
     */
    public void applyBoneVisibilityBySlot(AzEquipmentSlot currentSlot) {
        setAllVisible(false);

        switch (currentSlot) {
            case HEAD:
                setBoneVisible(this.head, true);
                break;
            case CHEST:
                setBoneVisible(this.body, true);
                setBoneVisible(this.rightArm, true);
                setBoneVisible(this.leftArm, true);
                setBoneVisible(this.waist, false);
                break;
            case LEGS:
                setBoneVisible(this.rightLeg, true);
                setBoneVisible(this.leftLeg, true);
                setBoneVisible(this.waist, true);
                break;
            case FEET:
                setBoneVisible(this.rightBoot, true);
                setBoneVisible(this.leftBoot, true);
                break;
            default:
                break;
        }
    }

    /**
     * Sets a bone as visible or hidden, with nullability
     */
    protected void setBoneVisible(@Nullable AzBone bone, boolean visible) {
        if (bone == null)
            return;

        bone.setHidden(!visible);
    }
}
