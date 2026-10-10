package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.easing.AzEasingType;

public final class AzControllerSetEasingTypeAction implements AzAction {

    private final String controllerName;

    private final AzEasingType easingType;

    public AzControllerSetEasingTypeAction(String controllerName, AzEasingType easingType) {
        this.controllerName = controllerName;
        this.easingType = easingType;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public AzEasingType easingType() {
        return this.easingType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerSetEasingTypeAction))
            return false;
        AzControllerSetEasingTypeAction other = (AzControllerSetEasingTypeAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && java.util.Objects.equals(this.easingType, other.easingType);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + java.util.Objects.hashCode(this.easingType);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerSetEasingTypeAction[controllerName=" + this.controllerName + ", easingType="
            + this.easingType + "]";
    }

    public static final Function<PacketBuffer, AzControllerSetEasingTypeAction> DECODER = buf -> {
        String controllerName = buf.readString(32767);
        AzEasingType easingType = AzEasingType.DECODER.apply(buf);
        return new AzControllerSetEasingTypeAction(controllerName, easingType);
    };

    public static final BiConsumer<PacketBuffer, AzControllerSetEasingTypeAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName());
        AzEasingType.ENCODER.accept(buf, action.easingType());
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_easing_type");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withEasingType(easingType));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetEasingTypeAction decode(PacketBuffer buf) {
        return DECODER.apply(buf);
    }

    public static void encode(PacketBuffer buf, AzControllerSetEasingTypeAction action) {
        ENCODER.accept(buf, action);
    }
}
