package mod.azure.azurelib.common.animation.impl;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.DoubleSupplier;

import mod.azure.azurelib.common.animation.AzAnimator;
import mod.azure.azurelib.common.animation.AzAnimatorConfig;
import mod.azure.azurelib.common.util.client.RenderUtils;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;

/**
 * The {@code AzItemAnimator} class is an abstract extension of the {@code AzAnimator} class, specifically designed to
 * handle animations for {@link ItemStack} objects. It provides common functionality and structure for animating items
 * within the framework. <br/>
 * <br/>
 * This class serves as a base for developing custom item animator implementations. Subclasses are required to implement
 * methods for animation controller registration and for specifying the animation location for the corresponding
 * {@code ItemStack}.
 */
@SuppressWarnings("unused")
public abstract class AzItemAnimator extends AzAnimator<UUID, ItemStack> {

    private static final MolangVariableRef ITEM_CURRENT_DURABILITY_REF = new MolangVariableRef(
        MolangQueries.ITEM_CURRENT_DURABILITY
    );

    private static final MolangVariableRef ITEM_IS_ENCHANTED_REF = new MolangVariableRef(
        MolangQueries.ITEM_IS_ENCHANTED
    );

    private static final MolangVariableRef MAX_DURABILITY_REF = new MolangVariableRef(MolangQueries.MAX_DURABILITY);

    private static final MolangVariableRef REMAINING_DURABILITY_REF = new MolangVariableRef(
        MolangQueries.REMAINING_DURABILITY
    );

    private ItemStack currentStack;

    private final DoubleSupplier currentDurabilitySupplier = () -> {
        int maxDamage = currentStack.getMaxDamage();

        return maxDamage <= 0 ? 0 : currentStack.getDamageValue() / (float) maxDamage;
    };

    private final DoubleSupplier isEnchantedSupplier = () -> RenderUtils.booleanToFloat(currentStack.isEnchanted());

    private final DoubleSupplier maxDurabilitySupplier = () -> currentStack.getMaxDamage();

    private final DoubleSupplier remainingDurabilitySupplier = () -> {
        int maxDamage = currentStack.getMaxDamage();
        return maxDamage <= 0 ? 0 : maxDamage - currentStack.getDamageValue();
    };

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
        MAX_DURABILITY_REF.setMemoized(maxDurabilitySupplier);
        REMAINING_DURABILITY_REF.setMemoized(remainingDurabilitySupplier);
    }
}
