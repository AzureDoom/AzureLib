package mod.azure.azurelib.rewrite.animation.dispatch.command.stage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import mod.azure.azurelib.rewrite.animation.property.AzAnimationStageProperties;

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
