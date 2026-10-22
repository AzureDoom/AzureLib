package mod.azure.azurelib.common.hitbox;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record AzHitboxHit(
    Entity entity,
    AzHitbox hitbox,
    int index,
    AzOrientedBox box,
    Vec3 location
) {

    public String name() {
        return hitbox.name();
    }

    public boolean hasTag(String tag) {
        return hitbox.hasTag(tag);
    }
}
