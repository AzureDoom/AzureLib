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

public final class AzControllerSetFreezeTickAction implements AzAction {

    private final String controllerName;

    private final double freezeTickOffset;

    public AzControllerSetFreezeTickAction(String controllerName, double freezeTickOffset) {
        this.controllerName = controllerName;
        this.freezeTickOffset = freezeTickOffset;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public double freezeTickOffset() {
        return this.freezeTickOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerSetFreezeTickAction))
            return false;
        AzControllerSetFreezeTickAction other = (AzControllerSetFreezeTickAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && Double.compare(this.freezeTickOffset, other.freezeTickOffset) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + Double.hashCode(this.freezeTickOffset);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerSetFreezeTickAction[controllerName=" + this.controllerName + ", freezeTickOffset="
            + this.freezeTickOffset + "]";
    }

    public static final Function<PacketBuffer, AzControllerSetFreezeTickAction> DECODER = buf -> {
        String controllerName = buf.readString(32767);
        double freezeTickOffset = buf.readDouble(); // Read double from the buffer
        return new AzControllerSetFreezeTickAction(controllerName, freezeTickOffset); // Create new instance
    };

    public static final BiConsumer<PacketBuffer, AzControllerSetFreezeTickAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName());
        buf.writeDouble(action.freezeTickOffset()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_freeze_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(controller.animationProperties().withFreezeTickOffset(freezeTickOffset));
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetFreezeTickAction decode(PacketBuffer buf) {
        return DECODER.apply(buf);
    }

    public static void encode(PacketBuffer buf, AzControllerSetFreezeTickAction action) {
        ENCODER.accept(buf, action);
    }
}
