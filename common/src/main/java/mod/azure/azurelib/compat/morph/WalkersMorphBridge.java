package mod.azure.azurelib.compat.morph;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.mixins.EntityRenderDispatcherAccessor;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class WalkersMorphBridge {

    private static final String MOD_ID = "walkers";

    private static final String[] API_PACKAGES = { "dev.tocraft.walkers.api.model.", "tocraft.walkers.api.model." };

    private enum State {
        UNCHECKED,
        PENDING,
        DONE,
        DISABLED
    }

    private static State state = State.UNCHECKED;

    private static Method registerMethod;

    private static @Nullable Method getUpdaterMethod;

    private static Method updateMethod;

    private static Class<?> updaterInterface;

    private WalkersMorphBridge() {}

    public static void clientTick() {
        switch (state) {
            case DONE, DISABLED -> {
                return;
            }
            case UNCHECKED -> resolve();
            default -> {}
        }

        if (state == State.PENDING) {
            tryHook();
        }
    }

    private static void resolve() {
        if (!Services.PLATFORM.isModLoaded(MOD_ID)) {
            state = State.DISABLED;
            return;
        }

        try {
            Class<?> updatersClass = null;

            for (var apiPackage : API_PACKAGES) {
                try {
                    updatersClass = Class.forName(apiPackage + "EntityUpdaters");
                    updaterInterface = Class.forName(apiPackage + "EntityUpdater");
                    break;
                } catch (ClassNotFoundException ignored) {
                    updatersClass = null;
                }
            }

            if (updatersClass == null) {
                throw new ClassNotFoundException("Walkers EntityUpdaters in any known package");
            }

            registerMethod = updatersClass.getMethod("register", EntityType.class, updaterInterface);
            getUpdaterMethod = findMethod(updatersClass, "getUpdater", 1);
            updateMethod = findMethod(updaterInterface, "update", 2);

            if (updateMethod == null) {
                throw new NoSuchMethodException("EntityUpdater.update(Player, LivingEntity)");
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            state = State.DISABLED;
            AzureLib.LOGGER.warn("Walkers detected but its shape hook could not be found; morphs will not animate", exception);
            return;
        }

        state = State.PENDING;
    }

    private static void tryHook() {
        var renderers = ((EntityRenderDispatcherAccessor) Minecraft.getInstance().getEntityRenderDispatcher())
            .azurelib$getRenderers();

        if (renderers.isEmpty()) {
            return;
        }

        var hooked = 0;

        try {
            for (var type : Registry.ENTITY_TYPE) {
                if (AzMorphCompat.isExcluded(type)) {
                    continue;
                }

                if (!(renderers.get(type) instanceof AzEntityRenderer<?>) && !AzMorphCompat.hasExplicitHandler(type)) {
                    continue;
                }

                var previous = getUpdaterMethod == null ? null : getUpdaterMethod.invoke(null, type);
                registerMethod.invoke(null, type, createUpdater(previous));
                hooked++;
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            state = State.DISABLED;
            AzureLib.LOGGER.warn(
                "Walkers detected but its shape hook could not be registered; morphs will not animate",
                exception
            );
            return;
        }

        state = State.DONE;
        AzureLib.LOGGER.info("Walkers detected: morph animations hooked for {} AzureLib entity types", hooked);
    }

    private static Object createUpdater(@Nullable Object previous) {
        var chained = new Object[] { previous };

        return Proxy.newProxyInstance(
            updaterInterface.getClassLoader(),
            new Class<?>[] { updaterInterface },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "update" -> {
                        if (args == null || args.length != 2) {
                            return null;
                        }

                        if (chained[0] != null) {
                            try {
                                updateMethod.invoke(chained[0], args);
                            } catch (InvocationTargetException | IllegalAccessException | RuntimeException exception) {
                                AzureLib.LOGGER.warn(
                                    "Walkers' own updater threw; dropping it for this type",
                                    exception
                                );
                                chained[0] = null;
                            }
                        }

                        if (args[0] instanceof Player player && args[1] instanceof LivingEntity shape) {
                            AzMorphCompat.drive(player, shape);
                        }

                        return null;
                    }
                    case "hashCode" -> {
                        return System.identityHashCode(proxy);
                    }
                    case "equals" -> {
                        return args != null && args.length == 1 && proxy == args[0];
                    }
                    case "toString" -> {
                        return "AzureLib morph updater";
                    }
                    default -> {
                        return null;
                    }
                }
            }
        );
    }

    private static @Nullable Method findMethod(Class<?> owner, String name, int parameterCount) {
        for (var method : owner.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                return method;
            }
        }

        return null;
    }
}
