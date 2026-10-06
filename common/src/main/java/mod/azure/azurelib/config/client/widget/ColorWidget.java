/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import mod.azure.azurelib.config.Configurable;
import mod.azure.azurelib.config.client.screen.DialogScreen;

/**
 * Color preview swatch for {@link Configurable.Gui.ColorValue} string fields. Clicking it opens an RGB(A) slider dialog
 * which writes back into the paired edit box.
 */
public final class ColorWidget extends AbstractWidget {

    public static final Component SELECT_COLOR = Component.translatable("text.azurelib.screen.color_dialog");

    private final boolean argb;

    private final String colorPrefix;

    private final IntSupplier colorSupplier;

    private final GetSet<String> colorText;

    private final Screen lastScreen;

    public ColorWidget(
        int x,
        int y,
        int width,
        int height,
        Configurable.Gui.ColorValue colorOptions,
        GetSet<String> colorText,
        Screen lastScreen
    ) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.argb = colorOptions.isARGB();
        this.colorPrefix = colorOptions.getGuiColorPrefix();
        this.colorText = colorText;
        this.colorSupplier = () -> parseColor(colorText.get(), this.colorPrefix);
        this.lastScreen = lastScreen;
    }

    /**
     * Accepts {@code #RRGGBB}, {@code 0xRRGGBB} or any custom prefix declared through
     * {@link Configurable.Gui.ColorValue#getGuiColorPrefix()}.
     */
    static int parseColor(String raw, String prefix) {
        if (raw == null)
            return 0;
        var text = raw.trim();
        try {
            if (!prefix.isEmpty() && text.startsWith(prefix)) {
                return (int) Long.parseLong(text.substring(prefix.length()), 16);
            }
            return (int) (long) Long.decode(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        var providedColor = this.colorSupplier.getAsInt();
        var color = this.argb ? providedColor : (0xFF << 24) | providedColor;
        var border = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFA0A0A0;
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), border);
        graphics.fillGradient(
            this.getX() + 1,
            this.getY() + 1,
            this.getRight() - 1,
            this.getBottom() - 1,
            0xFFFFFFFF,
            0xFF888888
        );
        graphics.fill(this.getX() + 1, this.getY() + 1, this.getRight() - 1, this.getBottom() - 1, color);
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo info) {
        return info.button() == InputConstants.MOUSE_BUTTON_LEFT;
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
        var dialog = new ColorSelectorDialog(
            SELECT_COLOR,
            this.lastScreen,
            this.argb,
            this.colorSupplier
        );
        dialog.onConfirmed(_ -> {
            var color = dialog.getResultColor();
            var hex = this.argb
                ? String.format(Locale.ROOT, "%08X", color)
                : String.format(Locale.ROOT, "%06X", color & 0xFFFFFF);
            this.colorText.set(this.colorPrefix + hex);
            dialog.displayPreviousScreen();
        });
        Minecraft.getInstance().gui.setScreen(dialog);
    }

    @Override
    public void updateWidgetNarration(@NonNull NarrationElementOutput output) {}

    public interface GetSet<T> {

        T get();

        void set(T t);

        static <T> GetSet<T> of(Supplier<T> get, Consumer<T> set) {
            return new GetSet<>() {

                @Override
                public T get() {
                    return get.get();
                }

                @Override
                public void set(T t) {
                    set.accept(t);
                }
            };
        }
    }

    private static final class ColorSelectorDialog extends DialogScreen {

        private final boolean argb;

        private final IntSupplier colorProvider;

        private final List<ColorSlider> sliders = new ArrayList<>();

        public ColorSelectorDialog(
            Component title,
            Screen background,
            boolean allowTransparency,
            IntSupplier colorProvider
        ) {
            super(title, new Component[0], background);
            this.argb = allowTransparency;
            this.colorProvider = colorProvider;
        }

        @Override
        protected void init() {
            this.sliders.clear();
            var width = 190;
            var height = 120;
            var rightMargin = 85;
            if (this.argb) {
                height = 150;
                rightMargin = 110;
                width = 230;
            }
            super.init();
            this.setDimensions(width, height);
            var color = this.colorProvider.getAsInt();
            var sliderWidth = this.dialogWidth - rightMargin;
            var components = this.argb
                ? new ColorComponent[] {
                    ColorComponent.RED,
                    ColorComponent.GREEN,
                    ColorComponent.BLUE,
                    ColorComponent.ALPHA }
                : new ColorComponent[] { ColorComponent.RED, ColorComponent.GREEN, ColorComponent.BLUE };
            for (var i = 0; i < components.length; i++) {
                this.sliders.add(
                    this.addRenderableWidget(
                        new ColorSlider(dialogLeft + 5, dialogTop + 20 + i * 25, sliderWidth, 20, color, components[i])
                    )
                );
            }
            this.addRenderableWidget(
                new ColorDisplay(
                    dialogLeft + 10 + sliderWidth,
                    dialogTop + 20,
                    rightMargin - 15,
                    rightMargin - 15,
                    this.argb,
                    this::getResultColor
                )
            );
            super.addDefaultDialogButtons();
        }

        @Override
        protected void addDefaultDialogButtons() {
            // added manually after the sliders in init()
        }

        public int getResultColor() {
            var color = 0;
            for (ColorSlider slider : this.sliders) {
                color |= slider.getColor();
            }
            return color;
        }

        private static final class ColorDisplay extends AbstractWidget {

            private final boolean argb;

            private final IntSupplier colorProvider;

            public ColorDisplay(int x, int y, int width, int height, boolean argb, IntSupplier colorProvider) {
                super(x, y, width, height, CommonComponents.EMPTY);
                this.argb = argb;
                this.colorProvider = colorProvider;
            }

            @Override
            public void extractWidgetRenderState(
                @NonNull GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float partialTicks
            ) {
                var color = this.colorProvider.getAsInt();
                if (!this.argb) {
                    color |= 0xFF << 24;
                }
                graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0xFFA0A0A0);
                graphics.fillGradient(
                    this.getX() + 1,
                    this.getY() + 1,
                    this.getRight() - 1,
                    this.getBottom() - 1,
                    0xFFFFFFFF,
                    0xFF888888
                );
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getRight() - 1, this.getBottom() - 1, color);
            }

            @Override
            protected boolean isValidClickButton(@NonNull MouseButtonInfo info) {
                return false;
            }

            @Override
            public void updateWidgetNarration(@NonNull NarrationElementOutput output) {}
        }

        private static final class ColorSlider extends AbstractSliderButton {

            private final ColorComponent colorComponent;

            public ColorSlider(int x, int y, int width, int height, int color, ColorComponent colorComponent) {
                super(x, y, width, height, CommonComponents.EMPTY, colorComponent.getByteColor(color) / 255.0D);
                this.colorComponent = colorComponent;
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(this.colorComponent.updateTitle(this.value));
            }

            @Override
            protected void applyValue() {}

            int getColor() {
                return this.colorComponent.getOffsetColor((int) Math.round(0xFF * this.value));
            }
        }

        private enum ColorComponent {

            ALPHA(24),
            RED(16),
            GREEN(8),
            BLUE(0);

            private final int bitOffset;

            ColorComponent(int bitOffset) {
                this.bitOffset = bitOffset;
            }

            public int getOffsetColor(int value) {
                return (value & 0xFF) << this.bitOffset;
            }

            public int getByteColor(int value) {
                return (value >> this.bitOffset) & 0xFF;
            }

            public Component updateTitle(double sliderValue) {
                var key = "text.azurelib.screen.color." + this.name().toLowerCase(Locale.ROOT);
                return Component.translatable(key, (int) Math.round(sliderValue * 255));
            }
        }
    }
}
