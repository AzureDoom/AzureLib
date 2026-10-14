package mod.azure.azurelib.render.armor.bone;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;

import javax.annotation.Nullable;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
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
    public void applyBaseTransformations(BipedEntityModel baseModel) {
        if (this.head != null) {
            ModelPart headPart = baseModel.head;

            RenderUtils.matchModelPartRot(headPart, this.head);
            this.head.updatePosition(headPart.pivotX, -headPart.pivotY, headPart.pivotZ);
        }

        if (this.body != null) {
            ModelPart bodyPart = baseModel.body;

            RenderUtils.matchModelPartRot(bodyPart, this.body);
            this.body.updatePosition(bodyPart.pivotX, -bodyPart.pivotY, bodyPart.pivotZ);
        }

        if (this.rightArm != null) {
            ModelPart rightArmPart = baseModel.rightArm;

            RenderUtils.matchModelPartRot(rightArmPart, this.rightArm);
            this.rightArm.updatePosition(
                rightArmPart.pivotX + 5,
                2 - rightArmPart.pivotY,
                rightArmPart.pivotZ
            );
        }

        if (this.leftArm != null) {
            ModelPart leftArmPart = baseModel.leftArm;

            RenderUtils.matchModelPartRot(leftArmPart, this.leftArm);
            this.leftArm.updatePosition(
                leftArmPart.pivotX - 5f,
                2f - leftArmPart.pivotY,
                leftArmPart.pivotZ
            );
        }

        if (this.rightLeg != null) {
            ModelPart rightLegPart = baseModel.rightLeg;

            RenderUtils.matchModelPartRot(rightLegPart, this.rightLeg);
            this.rightLeg.updatePosition(
                rightLegPart.pivotX + 2,
                12 - rightLegPart.pivotY,
                rightLegPart.pivotZ
            );

            if (this.rightBoot != null) {
                RenderUtils.matchModelPartRot(rightLegPart, this.rightBoot);
                this.rightBoot.updatePosition(
                    rightLegPart.pivotX + 2,
                    12 - rightLegPart.pivotY,
                    rightLegPart.pivotZ
                );
            }
            if (this.waist != null) {
                RenderUtils.matchModelPartRot(baseModel.body, this.waist);
                this.waist.updatePosition(
                    baseModel.body.pivotX,
                    -(baseModel.body.pivotY),
                    baseModel.body.pivotZ
                );
            }
        }

        if (this.leftLeg != null) {
            ModelPart leftLegPart = baseModel.leftLeg;

            RenderUtils.matchModelPartRot(leftLegPart, this.leftLeg);
            this.leftLeg.updatePosition(
                leftLegPart.pivotX - 2,
                12 - leftLegPart.pivotY,
                leftLegPart.pivotZ
            );

            if (this.leftBoot != null) {
                RenderUtils.matchModelPartRot(leftLegPart, this.leftBoot);
                this.leftBoot.updatePosition(
                    leftLegPart.pivotX - 2,
                    12 - leftLegPart.pivotY,
                    leftLegPart.pivotZ
                );
            }
            if (this.waist != null) {
                RenderUtils.matchModelPartRot(baseModel.body, this.waist);
                this.waist.updatePosition(
                    baseModel.body.pivotX,
                    -(baseModel.body.pivotY),
                    baseModel.body.pivotZ
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
        BipedEntityModel model
    ) {
        setAllVisible(false);

        currentPart.visible = true;
        AzBone bone = null;

        if (currentPart == model.hat || currentPart == model.head) {
            bone = this.head;
        } else if (currentPart == model.body) {
            bone = this.body;
        } else if (currentPart == model.leftArm) {
            bone = this.leftArm;
        } else if (currentPart == model.rightArm) {
            bone = this.rightArm;
        } else if (currentPart == model.leftLeg) {
            bone = currentSlot == AzEquipmentSlot.FEET ? this.leftBoot : this.leftLeg;
        } else if (currentPart == model.rightLeg) {
            bone = currentSlot == AzEquipmentSlot.FEET ? this.rightBoot : this.rightLeg;
        }

        if (bone != null) {
            bone.setHidden(false);
        }

        if (
            currentSlot == AzEquipmentSlot.LEGS &&
                (currentPart == model.leftLeg || currentPart == model.rightLeg) &&
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
