package mod.azure.azurelib.render.layer;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;

/**
 * Renders a skull item as a worn head, the way 1.7.10's {@code RenderBiped} does. The skull renderer's instance field
 * and render method are unnamed in some 1.7.10 mappings, so they are looked up reflectively by both names.
 */
final class AzSkullRenderHelper {

    private static boolean lookedUp;

    @Nullable
    private static Object skullRenderer;

    @Nullable
    private static Method renderSkull;

    @Nullable
    private static Method readProfile;

    private AzSkullRenderHelper() {}

    private static void lookUp() {
        if (lookedUp) {
            return;
        }

        lookedUp = true;

        try {
            Field instance = ReflectionHelper.findField(TileEntitySkullRenderer.class, "field_147536_b", "instance");
            skullRenderer = instance.get(null);
            renderSkull = ReflectionHelper.findMethod(
                TileEntitySkullRenderer.class,
                (TileEntitySkullRenderer) skullRenderer,
                new String[] { "func_152674_a", "renderSkull" },
                float.class,
                float.class,
                float.class,
                int.class,
                float.class,
                int.class,
                GameProfile.class
            );
            readProfile = ReflectionHelper.findMethod(
                NBTUtil.class,
                null,
                new String[] { "func_152459_a", "readGameProfileFromNBT" },
                NBTTagCompound.class
            );
        } catch (Exception e) {
            AzureLib.LOGGER.error("Could not find the skull renderer, skulls won't render on AzureLib models", e);
        }
    }

    static void renderSkull(ItemStack stack) {
        lookUp();

        if (skullRenderer == null || renderSkull == null) {
            return;
        }

        try {
            GameProfile profile = null;
            NBTTagCompound tag = stack.getTagCompound();

            if (tag != null) {
                if (tag.hasKey("SkullOwner", 10) && readProfile != null) {
                    profile = (GameProfile) readProfile.invoke(null, tag.getCompoundTag("SkullOwner"));
                } else if (tag.hasKey("SkullOwner", 8) && !tag.getString("SkullOwner").isEmpty()) {
                    profile = new GameProfile(null, tag.getString("SkullOwner"));
                }
            }

            // Side 1 (up), rotation 0: the caller has already moved to the head bone and faces it forward, so
            // RenderBiped's 180 would turn the skull around.
            renderSkull.invoke(skullRenderer, 0.0F, 0.0F, 0.0F, 1, 0.0F, stack.getItem().getDamage(stack), profile);
        } catch (Exception e) {
            AzureLib.LOGGER.error("Failed to render skull", e);
            skullRenderer = null;
        }
    }
}
