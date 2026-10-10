package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;

public final class AzRootSetRepeatTimesAction implements AzAction {

    private final double repeatXTimes;

    public AzRootSetRepeatTimesAction(double repeatXTimes) {
        this.repeatXTimes = repeatXTimes;
    }

    public double repeatXTimes() {
        return this.repeatXTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzRootSetRepeatTimesAction))
            return false;
        AzRootSetRepeatTimesAction other = (AzRootSetRepeatTimesAction) o;
        return Double.compare(this.repeatXTimes, other.repeatXTimes) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Double.hashCode(this.repeatXTimes);
        return result;
    }

    @Override
    public String toString() {
        return "AzRootSetRepeatTimesAction[repeatXTimes=" + this.repeatXTimes + "]";
    }

    public static final Function<PacketBuffer, AzRootSetRepeatTimesAction> DECODER = buf -> {
        double repeatXTimes = buf.readDouble(); // Read double from the buffer
        return new AzRootSetRepeatTimesAction(repeatXTimes); // Create a new instance
    };

    public static final BiConsumer<PacketBuffer, AzRootSetRepeatTimesAction> ENCODER = (buf, action) -> {
        buf.writeDouble(action.repeatXTimes()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_repeat_times_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withRepeatXTimes(repeatXTimes)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetRepeatTimesAction decode(PacketBuffer buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(PacketBuffer buf, AzRootSetRepeatTimesAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
