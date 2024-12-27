package mod.azure.azurelib.fabric.core2.example.entities.ovamorph;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.core2.animation.AzAnimatorConfig;
import mod.azure.azurelib.core2.animation.controller.AzAnimationController;
import mod.azure.azurelib.core2.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.core2.animation.impl.AzEntityAnimator;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class OvamorphAnimator extends AzEntityAnimator<Ovamorph> {

    private static final ResourceLocation ANIMATION = AzureLib.modResource(
        "animations/entity/ovamorph.animation.json"
    );

    public OvamorphAnimator() {
        super(AzAnimatorConfig.defaultConfig());
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<Ovamorph> animationControllerContainer) {
        animationControllerContainer.add(
            AzAnimationController.builder(this, OvamorphAnimationRefs.BASE_CONTROLLER_NAME)
                .setTransitionLength(5)
                .build()
        );
    }

    @Override
    public @NotNull ResourceLocation getAnimationLocation(Ovamorph animatable) {
        return ANIMATION;
    }

}
