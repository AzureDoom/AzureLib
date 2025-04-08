package mod.azure.azurelib.testing.item;

import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.testing.CommonStrings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class PistolAnimationDispatcher {

    private static final AzCommand FIRING_COMMAND = AzCommand.create(
        CommonStrings.BASE_CONTROLLER,
        CommonStrings.FIRING_ANIMATION_NAME,
        AzPlayBehaviors.PLAY_ONCE
    );

    public void serverFire(Entity entity, ItemStack itemStack) {
        FIRING_COMMAND.sendForItem(entity, itemStack);
    }
}
