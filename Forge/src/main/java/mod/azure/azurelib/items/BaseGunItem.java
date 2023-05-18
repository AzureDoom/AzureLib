package mod.azure.azurelib.items;

import java.util.List;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public abstract class BaseGunItem extends Item implements GeoItem {

	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

	/*
	 * Make sure the durability is always +1 from what you a gun to use. This is make the item stops at 1 durablity properly. Example: Clip size of 20 would be registered with a durability of 21.
	 */
	public BaseGunItem(Properties properties) {
		super(properties);
	}

	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "shoot_controller", event -> PlayState.CONTINUE).triggerableAnim("firing", RawAnimation.begin().then("firing", LoopType.PLAY_ONCE)).triggerableAnim("reload", RawAnimation.begin().then("reload", LoopType.PLAY_ONCE)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	public void removeAmmo(Item ammo, PlayerEntity playerEntity) {
		if (!playerEntity.isCreative()) {
			for (ItemStack item : playerEntity.inventory.offhand) {
				if (item.getItem() == ammo) {
					item.shrink(1);
					break;
				}
				for (ItemStack item1 : playerEntity.inventory.items) {
					if (item1.getItem() == ammo) {
						item1.shrink(1);
						break;
					}
				}
			}
		}
	}

	public void removeOffHandItem(Item ammo, PlayerEntity playerEntity) {
		if (!playerEntity.isCreative()) {
			for (ItemStack item : playerEntity.inventory.offhand) {
				if (item.getItem() == ammo) {
					item.shrink(1);
					break;
				}
			}
		}
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getItemInHand(hand);
		user.startUsingItem(hand);
		return ActionResult.consume(itemStack);
	}

	/*
	 * Turns off the enchanted glint. Useful for Arachnids that uses enchantments for attachments.
	 */
	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.isEnchanted();
	}

	/*
	 * Only here so mobs can use the guns too with the bow like goal/tasks.
	 */
	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	/*
	 * Adds Ammo tooltip.
	 */
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag context) {
		tooltip.add(new TranslationTextComponent("Ammo: " + (stack.getMaxDamage() - stack.getDamageValue() - 1) + " / " + (stack.getMaxDamage() - 1)));
	}

	@Override
	public int getEnchantmentValue() {
		return 0;
	}

	/*
	 * Makes the item not use enchantments in the enchament table.
	 */
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	public static EntityRayTraceResult hitscanTrace(PlayerEntity player, double range, float ticks) {
		Vector3d look = player.getViewVector(ticks);
		Vector3d start = player.getEyePosition(ticks);
		Vector3d end = new Vector3d(player.getX() + look.x * range, player.getEyeY() + look.y * range, player.getZ() + look.z * range);
		double traceDistance = player.level.clip(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player)).getLocation().distanceToSqr(end);
		for (Entity possible : player.level.getEntities(player, player.getBoundingBox().expandTowards(look.scale(traceDistance)).expandTowards(3.0D, 3.0D, 3.0D), (entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity))) {
			if (possible.getBoundingBox().inflate(0.3D).clip(start, end).isPresent())
				if (start.distanceToSqr(possible.getBoundingBox().inflate(0.3D).clip(start, end).get()) < traceDistance)
					return ProjectileHelper.getEntityHitResult(player.level, player, start, end, player.getBoundingBox().expandTowards(look.scale(traceDistance)).inflate(3.0D, 3.0D, 3.0D), (target) -> !target.isSpectator() && player.isAttackable() && player.canSee(target));
		}
		return null;
	}

}