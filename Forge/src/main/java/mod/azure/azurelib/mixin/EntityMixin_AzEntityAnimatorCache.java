package mod.azure.azurelib.mixin;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A Mixin class designed to integrate an animation cache mechanism into the {@link Entity} class through the use of the
 * {@link AzAnimatorAccessor} interface. This allows entities to store an instance of {@link AzAnimator} for managing
 * animations. Implements methods to set and retrieve the {@link AzAnimator} instance, enabling animation control and
 * association to the entity.
 */
@Mixin(Entity.class)
public abstract class EntityMixin_AzEntityAnimatorCache implements AzAnimatorAccessor<Entity> {

    @Unique
    private AzAnimator<Entity> animator;

    @Override
    public void setAnimator(AzAnimator<Entity> animator) {
        this.animator = animator;
    }

    @Override
    public AzAnimator<Entity> getAnimatorOrNull() {
        return animator;
    }
}
