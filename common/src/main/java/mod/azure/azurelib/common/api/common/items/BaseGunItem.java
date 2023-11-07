package mod.azure.azurelib.common.api.common.items;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.internal.common.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.common.internal.common.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.common.internal.common.core.animation.Animation.LoopType;
import mod.azure.azurelib.common.internal.common.core.animation.AnimationController;
import mod.azure.azurelib.common.internal.common.core.animation.RawAnimation;
import mod.azure.azurelib.common.internal.common.core.object.PlayState;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.common.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Base Gun Class
 * Has controller preconfigured for animation triggers for animations called firing and reload
 * Handles reload packet sending
 *
 * @author AzureDoom
 */
public abstract class BaseGunItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    /**
     * Make sure the durability is always +1 from what you a gun to use. This is make the item stops at 1 durability properly. Example: Clip size of 20 would be registered with a durability of 21.
     */
    protected BaseGunItem(Properties properties) {
        super(properties);
    }

    /**
     * Preconfigured for triggers called firing/reload animations
     *
     * @param controllers The object to register your controller instances to
     */
    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "shoot_controller", event -> PlayState.CONTINUE).triggerableAnim("firing", RawAnimation.begin().then("firing", LoopType.PLAY_ONCE)).triggerableAnim("reload", RawAnimation.begin().then("reload", LoopType.PLAY_ONCE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Item used in {@link BaseGunItem#reload(Player, InteractionHand)}
     * Defaults to AIR
     *
     * @return Item
     */
    public Item getAmmoItem() {
        return Items.AIR;
    }

    /**
     * Returns how much ammo should reload for/repair as we use durability to track ammo
     *
     * @return repair amount in int
     */
    public int getRepairAmount() {
        return 1;
    }

    /**
     * Returns how long in ticks should the item be on cooldown after reloading.
     * Usually this is how long the reload animation should take to play.
     *
     * @return cooldown ticks in int
     */
    public int getCoolDownTime() {
        return 5;
    }

    /**
     * Returns SoundEvent to be played when reloading
     *
     * @return SoundEvent
     */
    public SoundEvent getReloadsound() {
        return SoundEvents.LEVER_CLICK;
    }

    /**
     * Sends reloading packet from the client to the server when pressing {@link ClientUtils#RELOAD} keymap
     */
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (world.isClientSide && entity instanceof Player player && player.getMainHandItem().getItem() instanceof BaseGunItem)
            if (ClientUtils.RELOAD.isDown() && selected && !player.getCooldowns().isOnCooldown(stack.getItem())) {
                Services.NETWORK.reloadGun();
            }
    }

    /**
     * Handles the item repairing/removing of ammo item set in
     *
     * @param user Player who's reloading
     * @param hand Currenly only sets the {@link InteractionHand#MAIN_HAND}
     */
    public static void reload(Player user, InteractionHand hand) {
        if (user.getItemInHand(hand).getItem() instanceof BaseGunItem gunItem) {
            while (!user.isCreative() && user.getItemInHand(hand).getDamageValue() != 0 && user.getInventory().countItem(gunItem.getAmmoItem()) > 0) {
                removeAmmo(gunItem.getAmmoItem(), user);
                user.getItemInHand(hand).hurtAndBreak(-gunItem.getRepairAmount(), user, s -> user.broadcastBreakEvent(hand));
                user.getItemInHand(hand).setPopTime(gunItem.getCoolDownTime());
                user.getCommandSenderWorld().playSound((Player) null, user.getX(), user.getY(), user.getZ(), gunItem.getReloadsound(), SoundSource.PLAYERS, 1.00F, 1.0F);
            }
        }
    }

    /**
     * Removes matching item from offhand first then check inventory for item
     *
     * @param ammo         Item you want to be used as ammo
     * @param playerEntity Player whose inventory is being checked.
     */
    public static void removeAmmo(Item ammo, Player playerEntity) {
        if (!playerEntity.isCreative()) { // Creative mode reloading breaks things
            for (var item : playerEntity.getInventory().offhand) {
                if (item.getItem() == ammo) {
                    item.shrink(1); // Removes 1 of the items
                    break;
                }
                for (var item1 : playerEntity.getInventory().items) {
                    if (item1.getItem() == ammo) {
                        item1.shrink(1); // Removes 1 of the items
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes ammo item from offhand only, doesn't check inventory
     *
     * @param ammo         Item you want to be used as ammo
     * @param playerEntity Player whose inventory is being checked.
     */
    public static void removeOffHandItem(Item ammo, Player playerEntity) {
        if (!playerEntity.isCreative()) {
            for (var item : playerEntity.getInventory().offhand) {
                if (item.getItem() == ammo) {
                    item.shrink(1);
                    break;
                }
            }
        }
    }

    /**
     * Makes it so it will use the gun properly
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    /**
     * Only here so mobs can use the guns too with the bow like goal/tasks.
     */
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    /**
     * Adds Ammo amount tooltip.
     */
    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(Component.translatable("Ammo: " + (stack.getMaxDamage() - stack.getDamageValue() - 1) + " / " + (stack.getMaxDamage() - 1)).withStyle(ChatFormatting.ITALIC));
    }

}