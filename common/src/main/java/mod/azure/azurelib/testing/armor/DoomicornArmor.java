package mod.azure.azurelib.testing.armor;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DoomicornArmor extends ArmorItem {

    private final DoomicornArmorAnimationDispatcher dispatcher;

    public DoomicornArmor(Type type) {
        super(ArmorMaterials.NETHERITE, type, new Properties().stacksTo(1));
        this.dispatcher = new DoomicornArmorAnimationDispatcher();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> swapWithEquipmentSlot(
        @NotNull Item item,
        @NotNull Level level,
        @NotNull Player player,
        @NotNull InteractionHand hand
    ) {
        InteractionResultHolder<ItemStack> result = super.swapWithEquipmentSlot(item, level, player, hand);

        if (!level.isClientSide) {
            EquipmentSlot slot = getEquipmentSlot();
            ItemStack itemStack = player.getItemBySlot(slot);
            dispatcher.serverEquipHelmet(player, itemStack);
        }

        return result;
    }
}
