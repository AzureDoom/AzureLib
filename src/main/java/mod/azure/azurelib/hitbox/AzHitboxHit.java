package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class AzHitboxHit {

    private final Entity entity;

    private final AzHitbox hitbox;

    private final int index;

    private final AzOrientedBox box;

    private final Vec3d location;

    public AzHitboxHit(Entity entity, AzHitbox hitbox, int index, AzOrientedBox box, Vec3d location) {
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

    public Vec3d location() {
        return location;
    }

    public String name() {
        return hitbox.name();
    }

    public boolean hasTag(String tag) {
        return hitbox.hasTag(tag);
    }
}
