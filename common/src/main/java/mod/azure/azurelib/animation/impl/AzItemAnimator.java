package mod.azure.azurelib.animation.impl;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorConfig;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * The {@code AzItemAnimator} class is an abstract extension of the {@code AzAnimator} class, specifically designed to
 * handle animations for {@link ItemStack} objects. It provides common functionality and structure for animating items
 * within the framework. <br/>
 * <br/>
 * This class serves as a base for developing custom item animator implementations. Subclasses are required to implement
 * methods for animation controller registration and for specifying the animation location for the corresponding
 * {@code ItemStack}.
 */
public abstract class AzItemAnimator extends AzAnimator<UUID, ItemStack> {

    protected AzItemAnimator() {
        super();
    }

    protected AzItemAnimator(AzAnimatorConfig config) {
        super(config);
    }

    @Override
    protected void applyMolangQueries(ItemStack animatable, double animTime, float partialTicks) {
        super.applyMolangQueries(animatable, animTime, partialTicks);

        var parser = MolangParser.INSTANCE;

        parser.setMemoizedValue(
            MolangQueries.ITEM_CURRENT_DURABILITY,
            () -> animatable.getDamageValue() / (float) animatable.getMaxDamage()
        );
        parser.setMemoizedValue(
            MolangQueries.ITEM_IS_ENCHANTED,
            () -> RenderUtils.booleanToFloat(!animatable.isEnchanted())
        );
    }
}
