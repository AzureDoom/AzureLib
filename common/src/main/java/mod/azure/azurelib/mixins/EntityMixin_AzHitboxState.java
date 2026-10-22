package mod.azure.azurelib.mixins;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import mod.azure.azurelib.hitbox.AzHitboxState;
import mod.azure.azurelib.hitbox.AzHitboxStateAccessor;

@Mixin(Entity.class)
public abstract class EntityMixin_AzHitboxState implements AzHitboxStateAccessor {

    @Unique
    @Nullable
    private AzHitboxState azurelib$hitboxState;

    @Override
    public @Nullable AzHitboxState azurelib$getHitboxState() {
        return azurelib$hitboxState;
    }

    @Override
    public void azurelib$setHitboxState(@Nullable AzHitboxState state) {
        this.azurelib$hitboxState = state;
    }
}
