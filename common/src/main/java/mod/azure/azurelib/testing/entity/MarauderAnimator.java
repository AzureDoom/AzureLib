package mod.azure.azurelib.testing.entity;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimatorConfig;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationController;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.rewrite.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.testing.CommonStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code MarauderAnimator} class is responsible for controlling the animations of a {@link MarauderEntity}. It
 * defines the animation workflows for various states such as idle, walking, running, spawning, attacking, and dying,
 * and binds these animations to the corresponding keyframe events. This class extends the {@code AzEntityAnimator}
 * framework, providing an implementation specific to the {@code MarauderEntity}.
 */
public class MarauderAnimator extends AzEntityAnimator<MarauderEntity> {

    private static final ResourceLocation ANIMATIONS = AzureLib.modResource(
        "animations/entity/marauder.animation.json"
    );

    public MarauderAnimator() {
        super(AzAnimatorConfig.defaultConfig());
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<MarauderEntity> animationControllerContainer) {
        animationControllerContainer.add(
            AzAnimationController.builder(this, CommonStrings.BASE_CONTROLLER)
                .setTransitionLength(0)
                .setKeyframeCallbacks(
                    AzKeyframeCallbacks.<MarauderEntity>builder()
                        .setSoundKeyframeHandler(
                            event -> {
                                if (event.getKeyframeData().getSound().equals("walk")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.METAL_STEP,
                                            SoundSource.HOSTILE,
                                            1.00F,
                                            1.0F,
                                            true
                                        );
                                }
                                if (event.getKeyframeData().getSound().equals("run")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.SKELETON_STEP,
                                            SoundSource.HOSTILE,
                                            1.00F,
                                            1.0F,
                                            true
                                        );
                                }
                                if (event.getKeyframeData().getSound().equals("portal")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.PORTAL_AMBIENT,
                                            SoundSource.HOSTILE,
                                            0.20F,
                                            1.0F,
                                            true
                                        );
                                }
                                if (event.getKeyframeData().getSound().equals("axe")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.ENDER_EYE_LAUNCH,
                                            SoundSource.HOSTILE,
                                            1.00F,
                                            1.0F,
                                            true
                                        );
                                }
                            }
                        )
                        .build()
                )
                .build()
        );
    }

    @Override
    public @NotNull ResourceLocation getAnimationLocation(MarauderEntity drone) {
        return ANIMATIONS;
    }
}
