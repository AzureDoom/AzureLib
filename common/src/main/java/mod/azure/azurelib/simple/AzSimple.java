package mod.azure.azurelib.simple;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;

/**
 * The entry point of AzureLib's Simple API: a beginner-friendly layer over the regular animator, controller, command
 * and renderer classes.
 *
 * <pre>{@code
 * // Client: one renderer class, no animator or dispatcher needed.
 * public class MyEntityRenderer extends AzSimpleEntityRenderer<MyEntity> {
 *
 *     public MyEntityRenderer(EntityRendererProvider.Context context) {
 *         super(context, AzSimple.entity("mymod", "my_entity").idle("idle").walk("walk").transition(5).shadow(0.5f));
 *     }
 * }
 *
 * // Anywhere, usually server side: trigger animations.
 * AzSimple.play(entity, "attack"); // plays once, then returns to idle/walk
 * AzSimple.loop(entity, "dance"); // loops until stopped or replaced
 * AzSimple.stop(entity); // back to idle/walk
 * }</pre>
 * <p>
 * Assets are found by convention: {@code entity("mymod", "my_entity")} uses
 * {@code assets/mymod/geo/entity/my_entity.geo.json}, {@code textures/entity/my_entity.png} and
 * {@code animations/entity/my_entity.animation.json}; items and block entities use the {@code item} and {@code block}
 * folders. Override any of them on the builder.
 * </p>
 */
@SuppressWarnings("unused")
public final class AzSimple {

    public static final String BASE_CONTROLLER = "base_controller";

    private AzSimple() {}

    public static <T extends Entity> AzSimpleConfig.EntityBuilder<T> entity(String namespace, String name) {
        return entity(new ResourceLocation(namespace, name));
    }

    public static <T extends Entity> AzSimpleConfig.EntityBuilder<T> entity(ResourceLocation id) {
        return new AzSimpleConfig.EntityBuilder<>(id);
    }

    public static AzSimpleConfig.ItemBuilder item(String namespace, String name) {
        return item(new ResourceLocation(namespace, name));
    }

    public static AzSimpleConfig.ItemBuilder item(ResourceLocation id) {
        return new AzSimpleConfig.ItemBuilder(id);
    }

    public static <T extends BlockEntity> AzSimpleConfig.BlockBuilder<T> block(String namespace, String name) {
        return block(new ResourceLocation(namespace, name));
    }

    public static <T extends BlockEntity> AzSimpleConfig.BlockBuilder<T> block(ResourceLocation id) {
        return new AzSimpleConfig.BlockBuilder<>(id);
    }

    /**
     * The command behind every play/loop/stop method, for callers that want to cache it or compose it with other
     * commands.
     *
     * @param layer     the layer to target, or {@code null} to let the animator choose
     * @param animation the animation name; ignored for {@link AzSimplePlayMode#STOP}
     * @param mode      what to do
     */
    public static AzCommand command(@Nullable String layer, @Nullable String animation, AzSimplePlayMode mode) {
        return new AzCommand(List.of(new AzSimplePlayAction(layer, animation, mode)));
    }

    /**
     * Plays {@code animation} once on the entity for everyone tracking it, then returns to the automatic state. Called
     * on the client it only plays locally, and never overrides an animation the server started.
     */
    public static void play(Entity entity, String animation) {
        send(entity, null, animation, AzSimplePlayMode.PLAY);
    }

    /**
     * Loops {@code animation} on the entity until it is stopped or replaced.
     */
    public static void loop(Entity entity, String animation) {
        send(entity, null, animation, AzSimplePlayMode.LOOP);
    }

    /**
     * Stops triggered animations on the entity and returns to the automatic state.
     */
    public static void stop(Entity entity) {
        send(entity, null, null, AzSimplePlayMode.STOP);
    }

    /**
     * Plays {@code animation} once on one layer of the entity.
     */
    public static void playOnLayer(Entity entity, String layer, String animation) {
        send(entity, layer, animation, AzSimplePlayMode.PLAY);
    }

    /**
     * Loops {@code animation} on one layer of the entity.
     */
    public static void loopOnLayer(Entity entity, String layer, String animation) {
        send(entity, layer, animation, AzSimplePlayMode.LOOP);
    }

    /**
     * Stops whatever was triggered on one layer of the entity.
     */
    public static void stopLayer(Entity entity, String layer) {
        send(entity, layer, null, AzSimplePlayMode.STOP);
    }

