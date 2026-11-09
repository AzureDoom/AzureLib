package mod.azure.azurelib.simple;

import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public class AzSimpleItemAnimator extends AzItemAnimator implements AzSimpleAnimatorHandle {

    private final AzSimpleConfig<ItemStack> simpleConfig;

    private final AzSimpleAnimationResolver<ItemStack> resolver;

    public AzSimpleItemAnimator(AzSimpleConfig<ItemStack> simpleConfig) {
        super(simpleConfig.animatorConfig());
        this.simpleConfig = simpleConfig;
        this.resolver = new AzSimpleAnimationResolver<>(this, simpleConfig);
    }

    @Override
    public final void registerControllers(AzAnimationControllerContainer<ItemStack> animationControllerContainer) {
        resolver.registerControllers(animationControllerContainer);
        registerAdditionalControllers(animationControllerContainer);
    }

    protected void registerAdditionalControllers(
        AzAnimationControllerContainer<ItemStack> animationControllerContainer
    ) {}

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<ItemStack> modifiers) {
        resolver.registerPoseModifiers(modifiers);
    }

    @Override
    public ResourceLocation getAnimationLocation(ItemStack animatable) {
        return simpleConfig.animationLocation(animatable);
    }

    @Override
    public void handleSimpleAction(AzDispatchSide originSide, AzSimplePlayAction action) {
        resolver.handle(originSide, action);
    }

    public AzSimpleAnimationResolver<ItemStack> simple() {
        return resolver;
    }
}
