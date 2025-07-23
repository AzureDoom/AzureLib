package mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationController;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.sequence.AzAnimationSequence;

public class AzRootPlayAnimationSequenceAction implements AzAction {

    private final String controllerName;

    private final AzAnimationSequence sequence;

    public static final Function<PacketBuffer, AzRootPlayAnimationSequenceAction> DECODER = buf -> {
        String controllerName = buf.readUtf(); // Read controller name (UTF string)
        AzAnimationSequence sequence = AzAnimationSequence.DECODER.apply(buf); // Decode AzAnimationSequence
        return new AzRootPlayAnimationSequenceAction(controllerName, sequence); // Create new instance
    };

    public static final BiConsumer<PacketBuffer, AzRootPlayAnimationSequenceAction> ENCODER = (buf, action) -> {
        buf.writeUtf(action.controllerName()); // Write controller name (UTF string)
        AzAnimationSequence.ENCODER.accept(buf, action.sequence()); // Encode AzAnimationSequence
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/play_animation_sequence");

    public AzRootPlayAnimationSequenceAction(String controllerName, AzAnimationSequence sequence) {
        this.controllerName = controllerName;
        this.sequence = sequence;
    }

    public String controllerName() {
        return controllerName;
    }

    public AzAnimationSequence sequence() {
        return sequence;
    }

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.run(originSide, sequence);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootPlayAnimationSequenceAction decode(PacketBuffer buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(PacketBuffer buf, AzRootPlayAnimationSequenceAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }

    @Override
    public String toString() {
        return "AzRootPlayAnimationSequenceAction{" +
            "controllerName='" + controllerName + '\'' +
            ", sequence=" + sequence +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AzRootPlayAnimationSequenceAction that = (AzRootPlayAnimationSequenceAction) o;

        if (!controllerName.equals(that.controllerName))
            return false;
        return sequence.equals(that.sequence);
    }

    @Override
    public int hashCode() {
        int result = controllerName.hashCode();
        result = 31 * result + sequence.hashCode();
        return result;
    }
}
