package mod.azure.azurelib.rewrite.animation.dispatch.command.action.registry;

import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The AzActionRegistry class serves as a centralized registry for mapping {@link AzAction} implementations to their
 * associated {@link ResourceLocation} identifiers and codecs. This registry enables efficient encoding, decoding, and
 * dispatching of animation-related actions within the animation system. <br>
 * Key Responsibilities:
 * <ul>
 * <li>Maintain a bidirectional mapping between {@link ResourceLocation} identifiers and short integer IDs for efficient
 * serialization/deserialization.</li>
 * <li>Register {@link AzAction} implementations and their corresponding {@link StreamCodec} instances.</li>
 * <li>Provide methods for retrieving codecs and IDs based on resource locations or integer IDs.
 * </ul>
 */
public class AzActionRegistry {

    private static final Map<ResourceLocation, Short> RESOURCE_LOCATION_TO_ID = new Object2ShortArrayMap<>();

    private static final Map<Short, StreamCodec<FriendlyByteBuf, ? extends AzAction>> CODEC_BY_ID =
        new HashMap<>();

    private static short NEXT_FREE_ID = 0;

    static {
        // Root actions
        register(AzRootCancelAction.RESOURCE_LOCATION, AzRootCancelAction.CODEC);
        register(AzRootCancelAllAction.RESOURCE_LOCATION, AzRootCancelAllAction.CODEC);
        register(AzRootPlayAnimationSequenceAction.RESOURCE_LOCATION, AzRootPlayAnimationSequenceAction.CODEC);
        register(AzRootSetAnimationSpeedAction.RESOURCE_LOCATION, AzRootSetAnimationSpeedAction.CODEC);
        register(AzRootSetEasingTypeAction.RESOURCE_LOCATION, AzRootSetEasingTypeAction.CODEC);
        register(AzRootSetTransitionSpeedAction.RESOURCE_LOCATION, AzRootSetTransitionSpeedAction.CODEC);

        // Controller actions
        // TODO:

        // Animation actions
        // TODO:
    }

    public static @Nullable <A, T extends StreamCodec<FriendlyByteBuf, A>> T getCodecOrNull(
        ResourceLocation resourceLocation
    ) {
        var id = RESOURCE_LOCATION_TO_ID.get(resourceLocation);
        @SuppressWarnings("unchecked")
        var codec = (T) CODEC_BY_ID.get(id);
        return codec;
    }

    public static @Nullable <A, T extends StreamCodec<FriendlyByteBuf, A>> T getCodecOrNull(short id) {
        @SuppressWarnings("unchecked")
        var codec = (T) CODEC_BY_ID.get(id);
        return codec;
    }

    public static @Nullable Short getIdOrNull(ResourceLocation resourceLocation) {
        return RESOURCE_LOCATION_TO_ID.get(resourceLocation);
    }

    private static <A extends AzAction> void register(
        ResourceLocation resourceLocation,
        StreamCodec<FriendlyByteBuf, A> codec
    ) {
        var id = RESOURCE_LOCATION_TO_ID.computeIfAbsent(resourceLocation, ($) -> NEXT_FREE_ID++);
        CODEC_BY_ID.put(id, codec);
    }
}
