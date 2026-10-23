package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public final class AzHitboxHit {

    private final Entity entity;

    private final AzHitbox hitbox;

    private final int index;

    private final AzOrientedBox box;

    private final Vec3 location;

    public AzHitboxHit(Entity entity, AzHitbox hitbox, int index, AzOrientedBox box, Vec3 location) {
        this.entity = entity;
        this.hitbox = hitbox;
        this.index = index;
        this.box = box;
        this.location = location;
    }

    public Entity entity() {
        return entity;
    }

    public AzHitbox hitbox() {
        return hitbox;
    }

    public int index() {
        return index;
    }

    public AzOrientedBox box() {
        return box;
    }

    public Vec3 location() {
        return location;
    }

    public String name() {
        return hitbox.name();
    }

    public boolean hasTag(String tag) {
        return hitbox.hasTag(tag);
    }
}
