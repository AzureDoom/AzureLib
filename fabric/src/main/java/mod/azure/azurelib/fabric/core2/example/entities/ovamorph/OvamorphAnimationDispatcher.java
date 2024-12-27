package mod.azure.azurelib.fabric.core2.example.entities.ovamorph;

import mod.azure.azurelib.core2.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.core2.animation.play_behavior.AzPlayBehaviors;

public class OvamorphAnimationDispatcher {

    private static final AzCommand OPEN = AzCommand.create(
        OvamorphAnimationRefs.BASE_CONTROLLER_NAME, OvamorphAnimationRefs.OPEN_ANIMATION_NAME, AzPlayBehaviors.HOLD_ON_LAST_FRAME
    );

    private final Ovamorph ovamorph;

    public OvamorphAnimationDispatcher(Ovamorph ovamorph) {
        this.ovamorph = ovamorph;
    }

    public void open() {
        OPEN.sendForEntity(ovamorph);
    }
}
