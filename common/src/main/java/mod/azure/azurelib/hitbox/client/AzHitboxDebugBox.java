package mod.azure.azurelib.hitbox.client;

import org.joml.Quaternionf;

public record AzHitboxDebugBox(
    double centerX,
    double centerY,
    double centerZ,
    float halfX,
    float halfY,
    float halfZ,
    Quaternionf rotation,
    int color
) {}
