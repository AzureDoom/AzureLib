package mod.azure.azurelib.rewrite.animation.dispatch.command.stage;

import mod.azure.azurelib.rewrite.animation.property.AzAnimationStageProperties;
import net.minecraft.network.FriendlyByteBuf;

public record AzAnimationStage(
    String name,
    AzAnimationStageProperties properties
) {

    public static final StreamCodec<FriendlyByteBuf, AzAnimationStage> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        AzAnimationStage::name,
        AzAnimationStageProperties.CODEC,
        AzAnimationStage::properties,
        AzAnimationStage::new
    );

}
