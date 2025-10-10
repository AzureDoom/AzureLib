package mod.azure.azurelib.animation.dispatch.command.stage;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

public record AzAnimationStage(
    String name,
    AzAnimationStageProperties properties
) {

    public static final Function<FriendlyByteBuf, AzAnimationStage> DECODER = buf -> {
        String name = buf.readUtf();
        AzAnimationStageProperties properties = AzAnimationStageProperties.DECODER.apply(buf);
        return new AzAnimationStage(name, properties);
    };

    public static final BiConsumer<FriendlyByteBuf, AzAnimationStage> ENCODER = (buf, stage) -> {
        buf.writeUtf(stage.name());
        AzAnimationStageProperties.ENCODER.accept(buf, stage.properties());
    };

}
