package mod.azure.azurelib.hitbox;

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
