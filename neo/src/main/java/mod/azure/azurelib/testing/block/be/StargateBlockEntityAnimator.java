package mod.azure.azurelib.testing.block.be;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimatorConfig;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationController;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.rewrite.animation.impl.AzBlockAnimator;

/**
 * StargateBlockEntityAnimator is responsible for managing and configuring animations for the StargateBlockEntity. It
 * defines specific animations and registers them with the animation controller system, enabling dynamic and interactive
 * visual effects based on the block entity's state.
 */
public class StargateBlockEntityAnimator extends AzBlockAnimator<StargateBlockEntity> {

    private static final ResourceLocation ANIMATIONS = AzureLib.modResource(
        "animations/block/stargate.animation.json"
    );

    protected StargateBlockEntityAnimator() {
        super(AzAnimatorConfig.defaultConfig());
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<StargateBlockEntity> animationControllerContainer) {
        animationControllerContainer.add(
            AzAnimationController.builder(this, "base_controller")
                .build()
        );
    }

    @Override
    public @NotNull ResourceLocation getAnimationLocation(StargateBlockEntity animatable) {
        return ANIMATIONS;
    }
}
