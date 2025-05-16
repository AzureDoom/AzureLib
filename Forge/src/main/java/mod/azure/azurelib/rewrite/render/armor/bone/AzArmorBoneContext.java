package mod.azure.azurelib.rewrite.render.armor.bone;

import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.inventory.EquipmentSlotType;

public class AzArmorBoneContext {

    private AzBakedModel lastModel;

    protected AzBone head;

    protected AzBone body;

    protected AzBone rightArm;

    protected AzBone leftArm;

    protected AzBone rightLeg;

    protected AzBone leftLeg;

    protected AzBone rightBoot;

    protected AzBone leftBoot;

    public AzArmorBoneContext() {
        this.head = null;
        this.body = null;
        this.rightArm = null;
        this.leftArm = null;
        this.rightLeg = null;
        this.leftLeg = null;
        this.rightBoot = null;
        this.leftBoot = null;
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
    }

    /**
     * Transform the currently rendering {@link AzBakedModel} to match the positions and rotations of the base model
     */
    public void applyBaseTransformations(BipedModel<?> baseModel) {
        if (this.head != null) {
            ModelRenderer headPart = baseModel.bipedHead;

            RenderUtils.matchModelPartRot(headPart, this.head);
            this.head.updatePosition(headPart.rotationPointX, -headPart.rotationPointY, headPart.rotationPointZ);
        }

        if (this.body != null) {
            ModelRenderer bodyPart = baseModel.bipedBody;

            RenderUtils.matchModelPartRot(bodyPart, this.body);
            this.body.updatePosition(bodyPart.rotationPointX, -bodyPart.rotationPointY, bodyPart.rotationPointZ);
        }

        if (this.rightArm != null) {
            ModelRenderer rightArmPart = baseModel.bipedRightArm;

            RenderUtils.matchModelPartRot(rightArmPart, this.rightArm);
            this.rightArm.updatePosition(rightArmPart.rotationPointX + 5, 2 - rightArmPart.rotationPointY, rightArmPart.rotationPointZ);
        }

        if (this.leftArm != null) {
            ModelRenderer leftArmPart = baseModel.bipedLeftArm;

            RenderUtils.matchModelPartRot(leftArmPart, this.leftArm);
            this.leftArm.updatePosition(leftArmPart.rotationPointX - 5f, 2f - leftArmPart.rotationPointY, leftArmPart.rotationPointZ);
        }

        if (this.rightLeg != null) {
            ModelRenderer rightLegPart = baseModel.bipedRightLeg;

            RenderUtils.matchModelPartRot(rightLegPart, this.rightLeg);
            this.rightLeg.updatePosition(rightLegPart.rotationPointX + 2, 12 - rightLegPart.rotationPointY, rightLegPart.rotationPointZ);

            if (this.rightBoot != null) {
                RenderUtils.matchModelPartRot(rightLegPart, this.rightBoot);
                this.rightBoot.updatePosition(rightLegPart.rotationPointX + 2, 12 - rightLegPart.rotationPointY, rightLegPart.rotationPointZ);
            }
        }

        if (this.leftLeg != null) {
            ModelRenderer leftLegPart = baseModel.bipedLeftLeg;

            RenderUtils.matchModelPartRot(leftLegPart, this.leftLeg);
            this.leftLeg.updatePosition(leftLegPart.rotationPointX - 2, 12 - leftLegPart.rotationPointY, leftLegPart.rotationPointZ);

            if (this.leftBoot != null) {
                RenderUtils.matchModelPartRot(leftLegPart, this.leftBoot);
                this.leftBoot.updatePosition(leftLegPart.rotationPointX - 2, 12 - leftLegPart.rotationPointY, leftLegPart.rotationPointZ);
            }
        }
    }

    /**
     * Resets the bone visibility for the model based on the current {@link ModelRenderer} and
     * {@link EquipmentSlotType}, and then sets the bones relevant to the current part as visible for rendering.<br>
     * <br>
     * If you are rendering a geo entity with armor, you should probably be calling this prior to rendering
     */
    public void applyBoneVisibilityByPart(
        EquipmentSlotType currentSlot,
        ModelRenderer currentPart,
        BipedModel<?> model
    ) {
        setAllVisible(false);

        currentPart.showModel = true;
        AzBone bone = null;

        if (currentPart == model.bipedHeadwear || currentPart == model.bipedHead) {
            bone = this.head;
        } else if (currentPart == model.bipedBody) {
            bone = this.body;
        } else if (currentPart == model.bipedLeftArm) {
            bone = this.leftArm;
        } else if (currentPart == model.bipedRightArm) {
            bone = this.rightArm;
        } else if (currentPart == model.bipedLeftLeg) {
            bone = currentSlot == EquipmentSlotType.FEET ? this.leftBoot : this.leftLeg;
        } else if (currentPart == model.bipedRightLeg) {
            bone = currentSlot == EquipmentSlotType.FEET ? this.rightBoot : this.rightLeg;
        }

        if (bone != null) {
            bone.setHidden(false);
        }
    }

    /**
     * Resets the bone visibility for the model based on the currently rendering slot, and then sets bones relevant to
     * the current slot as visible for rendering.<br>
     * <br>
     * This is only called by default for non-geo entities (I.E. players or vanilla mobs)
     */
    public void applyBoneVisibilityBySlot(EquipmentSlotType currentSlot) {
        setAllVisible(false);

        switch (currentSlot) {
            case HEAD:
                setBoneVisible(this.head, true);
                break;
            case CHEST:
                setBoneVisible(this.body, true);
                setBoneVisible(this.rightArm, true);
                setBoneVisible(this.leftArm, true);
                break;
            case LEGS:
                setBoneVisible(this.rightLeg, true);
                setBoneVisible(this.leftLeg, true);
                break;
            case FEET:
                setBoneVisible(this.rightBoot, true);
                setBoneVisible(this.leftBoot, true);
                break;
            case MAINHAND:
            case OFFHAND:
                // NO-OP
                break;
        }
    }

    /**
     * Sets a bone as visible or hidden, with nullability
     */
    protected void setBoneVisible(AzBone bone, boolean visible) {
        if (bone == null)
            return;

        bone.setHidden(!visible);
    }
}
