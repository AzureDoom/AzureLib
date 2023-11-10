package mod.azure.azurelib.common.api.common.builders;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

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

        public Builder canReload(int itemAmountUsed, int reloadAmount, int reloadCooldown, SoundEvent reloadSound) {
            this.properties.ammoCount = itemAmountUsed;
            this.properties.reloadAmount = reloadAmount;
            this.properties.reloadCooldown = reloadCooldown;
            this.properties.reloadSound = reloadSound;
            return this;
        }

        public Builder getAmmoItemRemovalAmount(int ammoCount) {
            this.properties.ammoCount = ammoCount;
            return this;
        }

        public Builder getReloadAmount(int reloadAmount) {
            this.properties.reloadAmount = reloadAmount;
            return this;
        }

        public Builder getReloadCoolDownTime(int cooldownTime) {
            this.properties.reloadCooldown = cooldownTime;
            return this;
        }

        public Builder getFiringCoolDownTime(int firingCooldown) {
            this.properties.firingCooldown = firingCooldown;
            return this;
        }

        public Builder setAmmoItem(Item ammo) {
            this.properties.ammoItem = ammo;
            return this;
        }

        public Builder getReloadsound(SoundEvent soundEvent) {
            this.properties.reloadSound = soundEvent;
            return this;
        }

        public AzureGunProperties build() {
            return this.properties;
        }
    }
}
