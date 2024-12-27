package mod.azure.azurelib.fabric.core2.example.items;

import mod.azure.azurelib.core2.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.core2.animation.dispatch.command.AzCommand;

public class AzPistolAnimationDispatcher {

    private static final String FIRING_ANIMATION_NAME = "firing";

    private static final AzCommand FIRING_COMMAND = AzCommand.create(
        "base_controller",
        FIRING_ANIMATION_NAME,
        AzPlayBehaviors.PLAY_ONCE
    );

    public void firing(Entity entity, ItemStack itemStack) {
        FIRING_COMMAND.sendForItem(entity, itemStack);
    }
}
