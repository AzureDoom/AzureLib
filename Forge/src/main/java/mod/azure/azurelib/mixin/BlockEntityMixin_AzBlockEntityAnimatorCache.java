package mod.azure.azurelib.mixin;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin class that implements the {@code AzAnimatorAccessor<TileEntity>} interface to enable managing and associating
 * an {@link AzAnimator} instance with a {@link TileEntity}. This allows for caching and retrieval of the animator
 * associated with specific block entities. This mixin modifies the behavior of {@link TileEntity} by adding an
 * animator cache that can be used to store and retrieve {@link AzAnimator} instances for animation handling.
 */
@Mixin(TileEntity.class)
public abstract class BlockEntityMixin_AzBlockEntityAnimatorCache implements AzAnimatorAccessor<TileEntity> {

    @Unique
    private AzAnimator<TileEntity> animator;

    @Override
    public void setAnimator(AzAnimator<TileEntity> animator) {
        this.animator = animator;
    }

    @Override
    public AzAnimator<TileEntity> getAnimatorOrNull() {
        return animator;
    }
}
