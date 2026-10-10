package mod.azure.azurelib.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Equipment slots, standing in for the {@code EntityEquipmentSlot} enum that Minecraft 1.7.10 does not have.
 * <p>
 * 1.7.10 addresses equipment by index: {@code getEquipmentInSlot(0)} is the held item and 1..4 are boots, leggings,
 * chestplate and helmet. {@code ItemArmor#armorType} and the armor render pass use 0 helmet .. 3 boots. There is no off
 * hand on 1.7.10, so {@link #OFFHAND} always resolves to {@code null}.
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

    /** Index for {@code EntityLivingBase#getEquipmentInSlot}, or -1 for {@link #OFFHAND}. */
    public int getEquipmentIndex() {
        return this.equipmentIndex;
    }

    /** {@code ItemArmor#armorType} / armor render pass index (0 helmet .. 3 boots), or -1 for hand slots. */
    public int getArmorType() {
        return this.armorType;
    }

    @Nullable
    public ItemStack getStack(EntityLivingBase entity) {
        return this.equipmentIndex < 0 ? null : entity.getEquipmentInSlot(this.equipmentIndex);
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
