package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.network.AzByteBuf;

public final class AzRootPlayAnimationSequenceAction implements AzAction {

    private final AzAnimationSequence sequence;

    public AzRootPlayAnimationSequenceAction(AzAnimationSequence sequence) {
        this.sequence = sequence;
    }

    public AzAnimationSequence sequence() {
        return this.sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzRootPlayAnimationSequenceAction))
            return false;
        AzRootPlayAnimationSequenceAction other = (AzRootPlayAnimationSequenceAction) o;
        return java.util.Objects.equals(this.sequence, other.sequence);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.sequence);
        return result;
    }

    @Override
    public String toString() {
        return "AzRootPlayAnimationSequenceAction[sequence=" + this.sequence + "]";
    }

    public static final Function<AzByteBuf, AzRootPlayAnimationSequenceAction> DECODER = buf -> {
        AzAnimationSequence sequence = AzAnimationSequence.DECODER.apply(buf); // Decode AzAnimationSequence
        return new AzRootPlayAnimationSequenceAction(sequence); // Create new instance
    };

    public static final BiConsumer<AzByteBuf, AzRootPlayAnimationSequenceAction> ENCODER = (buf, action) -> {
        AzAnimationSequence.ENCODER.accept(buf, action.sequence()); // Encode AzAnimationSequence
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/play_animation_sequence");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationControllerContainer<?> controllerContainer = animator.getAnimationControllerContainer();
        Collection<? extends AzAnimationController<?>> controllers = controllerContainer.getAll();

        controllers.forEach(controller -> controller.run(originSide, sequence));
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootPlayAnimationSequenceAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(AzByteBuf buf, AzRootPlayAnimationSequenceAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
