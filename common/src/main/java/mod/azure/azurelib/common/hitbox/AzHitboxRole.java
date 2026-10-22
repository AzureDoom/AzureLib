package mod.azure.azurelib.common.hitbox;

public enum AzHitboxRole {
    COMBAT,
    INTERACT,
    ANCHOR;

    public boolean isPickable() {
        return this != ANCHOR;
    }

    public boolean blocksProjectiles() {
        return this == COMBAT;
    }
}
