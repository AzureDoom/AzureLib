package mod.azure.azurelib.animation.dispatch.command.action.registry;

import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.*;
import mod.azure.azurelib.animation.dispatch.command.action.impl.root.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.*;
import mod.azure.azurelib.animation.dispatch.command.action.impl.root.*;

/**
 * The AzActionRegistry class serves as a centralized registry for mapping {@link AzAction} implementations to their
 * associated {@link Identifier} identifiers and codecs. This registry enables efficient encoding, decoding, and
 * dispatching of animation-related actions within the animation system. <br>
 * Key Responsibilities:
 * <ul>
 * <li>Maintain a bidirectional mapping between {@link Identifier} identifiers and short integer IDs for efficient
 * serialization/deserialization.</li>
 * <li>Register {@link AzAction} implementations and their corresponding {@link StreamCodec} instances.</li>
 * <li>Provide methods for retrieving codecs and IDs based on resource locations or integer IDs.
 * </ul>
 */
public class AzActionRegistry {

    private static final Map<Identifier, Short> RESOURCE_LOCATION_TO_ID = new Object2ShortArrayMap<>();

    private static final Map<Short, StreamCodec<FriendlyByteBuf, ? extends AzAction>> CODEC_BY_ID =
        new HashMap<>();

    private static short NEXT_FREE_ID = 0;

    static {
        // Root actions
        register(AzRootCancelAllAction.RESOURCE_LOCATION, AzRootCancelAllAction.CODEC);
        register(AzRootPlayAnimationSequenceAction.RESOURCE_LOCATION, AzRootPlayAnimationSequenceAction.CODEC);
        register(AzRootSetAnimationSpeedAction.RESOURCE_LOCATION, AzRootSetAnimationSpeedAction.CODEC);
        register(AzRootSetEasingTypeAction.RESOURCE_LOCATION, AzRootSetEasingTypeAction.CODEC);
        register(AzRootSetTransitionSpeedAction.RESOURCE_LOCATION, AzRootSetTransitionSpeedAction.CODEC);
        register(AzRootSetStartTickOffsetAction.RESOURCE_LOCATION, AzRootSetStartTickOffsetAction.CODEC);
        register(AzRootSetFreezeTickAction.RESOURCE_LOCATION, AzRootSetFreezeTickAction.CODEC);
        register(AzRootSetRepeatTimesAction.RESOURCE_LOCATION, AzRootSetRepeatTimesAction.CODEC);
        register(AzRootSetReverseAction.RESOURCE_LOCATION, AzRootSetReverseAction.CODEC);

        // Controller actions
        register(AzControllerCancelAction.RESOURCE_LOCATION, AzControllerCancelAction.CODEC);
        register(
            AzControllerPlayAnimationSequenceAction.RESOURCE_LOCATION,
            AzControllerPlayAnimationSequenceAction.CODEC
        );
        register(AzControllerSetAnimationSpeedAction.RESOURCE_LOCATION, AzControllerSetAnimationSpeedAction.CODEC);
        register(AzControllerSetEasingTypeAction.RESOURCE_LOCATION, AzControllerSetEasingTypeAction.CODEC);
        register(AzControllerSetTransitionSpeedAction.RESOURCE_LOCATION, AzControllerSetTransitionSpeedAction.CODEC);
        register(AzControllerSetStartTickOffsetAction.RESOURCE_LOCATION, AzControllerSetStartTickOffsetAction.CODEC);
        register(AzControllerSetFreezeTickAction.RESOURCE_LOCATION, AzControllerSetFreezeTickAction.CODEC);
        register(AzControllerSetRepeatTimesAction.RESOURCE_LOCATION, AzControllerSetRepeatTimesAction.CODEC);
        register(AzControllerSetReverseAction.RESOURCE_LOCATION, AzControllerSetReverseAction.CODEC);
    }

    private AzActionRegistry() {}

    public static @Nullable <A, T extends StreamCodec<FriendlyByteBuf, A>> T getCodecOrNull(
            Identifier resourceLocation
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

    public static @Nullable Short getIdOrNull(Identifier resourceLocation) {
        return RESOURCE_LOCATION_TO_ID.get(resourceLocation);
    }

    private static <A extends AzAction> void register(
            Identifier resourceLocation,
        StreamCodec<FriendlyByteBuf, A> codec
    ) {
        var id = RESOURCE_LOCATION_TO_ID.computeIfAbsent(resourceLocation, $ -> NEXT_FREE_ID++);
        CODEC_BY_ID.put(id, codec);
    }
}
