package mod.azure.azurelib.mixins;

import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;

/**
 * Mixin class that implements the {@code AzAnimatorAccessor<TileEntity>} interface to enable managing and associating
 * an {@link AzAnimator} instance with a {@link TileEntity}. This allows for caching and retrieval of the animator
 * associated with specific block entities. This mixin modifies the behavior of {@link TileEntity} by adding an animator
 * cache that can be used to store and retrieve {@link AzAnimator} instances for animation handling.
 */
@Mixin(TileEntity.class)
public abstract class BlockEntityMixin_AzBlockEntityAnimatorCache implements AzAnimatorAccessor<Long, TileEntity> {

    @Unique
    @Nullable
    private AzAnimator<Long, TileEntity> animator;

    @Override
    public void setAnimator(@Nullable AzAnimator<Long, TileEntity> animator) {
        this.animator = animator;
    }

    @Override
    public @Nullable AzAnimator<Long, TileEntity> getAnimatorOrNull() {
        return animator;
    }
}
