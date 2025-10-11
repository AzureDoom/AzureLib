package mod.azure.azurelib.mixins;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;

/**
 * Mixin class that implements the {@code AzAnimatorAccessor<BlockEntity>} interface to enable managing and associating
 * an {@link AzAnimator} instance with a {@link BlockEntity}. This allows for caching and retrieval of the animator
 * associated with specific block entities. This mixin modifies the behavior of {@link BlockEntity} by adding an
 * animator cache that can be used to store and retrieve {@link AzAnimator} instances for animation handling.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin_AzBlockEntityAnimatorCache implements AzAnimatorAccessor<Long, BlockEntity> {

    @Unique
    @Nullable
    private AzAnimator<Long, BlockEntity> animator;

    @Override
    public void setAnimator(@Nullable AzAnimator<Long, BlockEntity> animator) {
        this.animator = animator;
    }

    @Override
    public @Nullable AzAnimator<Long, BlockEntity> getAnimatorOrNull() {
        return animator;
    }
}
