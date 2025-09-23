package mod.azure.azurelib.rewrite.testing.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehaviors;

public class PistolAnimationDispatcher {

    private static final AzCommand FIRING_COMMAND = AzCommand.create(
        "base_controller",
        "firing",
        AzPlayBehaviors.PLAY_ONCE
    );

    public void serverFire(Entity entity, ItemStack itemStack) {
        FIRING_COMMAND.sendForItem(entity, itemStack);
    }
}
