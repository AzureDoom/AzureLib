package mod.azure.azurelib.testing.armor;

import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.testing.CommonStrings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class DoomicornArmorAnimationDispatcher {

    private static final AzCommand EQUIP = AzCommand.create(
        CommonStrings.BASE_CONTROLLER,
        CommonStrings.EQUIP_ANIMATION_NAME
    );

    public void serverEquipHelmet(Entity entity, ItemStack itemStack) {
        EQUIP.sendForItem(entity, itemStack);
    }
}