    private static void send(
        @Nullable Entity entity,
        @Nullable String layer,
        @Nullable String animation,
        AzSimplePlayMode mode
    ) {
        if (entity == null || !isValid(animation, mode)) {
            return;
        }

        command(layer, animation, mode).sendForEntity(entity);
    }

    /**
     * Plays {@code animation} once on this stack, in every view. Pass the stack that lives in the holder's inventory,
     * not a copy: on the server this is where the stack gets its persistent identity. The item must be registered with
     * {@link AzIdentityRegistry#register}.
     *
     * @param stack  the animated stack
     * @param holder the entity holding the stack; used to find who receives the animation
     */
    public static void play(ItemStack stack, Entity holder, String animation) {
        send(stack, holder, null, animation, AzSimplePlayMode.PLAY, null);
    }

    /**
     * Loops {@code animation} on this stack until it is stopped or replaced.
     */
    public static void loop(ItemStack stack, Entity holder, String animation) {
        send(stack, holder, null, animation, AzSimplePlayMode.LOOP, null);
    }

    /**
     * Stops triggered animations on this stack.
     */
    public static void stop(ItemStack stack, Entity holder) {
        send(stack, holder, null, null, AzSimplePlayMode.STOP, null);
    }

    /**
     * Plays {@code animation} once on one view of the stack. Only differs from {@link #play(ItemStack, Entity, String)}
     * when the renderer uses {@code separateFirstPersonAnimator()}.
     */
    public static void play(ItemStack stack, Entity holder, String animation, AzItemAnimatorView view) {
        send(stack, holder, null, animation, AzSimplePlayMode.PLAY, view);
    }

    /**
     * Shorthand for {@code play(stack, holder, animation, AzItemAnimatorView.FIRST_PERSON)}.
     */
    public static void playFirstPerson(ItemStack stack, Entity holder, String animation) {
        play(stack, holder, animation, AzItemAnimatorView.FIRST_PERSON);
    }

    /**
     * Shorthand for {@code play(stack, holder, animation, AzItemAnimatorView.DEFAULT)}.
     */
    public static void playThirdPerson(ItemStack stack, Entity holder, String animation) {
        play(stack, holder, animation, AzItemAnimatorView.DEFAULT);
    }

    /**
     * Plays {@code animation} once on one layer of the stack.
     */
    public static void playOnLayer(ItemStack stack, Entity holder, String layer, String animation) {
        send(stack, holder, layer, animation, AzSimplePlayMode.PLAY, null);
    }

    private static void send(
        @Nullable ItemStack stack,
        @Nullable Entity holder,
        @Nullable String layer,
        @Nullable String animation,
        AzSimplePlayMode mode,
        @Nullable AzItemAnimatorView view
    ) {
        if (stack == null || stack.isEmpty() || holder == null || !isValid(animation, mode)) {
            return;
        }

        command(layer, animation, mode).sendForItem(holder, stack, view);
    }

    /**
     * Plays {@code animation} once on the block entity for every player tracking its chunk.
     */
    public static void play(BlockEntity blockEntity, String animation) {
        send(blockEntity, null, animation, AzSimplePlayMode.PLAY);
    }

    /**
     * Loops {@code animation} on the block entity until it is stopped or replaced.
     */
    public static void loop(BlockEntity blockEntity, String animation) {
        send(blockEntity, null, animation, AzSimplePlayMode.LOOP);
    }

    /**
     * Stops triggered animations on the block entity and returns to the automatic state.
     */
    public static void stop(BlockEntity blockEntity) {
        send(blockEntity, null, null, AzSimplePlayMode.STOP);
    }

    /**
     * Plays {@code animation} once on one layer of the block entity.
     */
    public static void playOnLayer(BlockEntity blockEntity, String layer, String animation) {
        send(blockEntity, layer, animation, AzSimplePlayMode.PLAY);
    }

    private static void send(
        @Nullable BlockEntity blockEntity,
        @Nullable String layer,
        @Nullable String animation,
        AzSimplePlayMode mode
    ) {
        if (blockEntity == null || blockEntity.getLevel() == null || !isValid(animation, mode)) {
            return;
        }

        command(layer, animation, mode).sendForBlockEntity(blockEntity);
    }

    private static boolean isValid(@Nullable String animation, AzSimplePlayMode mode) {
        return mode == AzSimplePlayMode.STOP || (animation != null && !animation.isEmpty());
    }
}
