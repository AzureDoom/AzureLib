package mod.azure.azurelib.render.item.icon;

import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.client.render.texture.TextureManager;

final class AzItemIconTexture extends DynamicTexture {

    private static final String ITEMS_SHEET = "/gui/items.png";

    private final byte[] rgba;

    private boolean filledForAnaglyph;

    AzItemIconTexture(int slot, byte[] rgba) {
        super(slot);
        this.rgba = rgba;
        fill(false);
    }

    @Override
    public void tick() {
        boolean wanted = this.anaglyph;
        if (wanted != this.filledForAnaglyph) {
            fill(wanted);
        }
    }

    @Override
    public void bind(TextureManager textureManager) {
        textureManager.bind(textureManager.load(ITEMS_SHEET));
    }

    private void fill(boolean anaglyph) {
        this.filledForAnaglyph = anaglyph;
        byte[] out = this.pixels; // 16 * 16 * 4 bytes, RGBA
        for (int i = 0; i < out.length && i + 3 < this.rgba.length; i += 4) {
            int r = this.rgba[i] & 0xFF;
            int g = this.rgba[i + 1] & 0xFF;
            int b = this.rgba[i + 2] & 0xFF;
            if (anaglyph) {
                int r2 = (r * 30 + g * 59 + b * 11) / 100;
                int g2 = (r * 30 + g * 70) / 100;
                int b2 = (r * 30 + b * 70) / 100;
                r = r2;
                g = g2;
                b = b2;
            }
            out[i] = (byte) r;
            out[i + 1] = (byte) g;
            out[i + 2] = (byte) b;
            out[i + 3] = this.rgba[i + 3];
        }
    }
}
