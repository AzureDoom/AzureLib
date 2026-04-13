package mod.azure.azurelib.animation.controller;

// TODO: This will eventually be usable in common-side code once animations are moved from assets to data.
public abstract class AzAbstractAnimationController {

    private final String name;

    protected AzAbstractAnimationController(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public abstract boolean hasAnimationFinished();
}
