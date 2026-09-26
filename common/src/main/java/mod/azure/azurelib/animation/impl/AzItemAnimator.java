package mod.azure.azurelib.animation.impl;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.DoubleSupplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorConfig;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
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

    private static final MolangVariableRef ITEM_CURRENT_DURABILITY_REF = new MolangVariableRef(
        MolangQueries.ITEM_CURRENT_DURABILITY
    );

    private static final MolangVariableRef ITEM_IS_ENCHANTED_REF = new MolangVariableRef(
        MolangQueries.ITEM_IS_ENCHANTED
    );

    /*
     * The stack currently being animated. The suppliers below are created once and read this field instead of capturing
     * the stack in new lambdas every frame; see AzEntityAnimator for the same pattern.
     */
    private ItemStack currentStack;

    private final DoubleSupplier currentDurabilitySupplier = () -> {
        int maxDamage = currentStack.getMaxDamage();

        // Non-damageable items have a max damage of 0; dividing would feed NaN into the bone transforms.
        return maxDamage <= 0 ? 0 : currentStack.getDamageValue() / (float) maxDamage;
    };

    private final DoubleSupplier isEnchantedSupplier = () -> RenderUtils.booleanToFloat(currentStack.isEnchanted());

    protected AzItemAnimator() {
        super();
    }

    protected AzItemAnimator(AzAnimatorConfig config) {
        super(config);
    }

    @Override
    protected void applyMolangQueries(ItemStack animatable, double animTime, float partialTicks) {
        super.applyMolangQueries(animatable, animTime, partialTicks);

        this.currentStack = animatable;

        ITEM_CURRENT_DURABILITY_REF.setMemoized(currentDurabilitySupplier);
        ITEM_IS_ENCHANTED_REF.setMemoized(isEnchantedSupplier);
    }
}
