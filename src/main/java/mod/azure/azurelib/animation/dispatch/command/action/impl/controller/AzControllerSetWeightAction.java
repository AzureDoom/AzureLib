package mod.azure.azurelib.animation.dispatch.command.action.impl.controller;

import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.network.AzByteBuf;

/**
 * Sets a controller's blend weight, either immediately ({@code fadeTicks <= 0}) or as a fade over {@code fadeTicks}.
 */
public final class AzControllerSetWeightAction implements AzAction {

    private final String controllerName;

    private final double weight;

    private final double fadeTicks;

    public AzControllerSetWeightAction(String controllerName, double weight, double fadeTicks) {
        this.controllerName = controllerName;
        this.weight = weight;
        this.fadeTicks = fadeTicks;
    }

    public String controllerName() {
        return this.controllerName;
    }

    public double weight() {
        return this.weight;
    }

    public double fadeTicks() {
        return this.fadeTicks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzControllerSetWeightAction))
            return false;
        AzControllerSetWeightAction other = (AzControllerSetWeightAction) o;
        return java.util.Objects.equals(this.controllerName, other.controllerName)
            && Double.compare(this.weight, other.weight) == 0
            && Double.compare(this.fadeTicks, other.fadeTicks) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.controllerName);
        result = 31 * result + Double.hashCode(this.weight);
        result = 31 * result + Double.hashCode(this.fadeTicks);
        return result;
    }

    @Override
    public String toString() {
        return "AzControllerSetWeightAction[controllerName=" + this.controllerName + ", weight=" + this.weight
            + ", fadeTicks=" + this.fadeTicks + "]";
    }

    public static final Function<AzByteBuf, AzControllerSetWeightAction> DECODER = buf -> {
        String controllerName = buf.readString(32767);
        double weight = buf.readDouble();
        double fadeTicks = buf.readDouble();

        return new AzControllerSetWeightAction(
            controllerName,
            weight,
            fadeTicks
        );
    };

    public static final BiConsumer<AzByteBuf, AzControllerSetWeightAction> ENCODER = (buf, action) -> {
        buf.writeString(action.controllerName());
        buf.writeDouble(action.weight());
        buf.writeDouble(action.fadeTicks());
    };

    public static final ResourceLocation RESOURCE_LOCATION = AzureLib.modResource("controller/set_weight");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        AzAnimationController<?> controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.fadeWeight(weight, fadeTicks);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static AzControllerSetWeightAction decode(AzByteBuf buf) {
        return DECODER.apply(buf);
    }

    public static void encode(AzByteBuf buf, AzControllerSetWeightAction action) {
        ENCODER.accept(buf, action);
    }
}
