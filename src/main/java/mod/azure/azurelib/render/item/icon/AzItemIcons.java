package mod.azure.azurelib.render.item.icon;

import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.item.Item;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.resource.AzResourceManager;
import mod.azure.azurelib.util.ResourceLocation;

public final class AzItemIcons {

    private static final int SLOTS = 256;

    private static final int FIRST_REAL_ITEM_ID = 256;

    private static final int DAMAGE_VALUES_CHECKED = 16;

    private static final Map<Item, ResourceLocation> PENDING = new LinkedHashMap<>();

    private static final Set<ResourceLocation> RESERVED = new LinkedHashSet<>();

    private static final Map<ResourceLocation, Integer> SLOT_BY_TEXTURE = new HashMap<>();

    private static final Deque<Integer> FREE_SLOTS = new ArrayDeque<>();

    @Nullable
    private static TextureManager textureManager;

    private AzItemIcons() {
        throw new UnsupportedOperationException();
    }

    public static synchronized void register(Item item, ResourceLocation texture) {
        if (item == null || texture == null) {
            throw new IllegalArgumentException("Item and texture are required");
        }
        if (textureManager == null) {
            PENDING.put(item, texture);
        } else {
            apply(item, texture);
        }
    }

    public static synchronized int getSlot(ResourceLocation texture) {
        Integer slot = SLOT_BY_TEXTURE.get(texture);
        return slot == null ? -1 : slot;
    }

    public static synchronized void reserve(ResourceLocation texture) {
        if (texture == null) {
            throw new IllegalArgumentException("Texture is required");
        }
        if (textureManager == null) {
            RESERVED.add(texture);
        } else {
            slotFor(texture);
        }
    }

    public static synchronized void onTextureManagerCreated(TextureManager manager) {
        textureManager = manager;
        findFreeSlots();
        for (ResourceLocation texture : RESERVED) {
            slotFor(texture);
        }
        RESERVED.clear();
        for (Map.Entry<Item, ResourceLocation> entry : PENDING.entrySet()) {
            apply(entry.getKey(), entry.getValue());
        }
        PENDING.clear();
        if (!SLOT_BY_TEXTURE.isEmpty()) {
            AzureLib.LOGGER.info(
                "Installed {} custom item icon(s); {} free items.png slot(s) left",
                SLOT_BY_TEXTURE.size(),
                FREE_SLOTS.size()
            );
        }
    }

    private static void apply(Item item, ResourceLocation texture) {
        int slot = slotFor(texture);
        if (slot >= 0) {
            McClient.setItemSprite(item, slot);
        }
    }

    private static int slotFor(ResourceLocation texture) {
        Integer existing = SLOT_BY_TEXTURE.get(texture);
        if (existing != null) {
            return existing;
        }
        byte[] rgba = loadIcon(texture);
        if (rgba == null) {
            return -1;
        }
        Integer slot = FREE_SLOTS.pollFirst();
        if (slot == null) {
            AzureLib.LOGGER.error("No free items.png slot left for icon {}; the item keeps its old icon", texture);
            return -1;
        }
        McClient.addDynamicTexture(textureManager, new AzItemIconTexture(slot, rgba));
        SLOT_BY_TEXTURE.put(texture, slot);
        return slot;
    }

    private static void findFreeSlots() {
        FREE_SLOTS.clear();
        Set<Integer> used = new HashSet<>();
        Item[] items = McClient.itemsById();
        for (int id = FIRST_REAL_ITEM_ID; id < items.length; id++) {
            Item item = items[id];
            if (item == null) {
                continue;
            }
            for (int damage = 0; damage < DAMAGE_VALUES_CHECKED; damage++) {
                try {
                    used.add(McClient.itemSprite(item, damage));
                } catch (RuntimeException ignored) {}
            }
        }

        BufferedImage sheet = loadDefaultItemsSheet();
        for (int slot = 0; slot < SLOTS; slot++) {
            if (!used.contains(slot) && (sheet == null || isTransparentTile(sheet, slot))) {
                FREE_SLOTS.addLast(slot);
            }
        }
    }

    @Nullable
    private static BufferedImage loadDefaultItemsSheet() {
        try (InputStream in = net.minecraft.client.Minecraft.class.getResourceAsStream("/gui/items.png")) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (IOException e) {
            AzureLib.LOGGER.warn("Couldn't read /gui/items.png", e);
        }
        AzureLib.LOGGER.warn("Couldn't read /gui/items.png; only item sprite usage decides which slots are free");
        return null;
    }

    private static boolean isTransparentTile(BufferedImage sheet, int slot) {
        int tile = sheet.getWidth() / 16;
        int x0 = (slot % 16) * tile;
        int y0 = (slot / 16) * tile;
        for (int y = y0; y < y0 + tile && y < sheet.getHeight(); y++) {
            for (int x = x0; x < x0 + tile && x < sheet.getWidth(); x++) {
                if ((sheet.getRGB(x, y) >>> 24) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    private static byte[] loadIcon(ResourceLocation texture) {
        BufferedImage image;
        try (InputStream in = AzResourceManager.getInstance().getResource(texture).getInputStream()) {
            image = ImageIO.read(in);
        } catch (IOException e) {
            AzureLib.LOGGER.error("Couldn't load item icon {}", texture, e);
            return null;
        }
        if (image == null) {
            AzureLib.LOGGER.error("Item icon {} isn't a readable image", texture);
            return null;
        }
        if (image.getWidth() != 16 || image.getHeight() != 16) {
            BufferedImage scaled = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
            );
            graphics.drawImage(image, 0, 0, 16, 16, null);
            graphics.dispose();
            image = scaled;
        }
        byte[] rgba = new byte[16 * 16 * 4];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int argb = image.getRGB(x, y);
                int i = (y * 16 + x) * 4;
                rgba[i] = (byte) (argb >> 16);
                rgba[i + 1] = (byte) (argb >> 8);
                rgba[i + 2] = (byte) argb;
                rgba[i + 3] = (byte) (argb >>> 24);
            }
        }
        return rgba;
    }
}
