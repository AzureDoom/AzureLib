package mod.azure.azurelib.compat.jeg;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import mod.azure.azurelib.hitbox.*;

public final class JegHitboxCompat {

    private JegHitboxCompat() {}

    public record Hit(
        Vec3 location,
        @Nullable AzHitboxHit hitbox
    ) {}

    public static @NotNull Optional<Hit> resolve(
        Entity target,
        Vec3 from,
        Vec3 to
    ) {
        if (!(target instanceof AzHitboxHolder holder)) {
            return Optional.empty();
        }

        if (!AzHitboxes.hasLivePose(target)) {
            return Optional.empty();
        }

        Optional<AzHitboxHit> boneHit = AzHitboxes.raycast(
            target,
            from,
            to,
            AzHitbox::blocksProjectiles
        );

        Optional<Vec3> vanillaHit = holder.getHitboxSet()
            .keepEntityBox()
                ? target.getBoundingBox().clip(from, to)
                : Optional.empty();

        if (boneHit.isPresent()) {
            AzHitboxHit hit = boneHit.get();

            if (
                vanillaHit.isPresent()
                    && from.distanceToSqr(vanillaHit.get()) < from.distanceToSqr(hit.location())
            ) {
                return Optional.of(
                    new Hit(vanillaHit.get(), null)
                );
            }

            return Optional.of(
                new Hit(hit.location(), hit)
            );
        }

        return vanillaHit.map(pos -> new Hit(pos, null));
    }

    public static AABB expandSearch(AABB original) {
        return original.inflate(AzHitboxes.maxOverhang());
    }

    public static void recordDamage(
        Entity projectile,
        Entity target,
        Vec3 from,
        Vec3 to,
        Vec3 actualHit
    ) {
        Optional<Hit> resolved = resolve(target, from, to);

        if (resolved == null || resolved.isEmpty()) {
            return;
        }

        AzHitboxHit hit = resolved.get().hitbox();

        if (hit == null) {
            return;
        }

        // Do not attach a bone hit to an unrelated JEG collision.
        if (hit.location().distanceToSqr(actualHit) > 0.0001D) {
            return;
        }

        if (!target.level().isClientSide()) {
            AzHitboxDamageTracker.recordProjectileHit(
                projectile,
                hit
            );
        }
    }
}
