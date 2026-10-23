package mod.azure.azurelib.hitbox;

import javax.annotation.Nullable;

public interface AzHitboxStateAccessor {

    @Nullable
    AzHitboxState azurelib$getHitboxState();

    void azurelib$setHitboxState(@Nullable AzHitboxState state);
}
