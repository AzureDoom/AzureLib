package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.network.AzByteBuf;

public final class AzRootSetFreezeTickAction implements AzAction {

    private final double freezeTickOffset;

    public AzRootSetFreezeTickAction(double freezeTickOffset) {
        this.freezeTickOffset = freezeTickOffset;
    }

    public double freezeTickOffset() {
        return this.freezeTickOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzRootSetFreezeTickAction))
            return false;
        AzRootSetFreezeTickAction other = (AzRootSetFreezeTickAction) o;
        return Double.compare(this.freezeTickOffset, other.freezeTickOffset) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Double.hashCode(this.freezeTickOffset);
        return result;
    }

    @Override
    public String toString() {
        return "AzRootSetFreezeTickAction[freezeTickOffset=" + this.freezeTickOffset + "]";
    }

    public static final Function<AzByteBuf, AzRootSetFreezeTickAction> DECODER = buf -> {
        double freezeTickOffset = buf.readDouble(); // Read double from the buffer
        return new AzRootSetFreezeTickAction(freezeTickOffset); // Create new instance
    };

    public static final BiConsumer<AzByteBuf, AzRootSetFreezeTickAction> ENCODER = (buf, action) -> {
        buf.writeDouble(action.freezeTickOffset()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_freeze_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withFreezeTickOffset(freezeTickOffset)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetFreezeTickAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(AzByteBuf buf, AzRootSetFreezeTickAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
