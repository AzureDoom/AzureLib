package mod.azure.azurelib.simple;

import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.impl.AzBlockAnimator;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public class AzSimpleBlockAnimator<T extends TileEntity> extends AzBlockAnimator<T> implements AzSimpleAnimatorHandle {

    private final AzSimpleConfig<T> simpleConfig;

    private final AzSimpleAnimationResolver<T> resolver;

    public AzSimpleBlockAnimator(AzSimpleConfig<T> simpleConfig) {
        super(simpleConfig.animatorConfig());
        this.simpleConfig = simpleConfig;
        this.resolver = new AzSimpleAnimationResolver<>(this, simpleConfig);
    }

    @Override
    public final void registerControllers(AzAnimationControllerContainer<T> animationControllerContainer) {
        resolver.registerControllers(animationControllerContainer);
        registerAdditionalControllers(animationControllerContainer);
    }

    protected void registerAdditionalControllers(AzAnimationControllerContainer<T> animationControllerContainer) {}

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<T> modifiers) {
        resolver.registerPoseModifiers(modifiers);
    }

    @Override
    public ResourceLocation getAnimationLocation(T animatable) {
        return simpleConfig.animationLocation(animatable);
    }

    @Override
    public void handleSimpleAction(AzDispatchSide originSide, AzSimplePlayAction action) {
        resolver.handle(originSide, action);
    }

    public AzSimpleAnimationResolver<T> simple() {
        return resolver;
    }
}
