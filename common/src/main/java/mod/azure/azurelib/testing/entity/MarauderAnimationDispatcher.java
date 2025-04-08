package mod.azure.azurelib.testing.entity;

import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.testing.CommonStrings;

/**
 * The MarauderAnimationDispatcher class is responsible for managing and dispatching animation commands for entities,
 * specifically tailored for the Marauder entity. It uses predefined animation commands to sync animations with specific
 * entity actions such as idling, walking, running, melee attacking, spawning, or dying. This dispatcher operates
 * independently, providing a clean and centralized way to handle animation transitions, triggered by client or server
 * events.
 */
public class MarauderAnimationDispatcher {

    private final AzCommand deathCommand = AzCommand.create(
        CommonStrings.BASE_CONTROLLER,
        CommonStrings.DEATH_ANIMATION_NAME,
        AzPlayBehaviors.HOLD_ON_LAST_FRAME
    );

    private final AzCommand idleCommand = AzCommand.create(
        CommonStrings.BASE_CONTROLLER,
        CommonStrings.IDLE_ANIMATION_NAME,
        AzPlayBehaviors.LOOP
    );

    private final AzCommand walkCommand = AzCommand.create(
        CommonStrings.BASE_CONTROLLER,
        CommonStrings.WALK_ANIMATION_NAME,
        AzPlayBehaviors.LOOP
    );

    private final AzCommand runCommand = AzCommand.create(
        CommonStrings.BASE_CONTROLLER,
        CommonStrings.RUN_ANIMATION_NAME,
        AzPlayBehaviors.LOOP
    );

    private final AzCommand meleeCommand = AzCommand.create(
        CommonStrings.BASE_CONTROLLER,
        CommonStrings.MELEE_ANIMATION_NAME,
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand spawnCommand = AzCommand.create(
        CommonStrings.BASE_CONTROLLER,
        CommonStrings.SPAWN_ANIMATION_NAME,
        AzPlayBehaviors.PLAY_ONCE
    );

    private final MarauderEntity marauder;

    public MarauderAnimationDispatcher(MarauderEntity marauder) {
        this.marauder = marauder;
    }

    public void clientIdle() {
        idleCommand.sendForEntity(marauder);
    }

    public void clientWalk() {
        walkCommand.sendForEntity(marauder);
    }

    public void clientRun() {
        runCommand.sendForEntity(marauder);
    }

    public void serverMelee() {
        meleeCommand.sendForEntity(marauder);
    }

    public void clientDeath() {
        deathCommand.sendForEntity(marauder);
    }

    public void clientSpawn() {
        spawnCommand.sendForEntity(marauder);
    }
}
