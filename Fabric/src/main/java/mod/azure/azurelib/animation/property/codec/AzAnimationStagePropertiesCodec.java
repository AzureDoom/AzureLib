package mod.azure.azurelib.animation.property.codec;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypeRegistry;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviorRegistry;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

public class AzAnimationStagePropertiesCodec {

    public static final Function<FriendlyByteBuf, AzAnimationStageProperties> DECODER = buf -> {
        byte propertyLength = buf.readByte();
        AzAnimationStageProperties properties = AzAnimationStageProperties.DEFAULT;

        for (int i = 0; i < propertyLength; i++) {
            byte code = buf.readByte();

            switch (code) {
                case 0:
                    boolean hasAnimationSpeed = buf.readBoolean();
                    double animationSpeed = hasAnimationSpeed ? buf.readDouble() : 1D;
                    properties = properties.withAnimationSpeed(animationSpeed);
                    break;
                case 1:
                    properties = properties.withTransitionLength(buf.readFloat());
                    break;
                case 2:
                    AzEasingType easingType = AzEasingTypeRegistry.getOrDefault(buf.readUtf(), AzEasingTypes.NONE);
                    properties = properties.withEasingType(easingType);
                    break;
                case 3:
                    AzPlayBehavior playBehavior = AzPlayBehaviorRegistry.getOrDefault(
                        buf.readUtf(),
                        AzPlayBehaviors.PLAY_ONCE
                    );
                    properties = properties.withPlayBehavior(playBehavior);
                    break;
                case 4:
                    boolean hasTickOffset = buf.readBoolean();
                    double startTickOffset = hasTickOffset ? buf.readDouble() : 0D;
                    properties = properties.withStartTickOffset(startTickOffset);
                    break;
            }
        }

        return properties;
    };

    public static final BiConsumer<FriendlyByteBuf, AzAnimationStageProperties> ENCODER = (buf, properties) -> {
        int propertyLength = 0;
        propertyLength += properties.hasAnimationSpeed() ? 1 : 0;
        propertyLength += properties.hasTransitionLength() ? 1 : 0;
        propertyLength += properties.hasEasingType() ? 1 : 0;
        propertyLength += properties.hasPlayBehavior() ? 1 : 0;
        propertyLength += properties.hasStartTickOffset() ? 1 : 0;

        buf.writeByte(propertyLength);

        if (properties.hasAnimationSpeed()) {
            buf.writeByte(0);
            buf.writeDouble(properties.animationSpeed());
        }

        if (properties.hasTransitionLength()) {
            buf.writeByte(1);
            buf.writeFloat(properties.transitionLength());
        }

        if (properties.hasEasingType()) {
            buf.writeByte(2);
            buf.writeUtf(properties.easingType().name());
        }

        if (properties.hasPlayBehavior()) {
            buf.writeByte(3);
            buf.writeUtf(properties.playBehavior().name());
        }

        if (properties.hasStartTickOffset()) {
            buf.writeByte(4);
            buf.writeBoolean(true);
            buf.writeDouble(properties.startTickOffset());
        }
    };
}
