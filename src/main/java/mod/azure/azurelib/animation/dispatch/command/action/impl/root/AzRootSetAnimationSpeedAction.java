package mod.azure.azurelib.animation.dispatch.command.action.impl.root;

import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.network.AzByteBuf;

public final class AzRootSetAnimationSpeedAction implements AzAction {

    private final double animationSpeed;

    public AzRootSetAnimationSpeedAction(double animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public double animationSpeed() {
        return this.animationSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzRootSetAnimationSpeedAction))
            return false;
        AzRootSetAnimationSpeedAction other = (AzRootSetAnimationSpeedAction) o;
        return Double.compare(this.animationSpeed, other.animationSpeed) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Double.hashCode(this.animationSpeed);
        return result;
    }

    @Override
    public String toString() {
        return "AzRootSetAnimationSpeedAction[animationSpeed=" + this.animationSpeed + "]";
    }

    public static final Function<AzByteBuf, AzRootSetAnimationSpeedAction> DECODER = buf -> {
        double animationSpeed = buf.readDouble(); // Read double from the buffer
        return new AzRootSetAnimationSpeedAction(animationSpeed); // Create a new instance
    };

    public static final BiConsumer<AzByteBuf, AzRootSetAnimationSpeedAction> ENCODER = (buf, action) -> {
        buf.writeDouble(action.animationSpeed()); // Write the animation speed to the buffer
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("root/set_animation_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withAnimationSpeed(animationSpeed)
                )
            );
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzRootSetAnimationSpeedAction decode(AzByteBuf buf) {
        return DECODER.apply(buf); // Delegate decoding to DECODER
    }

    public static void encode(AzByteBuf buf, AzRootSetAnimationSpeedAction action) {
        ENCODER.accept(buf, action); // Delegate encoding to ENCODER
    }
}
