package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.network.AzByteBuf;

public final class AzRootSetReverseAction implements AzAction {

    private final boolean hasReverse;

    public AzRootSetReverseAction(boolean hasReverse) {
        this.hasReverse = hasReverse;
    }

    public boolean hasReverse() {
        return this.hasReverse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzRootSetReverseAction))
            return false;
        AzRootSetReverseAction other = (AzRootSetReverseAction) o;
        return this.hasReverse == other.hasReverse;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Boolean.hashCode(this.hasReverse);
        return result;
    }

    @Override
    public String toString() {
        return "AzRootSetReverseAction[hasReverse=" + this.hasReverse + "]";
    }

    public static final Function<AzByteBuf, AzRootSetReverseAction> DECODER = buf -> {
        boolean hasReverse = buf.readBoolean(); // Read boolean from the buffer
        return new AzRootSetReverseAction(hasReverse); // Create a new instance
    };

    public static final BiConsumer<AzByteBuf, AzRootSetReverseAction> ENCODER = (buf, action) -> {
        buf.writeBoolean(action.hasReverse()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource(
        "root/set_reverse_tick_offset"
    );

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setReversing(hasReverse)
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetReverseAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(AzByteBuf buf, AzRootSetReverseAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
