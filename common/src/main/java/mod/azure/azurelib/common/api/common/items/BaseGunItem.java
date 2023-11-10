package mod.azure.azurelib.common.api.common.items;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.api.common.builders.AzureGunProperties;
import mod.azure.azurelib.common.api.common.helper.CommonUtils;
import mod.azure.azurelib.common.internal.common.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.common.internal.common.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.common.internal.common.core.animation.Animation.LoopType;
import mod.azure.azurelib.common.internal.common.core.animation.AnimationController;
import mod.azure.azurelib.common.internal.common.core.animation.RawAnimation;
import mod.azure.azurelib.common.internal.common.core.object.PlayState;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    public AzureGunProperties gunBuilder = new AzureGunProperties.Builder().canReload(1, 1, 5, SoundEvents.LEVER_CLICK).getFiringCoolDownTime(5).build();

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
     * Sends reloading packet from the client to the server when pressing {@link ClientUtils#RELOAD} keymap
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        CommonUtils.sendReloadPacket(stack, level, entity, selected);
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