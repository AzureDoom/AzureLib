package mod.azure.azurelib.common.api.common.builders;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

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

        public AzureGunProperties build() {
            return this.properties;
        }
    }
}
