package mod.azure.azurelib.animation.impl;

import net.minecraft.world.level.block.entity.BlockEntity;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorConfig;

/**
 * The {@code AzBlockAnimator} class extends the functionality of the {@link AzAnimator} to provide animation support
 * specifically for {@link BlockEntity} instances. This abstract class serves as a base for creating block entity
 * animators with reusable configuration and animation controller registration mechanisms.
 *
 * @param <T> The type of {@link BlockEntity} that this animator will manage animations for.
 */
public abstract class AzBlockAnimator<T extends BlockEntity> extends AzAnimator<Long, T> {

    protected AzBlockAnimator(AzAnimatorConfig config) {
        super(config);
    }
}
