package mod.azure.azurelib.rewrite.animation.dispatch.command.stage;

import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.rewrite.animation.property.AzAnimationStageProperties;

public class AzAnimationStage {

    private String name;

    private AzAnimationStageProperties properties;

    public AzAnimationStage(String name, AzAnimationStageProperties properties) {
        this.name = name;
        this.properties = properties;
    }

    public String name() {
        return name;
    }

    public AzAnimationStageProperties properties() {
        return properties;
    }

    public static final Function<PacketBuffer, AzAnimationStage> DECODER = buf -> {
        String name = buf.readUtf();
        AzAnimationStageProperties properties = AzAnimationStageProperties.DECODER.apply(buf);
        return new AzAnimationStage(name, properties);
    };

    public static final BiConsumer<PacketBuffer, AzAnimationStage> ENCODER = (buf, stage) -> {
        buf.writeUtf(stage.name());
        AzAnimationStageProperties.ENCODER.accept(buf, stage.properties());
    };

}
