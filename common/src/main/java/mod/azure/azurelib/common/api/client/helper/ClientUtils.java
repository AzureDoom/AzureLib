package mod.azure.azurelib.common.api.client.helper;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.azure.azurelib.common.internal.client.renderer.GeoRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

/**
 * Helper class for segregating client-side code
 */
public final class ClientUtils {

    /**
     * Translates the provided {@link PoseStack} to face towards the given {@link Entity}'s rotation.<br>
     * Usually used for rotating projectiles towards their trajectory, in an {@link GeoRenderer#preRender} override.<br>
     */
    public static void faceRotation(PoseStack poseStack, Entity animatable, float partialTick) {
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot())));
    }

    /**
     * Get the player on the client
     */
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    /**
     * Gets the current level on the client
     */
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }

    /**
     * Common reload KeyMapping for my various mods
     */
    public static KeyMapping RELOAD;

    /**
     * Common scope KeyMapping for my various mods
     */
    public static KeyMapping SCOPE;

    /**
     * Common scope KeyMapping for my various mods
     */
    public static KeyMapping FIRE_WEAPON;
}
