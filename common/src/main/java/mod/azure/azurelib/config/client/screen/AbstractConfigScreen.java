/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.List;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.client.widget.ConfigEntryWidget;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.config.validate.NotificationSeverity;
import mod.azure.azurelib.config.value.ConfigValue;
import mod.azure.azurelib.config.value.ObjectValue;

@SuppressWarnings("unused")
public abstract class AbstractConfigScreen extends Screen implements ConfigEntryWidget.IValidationRenderer {

    public static final int HEADER_HEIGHT = 35;

    public static final int FOOTER_HEIGHT = 30;

    public static final int ROW_SPACING = 22;

    protected final ConfigHolder<?> holder;

    protected final Screen last;

    protected int index;

    protected int pageSize;

    private DeferredDescription deferredDescription;

    protected AbstractConfigScreen(Component title, Screen previous, ConfigHolder<?> configHolder) {
        super(title);
        this.holder = configHolder;
        this.last = previous;
    }

    public String getConfigId() {
        return this.holder.getConfigId();
    }

    /**
     * @return Total number of scrollable rows on this screen
     */
    protected abstract int getEntryCount();

    @Override
    public void onClose() {
        super.onClose();
        this.saveConfig(true);
    }

    public static void renderScrollbar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        int width,
        int height,
        int index,
        int valueCount,
        int paging,
        int bgColor
    ) {
        if (valueCount <= paging)
            return;
        var step = height / (double) valueCount;
        var y1 = y + Mth.floor(index * step);
        var y2 = y + Mth.ceil((index + paging) * step);
        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.fill(x, y1, x + width, y2, 0xFF888888);
        graphics.fill(x, y1, x + width - 1, y2 - 1, 0xFFEEEEEE);
        graphics.fill(x + 1, y1 + 1, x + width - 1, y2 - 1, 0xFFCCCCCC);
    }

    protected void addFooter() {
        var centerY = this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2;
        var backLabel = this.isRoot() ? CommonComponents.GUI_DONE : ConfigEntryWidget.BACK;
        this.addRenderableWidget(
            Button.builder(backLabel, btn -> this.buttonBackClicked()).pos(5, centerY).size(120, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(ConfigEntryWidget.REVERT_DEFAULTS, btn -> this.buttonRevertToDefaultClicked())
                .pos(this.width - 105, centerY)
                .size(100, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(ConfigEntryWidget.REVERT_CHANGES, btn -> this.buttonRevertChangesClicked())
                .pos(this.width - 210, centerY)
                .size(100, 20)
                .build()
        );
    }

    protected void correctScrollingIndex(int count) {
        if (this.index + this.pageSize > count) {
            this.index = Math.max(count - this.pageSize, 0);
        }
    }

    protected boolean isRoot() {
        return !(this.last instanceof AbstractConfigScreen);
    }

    private void buttonBackClicked() {
        this.minecraft.gui.setScreen(this.last);
        this.saveConfig(false);
    }

    private void buttonRevertToDefaultClicked() {
        var dialog = new DialogScreen(
            ConfigEntryWidget.REVERT_DEFAULTS,
            new Component[] { ConfigEntryWidget.REVERT_DEFAULTS_DIALOG_TEXT },
            this
        );
        dialog.onConfirmed(screen -> {
            AzureLib.LOGGER.info("Reverting config {} to default values", this.getConfigId());
            revertToDefault(this.holder.values());
            ConfigIO.saveClientValues(this.holder);
            dialog.displayPreviousScreen();
        });
        this.minecraft.gui.setScreen(dialog);
    }

    private void buttonRevertChangesClicked() {
        var dialog = new DialogScreen(
            ConfigEntryWidget.REVERT_CHANGES,
            new Component[] { ConfigEntryWidget.REVERT_CHANGES_DIALOG_TEXT },
            this
        );
        dialog.onConfirmed(screen -> {
            ConfigIO.reloadClientValues(this.holder);
            dialog.displayPreviousScreen();
        });
        this.minecraft.gui.setScreen(dialog);
    }

    private static void revertToDefault(Collection<ConfigValue<?>> configValues) {
        configValues.forEach(value -> {
            if (value instanceof ObjectValue objValue) {
                revertToDefault(objValue.get().values());
            } else {
                value.useDefaultValue();
            }
        });
    }

    private void saveConfig(boolean force) {
        if (force || this.isRoot()) {
            ConfigIO.saveClientValues(this.holder);
        }
    }

    @Override
    public void drawDescription(
        GuiGraphicsExtractor graphics,
        AbstractWidget widget,
        List<FormattedCharSequence> text,
        NotificationSeverity severity,
        int textColor
    ) {
        this.deferredDescription = new DeferredDescription(
            severity,
            text,
            textColor,
            widget.getX() + 5,
            widget.getY() + widget.getHeight() + 10
        );
    }

    @Override
    public void drawIcon(GuiGraphicsExtractor graphics, AbstractWidget widget, NotificationSeverity severity) {
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            severity.getIcon(),
            widget.getX() - 22,
            widget.getY() + 2,
            0.0F,
            0.0F,
            16,
            16,
            16,
            16
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        var titleWidth = this.font.width(this.title);
        graphics.fill(0, 0, this.width, HEADER_HEIGHT, 0x99 << 24);
        graphics.text(
            this.font,
            this.title,
            (this.width - titleWidth) / 2,
            (HEADER_HEIGHT - this.font.lineHeight) / 2,
            0xFFFFFFFF,
            true
        );
        graphics.fill(0, this.height - FOOTER_HEIGHT, this.width, this.height, 0x99 << 24);
        graphics.fill(0, HEADER_HEIGHT, this.width, this.height - FOOTER_HEIGHT, 0x55 << 24);
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTicks));
        renderScrollbar(
            graphics,
            this.width - 5,
            HEADER_HEIGHT,
            5,
            this.height - FOOTER_HEIGHT - HEADER_HEIGHT,
            this.index,
            this.getEntryCount(),
            this.pageSize,
            0xFF << 24
        );
        if (this.deferredDescription != null) {
            this.deferredDescription.render(graphics, this.font, this.width, this.height);
            this.deferredDescription = null;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        var next = this.index + (int) -amountY;
        if (next >= 0 && next + this.pageSize <= this.getEntryCount()) {
            this.index = next;
            this.init(this.width, this.height);
            return true;
        }
        return false;
    }

    public record DeferredDescription(
        NotificationSeverity severity,
        List<FormattedCharSequence> texts,
        int textColor,
        int x,
        int y
    ) {

        public void render(GuiGraphicsExtractor graphics, Font font, int width, int height) {
            if (this.texts.isEmpty())
                return;
            var maxTextWidth = 0;
            for (FormattedCharSequence line : this.texts) {
                maxTextWidth = Math.max(maxTextWidth, font.width(line));
            }
            var startX = this.x + 12;
            var startY = this.y - 12;
            var heightOffset = 8;
            if (this.texts.size() > 1) {
                heightOffset += 2 + (this.texts.size() - 1) * 10;
            }
            if (startX + maxTextWidth > width) {
                startX -= 28 + maxTextWidth;
            }
            if (startY + heightOffset + 6 > height) {
                startY = height - heightOffset - 6;
            }

            var background = this.severity.background;
            var fadeMin = this.severity.fadeMin;
            var fadeMax = this.severity.fadeMax;
            var right = startX + maxTextWidth;
            var bottom = startY + heightOffset;
            graphics.nextStratum();
            graphics.fill(startX - 3, startY - 4, right + 3, startY - 3, background);
            graphics.fill(startX - 3, bottom + 3, right + 3, bottom + 4, background);
            graphics.fill(startX - 3, startY - 3, right + 3, bottom + 3, background);
            graphics.fill(startX - 4, startY - 3, startX - 3, bottom + 3, background);
            graphics.fill(right + 3, startY - 3, right + 4, bottom + 3, background);
            graphics.fillGradient(startX - 3, startY - 2, startX - 2, bottom + 2, fadeMin, fadeMax);
            graphics.fillGradient(right + 2, startY - 2, right + 3, bottom + 2, fadeMin, fadeMax);
            graphics.fill(startX - 3, startY - 3, right + 3, startY - 2, fadeMin);
            graphics.fill(startX - 3, bottom + 2, right + 3, bottom + 3, fadeMax);

            for (var i = 0; i < this.texts.size(); i++) {
                graphics.text(font, this.texts.get(i), startX, startY, this.textColor, false);
                if (i == 0) {
                    startY += 2;
                }
                startY += 10;
            }
        }
    }
}
