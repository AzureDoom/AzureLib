package mod.azure.azurelib.common.api.common.builders;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

/**
 * Builder class for setting up gun properties
 *
 * @author AzureDoom/Boston Vanseghi
 */
public class AzureGunProperties {
    private int ammoCount;
    private int reloadAmount;
    private int reloadCooldown;
    private SoundEvent reloadSound;
    private Item ammoItem;
    private int firingCooldown;
    private float damage;
    private int usedamage;
    private SoundEvent firingSound;
    private SoundEvent emptySound;
    private Enchantment enchantmentExtraDamage;

    public AzureGunProperties() {
    }

    public int getAmmoCount() {
        return this.ammoCount;
    }

    public int getReloadAmount() {
        return this.reloadAmount;
    }

    public int getReloadCooldown() {
        return this.reloadCooldown;
    }

    public int getFiringCooldown() {
        return this.firingCooldown;
    }

    public Item getAmmoItem() {
        return this.ammoItem;
    }

    public SoundEvent getReloadSound() {
        return this.reloadSound;
    }

    public SoundEvent getFiringSound() {
        return this.firingSound;
    }

    public SoundEvent getEmptySound() {
        return this.emptySound;
    }

    public float getDamage() {
        return this.damage;
    }

    public int getUseDamage() {
        return this.usedamage;
    }

    public Enchantment getEnchantmentExtraDamage() {
        return this.enchantmentExtraDamage;
    }

    public static class Builder {
        private AzureGunProperties properties;

        public Builder() {
            this.properties = new AzureGunProperties();
        }

        public Builder setCanReload(int itemAmountUsed, int reloadAmount, int reloadCooldown, SoundEvent reloadSound) {
            this.properties.ammoCount = itemAmountUsed;
            this.properties.reloadAmount = reloadAmount;
            this.properties.reloadCooldown = reloadCooldown;
            this.properties.reloadSound = reloadSound;
            return this;
        }

        public Builder setFiringCoolDownTime(int firingCooldown) {
            this.properties.firingCooldown = firingCooldown;
            return this;
        }

        public Builder setDamage(float damage) {
            this.properties.damage = damage;
            return this;
        }

        public Builder setUseDamage(int useDamage) {
            this.properties.usedamage = useDamage;
            return this;
        }

        public Builder setReloadSound(SoundEvent reloadSound) {
            this.properties.reloadSound = reloadSound;
            return this;
        }

        public Builder setFiringSound(SoundEvent firingSound) {
            this.properties.firingSound = firingSound;
            return this;
        }

        public Builder setEmptySound(SoundEvent emptySound) {
            this.properties.emptySound = emptySound;
            return this;
        }

        public Builder setEnchantmentExtraDamage(@Nullable Enchantment enchantment) {
            this.properties.enchantmentExtraDamage = enchantment;
            return this;
        }

        public AzureGunProperties build() {
            return this.properties;
        }

        public AzureGunProperties copy() {
            return this.properties;
        }
    }
}
