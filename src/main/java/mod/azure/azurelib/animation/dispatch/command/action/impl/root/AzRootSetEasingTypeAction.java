package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.easing.AzEasingType;

public final class AzRootSetEasingTypeAction implements AzAction {

    private final AzEasingType easingType;

    public AzRootSetEasingTypeAction(AzEasingType easingType) {
        this.easingType = easingType;
    }

    public AzEasingType easingType() {
        return this.easingType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzRootSetEasingTypeAction))
            return false;
        AzRootSetEasingTypeAction other = (AzRootSetEasingTypeAction) o;
        return java.util.Objects.equals(this.easingType, other.easingType);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.easingType);
        return result;
    }

    @Override
    public String toString() {
        return "AzRootSetEasingTypeAction[easingType=" + this.easingType + "]";
    }

    public static final Function<PacketBuffer, AzRootSetEasingTypeAction> DECODER = buf -> {
        AzEasingType easingType = AzEasingType.DECODER.apply(buf);
        return new AzRootSetEasingTypeAction(easingType);
    };

    public static final BiConsumer<PacketBuffer, AzRootSetEasingTypeAction> ENCODER = (buf, action) -> {
        AzEasingType.ENCODER.accept(buf, action.easingType());
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_easing_type");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
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

    public static AzRootSetEasingTypeAction decode(PacketBuffer buf) {
        return DECODER.apply(buf);
    }

    public static void encode(PacketBuffer buf, AzRootSetEasingTypeAction action) {
        ENCODER.accept(buf, action);
    }

}
