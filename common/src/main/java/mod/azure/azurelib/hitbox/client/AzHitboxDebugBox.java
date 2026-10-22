package mod.azure.azurelib.hitbox.client;

import org.joml.Quaternionf;

public record AzHitboxDebugBox(
    float centerX,
    float centerY,
    float centerZ,
    float halfX,
    float halfY,
    float halfZ,
    Quaternionf rotation,
    int color
) {}
