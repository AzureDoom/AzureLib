package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import mod.azure.azurelib.hitbox.AzHitboxState;
import mod.azure.azurelib.hitbox.AzHitboxStateAccessor;

@Mixin(Entity.class)
public abstract class EntityMixin_AzHitboxState implements AzHitboxStateAccessor {

    @Unique
    private AzHitboxState azurelib$hitboxState;

    @Override
    public AzHitboxState azurelib$getHitboxState() {
        return azurelib$hitboxState;
    }

    @Override
    public void azurelib$setHitboxState(AzHitboxState state) {
        this.azurelib$hitboxState = state;
    }
}
