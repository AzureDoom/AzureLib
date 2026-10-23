package mod.azure.azurelib.common.compat.morph;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import mod.azure.azurelib.AzureLib;

public final class AzMorphCompat {

    private static final Map<EntityType<?>, AzMorphHandler<?>> TYPE_HANDLERS = new ConcurrentHashMap<>();

    private static final Map<String, AzMorphHandler<LivingEntity>> NAMESPACE_HANDLERS = new ConcurrentHashMap<>();

    private static final Set<EntityType<?>> EXCLUDED = ConcurrentHashMap.newKeySet();

    private static final Set<EntityType<?>> FAILED = ConcurrentHashMap.newKeySet();

    private static final Map<LivingEntity, AzMorphContext> CONTEXTS = new WeakHashMap<>();

    private AzMorphCompat() {}

    public static <T extends LivingEntity> void register(EntityType<T> type, AzMorphHandler<? super T> handler) {
        TYPE_HANDLERS.put(type, handler);
    }

    public static void registerNamespace(String namespace, AzMorphHandler<LivingEntity> handler) {
        NAMESPACE_HANDLERS.put(namespace, handler);
    }

    public static void exclude(EntityType<?> type) {
        EXCLUDED.add(type);
    }

    public static boolean isMorphCopy(Entity entity) {
        return entity instanceof LivingEntity living && CONTEXTS.containsKey(living);
    }

    public static @Nullable AzMorphContext contextOf(Entity entity) {
        return entity instanceof LivingEntity living ? CONTEXTS.get(living) : null;
    }

    static boolean isExcluded(EntityType<?> type) {
        return EXCLUDED.contains(type);
    }

    static boolean hasExplicitHandler(EntityType<?> type) {
        return TYPE_HANDLERS.containsKey(type) || NAMESPACE_HANDLERS.containsKey(namespaceOf(type));
    }

    static void drive(Player player, LivingEntity shape) {
        var context = CONTEXTS.computeIfAbsent(shape, AzMorphContext::new);
        context.player = player;

        applyFrameDefaults(context);

        if (player.tickCount == context.lastPlayerTick) {
            return;
        }

        context.lastPlayerTick = player.tickCount;
        advanceTick(context);
        runHandler(context);
        context.wasSwinging = context.swinging;
    }

    private static void applyFrameDefaults(AzMorphContext context) {
        var player = context.player;
        var shape = context.shape;

        context.deltaX = player.getX() - player.xo;
        context.deltaY = player.getY() - player.yo;
        context.deltaZ = player.getZ() - player.zo;

        shape.xo = player.xo;
        shape.yo = player.yo;
        shape.zo = player.zo;
        shape.setPosRaw(player.getX(), player.getY(), player.getZ());
        shape.setOnGround(player.onGround());
        shape.setDeltaMovement(context.deltaX, context.deltaY, context.deltaZ);
        shape.setSprinting(player.isSprinting());
        shape.setShiftKeyDown(player.isShiftKeyDown());
    }

    private static void advanceTick(AzMorphContext context) {
        var player = context.player;
        var shape = context.shape;

        shape.tickCount++;
        shape.hurtTime = player.hurtTime;
        context.swinging = player.swinging;
        context.morphTicks++;
    }

    @SuppressWarnings("unchecked")
    private static void runHandler(AzMorphContext context) {
        var shape = context.shape;
        var type = shape.getType();

        if (FAILED.contains(type)) {
            return;
        }

        try {
            var handler = (AzMorphHandler<LivingEntity>) TYPE_HANDLERS.get(type);

            if (handler == null) {
                handler = NAMESPACE_HANDLERS.get(namespaceOf(type));
            }

            if (handler != null) {
                handler.onMorphTick(shape, context);
            } else if (shape instanceof AzMorphable morphable) {
                morphable.onMorphTick(context);
            }
        } catch (RuntimeException exception) {
            FAILED.add(type);
            AzureLib.LOGGER.warn(
                "Morph handler for {} failed; its morphs fall back to default animation",
                type,
                exception
            );
        }
    }

    private static String namespaceOf(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace();
    }
}
