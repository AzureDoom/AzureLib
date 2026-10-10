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
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;

public final class AzControllerPlayAnimationSequenceAction implements AzAction {

    private final String controllerName;

    private final AzAnimationSequence sequence;

    public AzControllerPlayAnimationSequenceAction(String controllerName, AzAnimationSequence sequence) {
        this.controllerName = controllerName;
        this.sequence = sequence;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public AzAnimationSequence sequence() {
        return this.sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerPlayAnimationSequenceAction))
            return false;
        AzControllerPlayAnimationSequenceAction other = (AzControllerPlayAnimationSequenceAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && java.util.Objects.equals(this.sequence, other.sequence);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + java.util.Objects.hashCode(this.sequence);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerPlayAnimationSequenceAction[controllerName=" + this.controllerName + ", sequence="
            + this.sequence + "]";
    }

    public static final Function<PacketBuffer, AzControllerPlayAnimationSequenceAction> DECODER = buf -> {
        String controllerName = buf.readString(32767); // Read controller name (UTF string)
        AzAnimationSequence sequence = AzAnimationSequence.DECODER.apply(buf); // Decode AzAnimationSequence
        return new AzControllerPlayAnimationSequenceAction(controllerName, sequence); // Create a new instance
    };

    public static final BiConsumer<PacketBuffer, AzControllerPlayAnimationSequenceAction> ENCODER = (
        buf,
        action
    ) -> {
        buf.writeString(action.controllerName()); // Write controller name (UTF string)
        AzAnimationSequence.ENCODER.accept(buf, action.sequence()); // Encode AzAnimationSequence
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/play_animation_sequence");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.run(originSide, sequence);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerPlayAnimationSequenceAction decode(PacketBuffer buf) {
        return DECODER.apply(buf); // Delegate to the DECODER functional interface
    }

    public static void encode(PacketBuffer buf, AzControllerPlayAnimationSequenceAction action) {
        ENCODER.accept(buf, action); // Delegate to the ENCODER functional interface
    }
}
