package mod.azure.azurelib.rewrite.animation.dispatch.command.action.registry;

import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The AzActionRegistry class is responsible for managing the registration and resolution of actions
 * (implementations of {@link AzAction}) within an animation system. It serves as a centralized registry
 * where each action is associated with a unique {@link ResourceLocation} and an internally generated
 * identifier for efficient lookup.
 */
public class AzActionRegistry {

    private static final Map<ResourceLocation, Short> RESOURCE_LOCATION_TO_ID = new Object2ShortArrayMap<>();

    private static final Map<Short, Class<? extends AzAction>> ACTION_CLASS_BY_ID =
        new HashMap<>();

    private static short NEXT_FREE_ID = 0;

    static {
        // Root actions
        register(AzRootCancelAction.RESOURCE_LOCATION, AzRootCancelAction.class);
        register(AzRootCancelAllAction.RESOURCE_LOCATION, AzRootCancelAllAction.class);
        register(AzRootPlayAnimationSequenceAction.RESOURCE_LOCATION, AzRootPlayAnimationSequenceAction.class);
        register(AzRootSetAnimationSpeedAction.RESOURCE_LOCATION, AzRootSetAnimationSpeedAction.class);
        register(AzRootSetEasingTypeAction.RESOURCE_LOCATION, AzRootSetEasingTypeAction.class);
        register(AzRootSetTransitionSpeedAction.RESOURCE_LOCATION, AzRootSetTransitionSpeedAction.class);

        // Controller actions
        // TODO:

        // Animation actions
        // TODO:
    }

    public static @Nullable Class<? extends AzAction> getActionClassOrNull(ResourceLocation resourceLocation) {
        var id = RESOURCE_LOCATION_TO_ID.get(resourceLocation);
        return id == null ? null : ACTION_CLASS_BY_ID.get(id);
    }

    public static @Nullable Class<? extends AzAction> getActionClassOrNull(short id) {
        return ACTION_CLASS_BY_ID.get(id);
    }

    public static @Nullable Short getIdOrNull(ResourceLocation resourceLocation) {
        return RESOURCE_LOCATION_TO_ID.get(resourceLocation);
    }

    private static <A extends AzAction> void register(ResourceLocation resourceLocation, Class<A> clazz) {
        Short id = RESOURCE_LOCATION_TO_ID.computeIfAbsent(resourceLocation, ($) -> NEXT_FREE_ID++);
        ACTION_CLASS_BY_ID.put(id, clazz);
    }

    public static @Nullable AzAction decode(FriendlyByteBuf buf) {
        var id = buf.readShort();
        var actionClass = getActionClassOrNull(id);

        if (actionClass == null) {
            throw new NullPointerException(
                    "Could not find action class for a given action id while decoding data. ID: " + id
            );
        }

        try {
            // Use the static decode method in the corresponding action class
            return (AzAction) actionClass.getDeclaredMethod("decode", FriendlyByteBuf.class).invoke(null, buf);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to decode action for ID: " + id, e);
        }
    }

    public static void encode(FriendlyByteBuf buf, AzAction action) {
        var resourceLocation = action.getResourceLocation();
        var id = getIdOrNull(resourceLocation);
        var actionClass = getActionClassOrNull(resourceLocation);

        if (id == null) {
            throw new NullPointerException(
                    "Could not find action id for a given resource location while encoding data. Resource Location: "
                            + resourceLocation
            );
        }

        if (actionClass == null) {
            throw new NullPointerException(
                    "Could not find action class for a given resource location while encoding data. Resource Location: "
                            + resourceLocation + ", ID: " + id
            );
        }

        buf.writeShort(id);

        try {
            // Use the static encode method in the corresponding action class
            actionClass.getDeclaredMethod("encode", FriendlyByteBuf.class, AzAction.class)
                    .invoke(null, buf, action);
        } catch (ReflectiveOperationException e) {
            throw new AzureLibException("Failed to encode action for Resource Location: " + resourceLocation, e);
        }
    }

}
