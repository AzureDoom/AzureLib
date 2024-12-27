package mod.azure.azurelib.fabric.core2.example.entities.marauder;

import mod.azure.azurelib.core2.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.core2.animation.play_behavior.AzPlayBehaviors;

public class MarauderAnimationDispatcher {

    private static final AzCommand DEATH = AzCommand.create(
        "base_controller",
        "death",
        AzPlayBehaviors.HOLD_ON_LAST_FRAME
    );

    private static final AzCommand IDLE = AzCommand.create("base_controller", "idle", AzPlayBehaviors.LOOP);

    private static final AzCommand RUN = AzCommand.create("base_controller", "run", AzPlayBehaviors.LOOP);

    private static final AzCommand SPAWN = AzCommand.create(
        "base_controller",
        "spawn",
        AzPlayBehaviors.PLAY_ONCE
    );

    private static final AzCommand AXE_ATTACK = AzCommand.create(
            "base_controller",
            "axe_attack",
        AzPlayBehaviors.PLAY_ONCE
    );

    private static final AzCommand WALK = AzCommand.create("base_controller", "walk", AzPlayBehaviors.LOOP);

    private final MarauderEntity marauder;

    public MarauderAnimationDispatcher(MarauderEntity marauder) {
        this.marauder = marauder;
    }

    public void death() {
        DEATH.sendForEntity(marauder);
    }

    public void idle() {
        IDLE.sendForEntity(marauder);
    }

    public void run() {
        RUN.sendForEntity(marauder);
    }

    public void spawn() {
        SPAWN.sendForEntity(marauder);
    }

    public void walk() {
        WALK.sendForEntity(marauder);
    }

    public void serverMelee() {
        AXE_ATTACK.sendForEntity(marauder);
    }
}
