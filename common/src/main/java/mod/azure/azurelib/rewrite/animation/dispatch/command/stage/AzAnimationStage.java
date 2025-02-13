package mod.azure.azurelib.rewrite.animation.dispatch.command.stage;

import mod.azure.azurelib.rewrite.animation.property.AzAnimationStageProperties;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record AzAnimationStage(
        String name,
        AzAnimationStageProperties properties
) {

    public void encode(@NotNull FriendlyByteBuf buf, @NotNull AzAnimationStage stage) {
        buf.writeUtf(stage.name());
        AzAnimationStageProperties.CODEC.encode(buf, stage.properties());
    }

    public AzAnimationStage decode(@NotNull FriendlyByteBuf buf) {
        var name = buf.readUtf();
        var properties = AzAnimationStageProperties.CODEC.decode(buf);

        return new AzAnimationStage(name, properties);
    }

}
