package mod.azure.azurelib.fabric.core2.example.entities.marauder;

import mod.azure.azurelib.core2.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.core2.animation.primitive.AzLoopType;

public class MarauderAnimationDispatcher {

    private static final AzCommand DEATH = AzCommand.create(
        "base_controller",
        "death",
        AzLoopType.HOLD_ON_LAST_FRAME
    );

    private static final AzCommand IDLE = AzCommand.create("base_controller", "idle", AzLoopType.LOOP);

    private static final AzCommand RUN = AzCommand.create("base_controller", "run", AzLoopType.LOOP);

    private static final AzCommand SPAWN = AzCommand.create(
        "base_controller",
        "spawn",
        AzLoopType.PLAY_ONCE
    );

    private static final AzCommand AXE_ATTACK = AzCommand.create(
            "base_controller",
            "axe_attack",
            AzLoopType.PLAY_ONCE
    );

    private static final AzCommand WALK = AzCommand.create("base_controller", "walk", AzLoopType.LOOP);

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
