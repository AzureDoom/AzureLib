/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;

import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.client.ConfigurationClient;
import mod.azure.azurelib.config.client.widget.ConfigEntryWidget;

import static mod.azure.azurelib.config.client.screen.AbstractConfigScreen.FOOTER_HEIGHT;
import static mod.azure.azurelib.config.client.screen.AbstractConfigScreen.HEADER_HEIGHT;

@SuppressWarnings("unused")
public class ConfigGroupScreen extends Screen {

    private static final int SPACING = 25;

    protected final Screen last;

    protected final String groupId;

    protected final List<ConfigHolder<?>> configHolders;

    protected int index;

    protected int pageSize;

    public ConfigGroupScreen(Screen last, String groupId, List<ConfigHolder<?>> configHolders) {
        super(Component.translatable("text.azurelib.screen.select_config"));
        this.last = last;
        this.groupId = groupId;
        this.configHolders = configHolders;
    }

    @Override
    protected void init() {
        final var viewportMin = HEADER_HEIGHT;
        final var viewportHeight = this.height - viewportMin - FOOTER_HEIGHT;
        this.pageSize = Math.max((viewportHeight - 20) / SPACING, 1);
        this.correctScrollingIndex(this.configHolders.size());
        var errorOffset = (viewportHeight - 20) - (this.pageSize * SPACING - 5);
        var offset = 0;
        var posX = 30;
        var componentWidth = this.width - 2 * posX;
        for (var i = this.index; i < this.index + this.pageSize; i++) {
            var j = i - this.index;
            if (i >= this.configHolders.size())
                break;
            var correct = errorOffset / (this.pageSize - j);
            errorOffset -= correct;
            offset += correct;
            var holder = this.configHolders.get(i);
            var y = viewportMin + 10 + j * SPACING + offset;
            var title = ConfigurationClient.getTitle(holder);
            this.addRenderableWidget(new LeftAlignedLabel(posX, y, componentWidth, 20, title, this.font));
            this.addRenderableWidget(
                Button.builder(ConfigEntryWidget.EDIT, btn -> {
                    var screen = new ConfigScreen(holder, title, holder.getValueMap(), this);
                    this.minecraft.setScreen(screen);
                }).pos(getValueX(posX, componentWidth), y).size(getValueWidth(componentWidth), 20).build()
            );
        }
        var centerY = this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2;
        this.addRenderableWidget(
            Button.builder(ConfigEntryWidget.BACK, btn -> this.minecraft.setScreen(this.last))
                .pos(5, centerY)
                .size(120, 20)
                .build()
        );
    }

    static int getValueX(int x, int width) {
        return x + width - getValueWidth(width);
    }

    static int getValueWidth(int width) {
        return width / 3;
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
            0xFFFFFFFF
        );
        graphics.fill(0, this.height - FOOTER_HEIGHT, this.width, this.height, 0x99 << 24);
        graphics.fill(0, HEADER_HEIGHT, this.width, this.height - FOOTER_HEIGHT, 0x55 << 24);
        AbstractConfigScreen.renderScrollbar(
            graphics,
            this.width - 5,
            HEADER_HEIGHT,
            5,
            this.height - FOOTER_HEIGHT - HEADER_HEIGHT,
            this.index,
            this.configHolders.size(),
            this.pageSize,
            0xFF << 24
        );
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTicks));
    }

    protected void correctScrollingIndex(int count) {
        if (this.index + this.pageSize > count) {
            this.index = Math.max(count - this.pageSize, 0);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        var next = this.index + (int) -amountY;
        if (next >= 0 && next + this.pageSize <= this.configHolders.size()) {
            this.index = next;
            this.init(this.width, this.height);
            return true;
        }
        return false;
    }

    protected static final class LeftAlignedLabel extends AbstractWidget {

        private final Font font;

        public LeftAlignedLabel(int x, int y, int width, int height, Component label, Font font) {
            super(x, y, width, height, label);
            this.font = font;
        }

        @Override
        public void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTicks
        ) {
            graphics.text(
                this.font,
                this.getMessage(),
                this.getX(),
                this.getY() + (this.height - this.font.lineHeight) / 2,
                0xFFAAAAAA
            );
        }

        @Override
        protected boolean isValidClickButton(@NonNull MouseButtonInfo info) {
            return false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return false;
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {}
    }
}
