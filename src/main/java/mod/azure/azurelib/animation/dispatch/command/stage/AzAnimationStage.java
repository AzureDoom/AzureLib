package mod.azure.azurelib.animation.dispatch.command.stage;

import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

public final class AzAnimationStage {

    private final String name;

    private final AzAnimationStageProperties properties;

    public AzAnimationStage(String name, AzAnimationStageProperties properties) {
        this.name = name;
        this.properties = properties;
    }

    public String name() {
        return this.name;
    }

    public AzAnimationStageProperties properties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzAnimationStage))
            return false;
        AzAnimationStage other = (AzAnimationStage) o;
        return java.util.Objects.equals(this.name, other.name)
            && java.util.Objects.equals(this.properties, other.properties);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.name);
        result = 31 * result + java.util.Objects.hashCode(this.properties);
        return result;
    }

    @Override
    public String toString() {
        return "AzAnimationStage[name=" + this.name + ", properties=" + this.properties + "]";
    }

    public static final Function<PacketBuffer, AzAnimationStage> DECODER = buf -> {
        String name = buf.readString(32767);
        AzAnimationStageProperties properties = AzAnimationStageProperties.DECODER.apply(buf);
        return new AzAnimationStage(name, properties);
    };

    public static final BiConsumer<PacketBuffer, AzAnimationStage> ENCODER = (buf, stage) -> {
        buf.writeString(stage.name());
        AzAnimationStageProperties.ENCODER.accept(buf, stage.properties());
    };

}
