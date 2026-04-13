package mod.azure.azurelib.animation.controller;

import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;

// TODO: This will eventually be usable in common-side code once animations are moved from assets to data.
public class AzAbstractAnimationController {

    private final String name;

    protected AzAnimationSequence currentSequence;

    protected AzDispatchSide currentSequenceOrigin;

    protected AzAbstractAnimationController(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    /**
     * Checks whether the last animation playing on this controller has finished or not.<br>
     * This will return true if the controller has had an animation set previously, and it has finished playing and
     * isn't going to loop or proceed to another animation.<br>
     *
     * @return Whether the previous animation finished or not
     */
    public boolean hasAnimationFinished() {
        return currentSequence != null;
    }
}
