package mod.azure.azurelib.animation.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;

/**
 * Represents a queue of animations to be processed in a sequential manner. This class manages a collection of
 * {@link AzQueuedAnimation} objects, allowing animations to be queued, retrieved, and cleared efficiently. It ensures
 * that animations are processed in the order they are added. <br/>
 * <br/>
 * The queue supports operations to inspect the next animation without removal, retrieve and remove the next animation,
 * add individual or multiple animations, and clear the entire queue. Additionally, it provides a method to determine if
 * the queue is empty.
 */
public class AzAnimationQueue {

    private final Queue<AzQueuedAnimation> animationQueue;

    public AzAnimationQueue() {
        this.animationQueue = new LinkedList<>();
    }

    /**
     * Adds the specified {@link AzQueuedAnimation} to the animation queue. The animation will be added to the end of
     * the queue and processed in the order it was added.
     *
     * @param queuedAnimation The {@link AzQueuedAnimation} to be added to the animation queue. This parameter must not
     *                        be null.
     */
    public void add(@NotNull AzQueuedAnimation queuedAnimation) {
        animationQueue.add(queuedAnimation);
    }

    /**
     * Adds all the specified {@link AzQueuedAnimation} objects to the animation queue. The animations will be added to
     * the end of the queue and processed in the order they are added.
     *
     * @param queuedAnimations The collection of {@link AzQueuedAnimation} objects to be added to the animation queue.
     *                         This parameter must not be null.
     */
    public void addAll(@NotNull Collection<AzQueuedAnimation> queuedAnimations) {
        animationQueue.addAll(queuedAnimations);
    }

    /**
     * Retrieves the next {@link AzQueuedAnimation} in the queue without removing it. If the queue is empty, this method
     * returns {@code null}. This allows inspection of the next animation to be processed without altering the queue's
     * state.
     *
     * @return The next {@link AzQueuedAnimation} in the queue, or {@code null} if the queue is empty.
     */
    public @Nullable AzQueuedAnimation peek() {
        return animationQueue.peek();
    }

    /**
     * Retrieves and removes the next {@link AzQueuedAnimation} from the animation queue. If the queue is empty, this
     * method returns {@code null}. This operation removes the retrieved animation from the queue.
     *
     * @return The next {@link AzQueuedAnimation} in the queue, or {@code null} if the queue is empty.
     */
    public @Nullable AzQueuedAnimation next() {
        return animationQueue.poll();
    }

    /**
     * Clears all animations from the animation queue. This method removes all {@link AzQueuedAnimation} objects
     * currently stored in the queue, leaving it empty.
     */
    public void clear() {
        animationQueue.clear();
    }

    /**
     * Checks if the animation queue is empty.
     *
     * @return {@code true} if the animation queue contains no animations, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return animationQueue.isEmpty();
    }
}
