package mod.azure.azurelib.render.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class AzArmorModel<S extends HumanoidRenderState> extends HumanoidModel<S> {

    private final AzArmorRendererPipeline rendererPipeline;

    public AzArmorModel(AzArmorRendererPipeline rendererPipeline) {
        super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
        this.rendererPipeline = rendererPipeline;
    }

    public void setAllVisible(boolean visible) {
        this.head.visible = visible;
        this.hat.visible = visible;
        this.body.visible = visible;
        this.rightArm.visible = visible;
        this.leftArm.visible = visible;
        this.rightLeg.visible = visible;
        this.leftLeg.visible = visible;

        rendererPipeline.context().boneContext().setAllVisible(visible);
    }
}
