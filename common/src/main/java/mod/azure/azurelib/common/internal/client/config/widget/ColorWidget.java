package mod.azure.azurelib.common.internal.client.config.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.client.config.screen.DialogScreen;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ColorWidget extends AbstractWidget {

	public static final Component SELECT_COLOR = Component.translatable("text.azurelib.screen.color_dialog");
	private final boolean argb;
	private final String colorPrefix;
	private final IntSupplier colorSupplier;
	private final GetSet<String> colorWidget;
	private final Screen lastScreen;

	public ColorWidget(int x, int y, int width, int height, Configurable.Gui.ColorValue colorOptions, GetSet<String> colorWidget, Screen lastScreen) {
		super(x, y, width, height, CommonComponents.EMPTY);
		this.argb = colorOptions.isARGB();
		this.colorPrefix = colorOptions.getGuiColorPrefix();
		this.colorWidget = colorWidget;
		this.colorSupplier = () -> {
			String rawColor = colorWidget.get();
			try {
				long longClr = Long.decode(rawColor);
				return (int) longClr;
			} catch (NumberFormatException e) {
				return 0;
			}
		};
		this.lastScreen = lastScreen;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialRenderTicks) {
		int borderColor = this.isFocused() ? 0xffffffff : 0xffa0a0a0;
		int providedColor = this.colorSupplier.getAsInt();
		int color = this.argb ? providedColor : (0xFF << 24) | providedColor;
		graphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, borderColor);
		graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFFFFFFFF, 0xFF888888);
		graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color);
	}

	@Override
	protected boolean isValidClickButton(int button) {
		return button == 0;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		ColorSelectorDialog dialog = new ColorSelectorDialog(SELECT_COLOR, this.lastScreen, this.argb, this.colorSupplier);
		dialog.onConfirmed(screen -> {
			int color = dialog.getResultColor();
			String colorText = this.colorPrefix + Integer.toHexString(color).toUpperCase();
			this.colorWidget.set(colorText);
			dialog.displayPreviousScreen(dialog);
		});
		Minecraft.getInstance().setScreen(dialog);
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput elementOutput) {
	}

	public interface GetSet<T> {

		T get();

		void set(T t);

		static <T> GetSet<T> of(Supplier<T> get, Consumer<T> set) {
			return new GetSet<T>() {
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

		public ColorSelectorDialog(Component title, Screen background, boolean allowTransparency, IntSupplier colorProvider) {
			super(title, new Component[0], background);
			this.argb = allowTransparency;
			this.colorProvider = colorProvider;
		}

		@Override
		protected void init() {
			this.sliders.clear();
			int width = 190;
			int height = 120;
			int rightMargin = 85;
			if (this.argb) {
				height = 150;
				rightMargin = 110;
				width = 230;
			}
			super.init();
			this.setDimensions(width, height);
			int color = this.colorProvider.getAsInt();
			this.sliders.add(this.addRenderableWidget(new ColorSlider(dialogLeft + 5, dialogTop + 20, dialogWidth - rightMargin, 20, color, ColorComponent.RED)));
			this.sliders.add(this.addRenderableWidget(new ColorSlider(dialogLeft + 5, dialogTop + 45, dialogWidth - rightMargin, 20, color, ColorComponent.GREEN)));
			this.sliders.add(this.addRenderableWidget(new ColorSlider(dialogLeft + 5, dialogTop + 70, dialogWidth - rightMargin, 20, color, ColorComponent.BLUE)));
			if (this.argb) {
				this.sliders.add(this.addRenderableWidget(new ColorSlider(dialogLeft + 5, dialogTop + 95, dialogWidth - rightMargin, 20, color, ColorComponent.ALPHA)));
			}
			this.addRenderableWidget(new ColorDisplay(dialogLeft + 5 + dialogWidth - rightMargin + 5, dialogTop + 20, rightMargin - 15, rightMargin - 15, argb, this::getResultColor));
			super.addDefaultDialogButtons();
		}

		@Override
		protected void addDefaultDialogButtons() {
		}

		public int getResultColor() {
			int color = 0;
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
			public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
				int color = this.colorProvider.getAsInt();
				if (!this.argb) {
					color |= 0xFF << 24;
				}
				int borderColor = 0xffa0a0a0;
				graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);
				graphics.fillGradient(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFFFFFFFF, 0xFF888888);
				graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, color);
			}

			@Override
			protected boolean isValidClickButton(int button) {
				return false;
			}

			@Override
			public void updateWidgetNarration(NarrationElementOutput p_169152_) {
			}
		}

		private static final class ColorSlider extends AbstractSliderButton {

			private final ColorComponent colorComponent;

			public ColorSlider(int x, int y, int width, int height, int color, ColorComponent colorComponent) {
				super(x, y, width, height, CommonComponents.EMPTY, (colorComponent.getByteColor(color) / 255.0D));
				this.colorComponent = colorComponent;
				this.updateMessage();
			}

			@Override
			protected void updateMessage() {
				Component colorLabel = this.colorComponent.updateTitle(this.value);
				this.setMessage(colorLabel);
			}

			@Override
			protected void applyValue() {
			}

			int getColor() {
				return this.colorComponent.getOffsetColor((int) (0xFF * this.value));
			}
		}

		private enum ColorComponent {

			ALPHA(24), RED(16), GREEN(8), BLUE(0);

			private final int bitOffset;
			private final Function<Double, Component> title;

			ColorComponent(int bitOffset) {
				this.bitOffset = bitOffset;
				this.title = val -> {
					String name = this.name().toLowerCase();
					String translate = "text.azurelib.screen.color." + name;
					int colorValue = (int) (val * 255);
					return Component.translatable(translate, colorValue);
				};
			}

			public int getOffsetColor(int value) {
				return value << bitOffset;
			}

			public int getByteColor(int value) {
				return (value >> bitOffset) & 0xFF;
			}

			public Component updateTitle(double sliderValue) {
				return this.title.apply(sliderValue);
			}
		}
	}
}
