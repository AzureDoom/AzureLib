package mod.azure.azurelib.mixins;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;

/**
 * A Mixin class designed to integrate an animation cache mechanism into the {@link Entity} class through the use of the
 * {@link AzAnimatorAccessor} interface. This allows entities to store an instance of {@link AzAnimator} for managing
 * animations. Implements methods to set and retrieve the {@link AzAnimator} instance, enabling animation control and
 * association to the entity.
 */
@Mixin(Entity.class)
public abstract class EntityMixin_AzEntityAnimatorCache implements AzAnimatorAccessor<UUID, Entity> {

    @Unique
    @Nullable
    private AzAnimator<UUID, Entity> animator;

    @Override
    public void setAnimator(@Nullable AzAnimator<UUID, Entity> animator) {
        this.animator = animator;
    }

    @Override
    public @Nullable AzAnimator<UUID, Entity> getAnimatorOrNull() {
        return animator;
    }
}
