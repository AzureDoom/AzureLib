package mod.azure.azurelib.entities;

import mod.azure.azurelib.AzureLibMod.AzureEntities;
import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TimerSpellTurrentEntity extends BlockEntity implements GeoBlockEntity {
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

	// We statically instantiate our RawAnimations for efficiency, consistency, and error-proofing
	private static final RawAnimation FERTILIZER_ANIMS = RawAnimation.begin().thenLoop("key_rotation");

	public TimerSpellTurrentEntity(BlockPos pos, BlockState state) {
		super(AzureEntities.TIMER_SPELL_TURRET_ENTITY.get(), pos, state);
	}

	// Let's set our animations up
	// For this one, we want it to play the "Fertilizer" animation set if it's raining,
	// or switch to a botarium if it's not.
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, state -> {
			return state.setAndContinue(FERTILIZER_ANIMS);
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}
}
