package mod.azure.azurelib.common.api.common.items;

import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.api.common.helper.AzureGunTypeEnum;
import mod.azure.azurelib.common.api.common.helper.CommonUtils;
import mod.azure.azurelib.common.internal.common.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.common.internal.common.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.common.internal.common.core.animation.AnimatableManager;
import mod.azure.azurelib.common.internal.common.core.animation.Animation;
import mod.azure.azurelib.common.internal.common.core.animation.AnimationController;
import mod.azure.azurelib.common.internal.common.core.animation.RawAnimation;
import mod.azure.azurelib.common.internal.common.core.object.PlayState;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public abstract class AzureBaseGunItem extends Item implements GeoItem {
    protected final AzureGunTypeEnum azureGunTypeEnum;
    private static final String firing = "firing";
    private static final String controller = "controller";
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    public AzureBaseGunItem(AzureGunTypeEnum azureGunTypeEnum, int maxClipSize) {
        super(new Properties().stacksTo(1).durability(maxClipSize + 1));
        this.azureGunTypeEnum = azureGunTypeEnum;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public AzureGunTypeEnum getAzureGunTypeEnum() {
        return this.azureGunTypeEnum;
    }

    public Item getAmmoType() {
        return null;
    }

    public SoundEvent getReloadSound() {
        return null;
    }

    public SoundEvent getFiringSound() {
        return null;
    }

    public int getReloadAmount() {
        return 1;
    }

    public int getCoolDown() {
        return 1;
    }

    public int getReloadCoolDown() {
        return 1;
    }

    private void singleFire(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull Player player) {
        player.getCooldowns().addCooldown(this, this.getCoolDown());
        CommonUtils.spawnLightSource(player, player.level().isWaterAt(player.blockPosition()));
        itemStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
    }

    public static void shoot(Player player) {
        if (player.getMainHandItem().getDamageValue() < (player.getMainHandItem().getMaxDamage() - 1) && player.getMainHandItem().getItem() instanceof AzureBaseGunItem gunBase) {
            if (!player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()))
                gunBase.singleFire(player.getMainHandItem(), player.level(), player);
        } else {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK,
                    SoundSource.PLAYERS, 0.25F, 1.3F);
        }
    }


    /**
     * Handles the item reloading.
     *
     * @param user Player who's reloading
     * @param hand Currently only sets the {@link InteractionHand#MAIN_HAND}
     */
    public static void reload(Player user, InteractionHand hand) {
        if (user.getMainHandItem().getItem() instanceof AzureBaseGunItem gunBase) {
            while (!user.isCreative() && user.getMainHandItem().getDamageValue() != 0 && user.getInventory().countItem(
                    gunBase.getAmmoType()) > 0) {
                AzureLibUtil.removeAmmo(gunBase.getAmmoType(), user);
                user.getCooldowns().addCooldown(gunBase, gunBase.getReloadCoolDown());
                user.getMainHandItem().hurtAndBreak(-gunBase.getReloadAmount(), user,
                        s -> user.broadcastBreakEvent(hand));
                user.getMainHandItem().setPopTime(3);
                if (gunBase.getReloadSound() != null)
                    user.level().playSound(null, user.getX(), user.getY(), user.getZ(), gunBase.getReloadSound(),
                            SoundSource.PLAYERS, 1.00F, 1.0F);
                if (!user.level().isClientSide) {
                    gunBase.triggerAnim(user,
                            GeoItem.getOrAssignId(user.getItemInHand(hand), (ServerLevel) user.level()),
                            AzureBaseGunItem.controller, "reload");
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        final var itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull BlockState blockState, @NotNull BlockPos blockPos, @NotNull LivingEntity livingEntity) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level level, List<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable(
                "Ammo: " + (itemStack.getMaxDamage() - itemStack.getDamageValue() - 1) + " / " + (itemStack.getMaxDamage() - 1)).withStyle(
                ChatFormatting.ITALIC));
        super.appendHoverText(itemStack, level, tooltip, tooltipFlag);
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, AzureBaseGunItem.controller,
                event -> PlayState.CONTINUE).triggerableAnim(AzureBaseGunItem.firing,
                RawAnimation.begin().then(AzureBaseGunItem.firing, Animation.LoopType.PLAY_ONCE)).triggerableAnim(
                "reload", RawAnimation.begin().then("reload", Animation.LoopType.PLAY_ONCE)));

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
