package mod.azure.azurelib.common.hitbox;

import org.jetbrains.annotations.Nullable;

public interface AzHitboxStateAccessor {

    @Nullable
    AzHitboxState azurelib$getHitboxState();

    void azurelib$setHitboxState(@Nullable AzHitboxState state);
}
