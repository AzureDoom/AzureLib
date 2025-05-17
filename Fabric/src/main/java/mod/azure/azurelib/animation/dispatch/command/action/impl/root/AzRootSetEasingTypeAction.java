package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.easing.AzEasingType;

/**
 * Represents an action that sets the easing type of animation controllers within an {@link AzAnimator}.
 */
public class AzRootSetEasingTypeAction implements AzAction {

    private final AzEasingType easingType;

    public static final Function<FriendlyByteBuf, AzRootSetEasingTypeAction> DECODER = buf -> {
        AzEasingType easingType = AzEasingType.DECODER.apply(buf);
        return new AzRootSetEasingTypeAction(easingType);
    };

    public static final BiConsumer<FriendlyByteBuf, AzRootSetEasingTypeAction> ENCODER = (buf, action) -> {
        AzEasingType.ENCODER.accept(buf, action.easingType());
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_easing_type");

    public AzRootSetEasingTypeAction(AzEasingType easingType) {
        this.easingType = easingType;
    }

    public AzEasingType easingType() {
        return easingType;
    }

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withEasingType(easingType)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetEasingTypeAction decode(FriendlyByteBuf buf) {
        return DECODER.apply(buf);
    }

    public static void encode(FriendlyByteBuf buf, AzRootSetEasingTypeAction action) {
        ENCODER.accept(buf, action);
    }

    @Override
    public String toString() {
        return "AzRootSetEasingTypeAction{" +
            "easingType=" + easingType +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AzRootSetEasingTypeAction that = (AzRootSetEasingTypeAction) o;

        return easingType == that.easingType;
    }

    @Override
    public int hashCode() {
        return easingType.hashCode();
    }
}
