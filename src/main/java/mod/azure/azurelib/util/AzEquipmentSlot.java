package mod.azure.azurelib.util;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.Mc;

/**
 * Equipment slots, standing in for the {@code EntityEquipmentSlot} enum that Minecraft b1.7.10 does not have.
 */
public enum AzEquipmentSlot {

    MAINHAND(Type.HAND, 0, -1),
    OFFHAND(Type.HAND, -1, -1),
    FEET(Type.ARMOR, 1, 3),
    LEGS(Type.ARMOR, 2, 2),
    CHEST(Type.ARMOR, 3, 1),
    HEAD(Type.ARMOR, 4, 0);

    private final Type type;

    private final int equipmentIndex;

    private final int armorType;

    AzEquipmentSlot(Type type, int equipmentIndex, int armorType) {
        this.type = type;
        this.equipmentIndex = equipmentIndex;
        this.armorType = armorType;
    }

    public Type getSlotType() {
        return this.type;
    }

    /** 1.7.10 equipment index (0 held item, 1 boots .. 4 helmet), or -1 for {@link #OFFHAND} (Beta has none). */
    public int getEquipmentIndex() {
        return this.equipmentIndex;
    }

    /** {@code ItemArmor#armorType} / armor render pass index (0 helmet .. 3 boots), or -1 for hand slots. */
    public int getArmorType() {
        return this.armorType;
    }

    @Nullable
    public ItemStack getStack(MobEntity entity) {
        if (this.equipmentIndex < 0) {
            return null;
        }
        // Beta's PlayerInventory#armor is indexed boots (0) .. helmet (3).
        return this.equipmentIndex == 0
            ? Mc.heldItem(entity)
            : Mc.armor(entity, this.equipmentIndex - 1);
    }

    public static AzEquipmentSlot fromArmorType(int armorType) {
        switch (armorType) {
            case 0:
                return HEAD;
            case 1:
                return CHEST;
            case 2:
                return LEGS;
            case 3:
                return FEET;
            default:
                return CHEST;
        }
    }

    public enum Type {
        HAND,
        ARMOR
    }
}
