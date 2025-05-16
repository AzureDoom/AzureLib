package mod.azure.azurelib.mixin;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin class that implements the {@code AzAnimatorAccessor<BlockEntity>} interface to enable managing and associating
 * an {@link AzAnimator} instance with a {@link BlockEntity}. This allows for caching and retrieval of the animator
 * associated with specific block entities. This mixin modifies the behavior of {@link BlockEntity} by adding an
 * animator cache that can be used to store and retrieve {@link AzAnimator} instances for animation handling.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin_AzBlockEntityAnimatorCache implements AzAnimatorAccessor<BlockEntity> {

    @Unique
    private AzAnimator<BlockEntity> animator;

    @Override
    public void setAnimator(AzAnimator<BlockEntity> animator) {
        this.animator = animator;
    }

    @Override
    public AzAnimator<BlockEntity> getAnimatorOrNull() {
        return animator;
    }
}
