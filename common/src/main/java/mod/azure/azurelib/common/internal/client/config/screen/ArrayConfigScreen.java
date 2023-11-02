package mod.azure.azurelib.common.internal.client.config.screen;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.client.config.DisplayAdapter;
import mod.azure.azurelib.common.internal.client.config.DisplayAdapterManager;
import mod.azure.azurelib.common.internal.client.config.widget.ConfigEntryWidget;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapters;
import mod.azure.azurelib.common.internal.common.config.validate.NotificationSeverity;
import mod.azure.azurelib.common.internal.common.config.value.ArrayValue;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class ArrayConfigScreen<V, C extends ConfigValue<V> & ArrayValue> extends AbstractConfigScreen {

	public static final Component ADD_ELEMENT = Component.translatable("text.azurelib.value.add_element");

	public final C array;
	private final boolean fixedSize;

	private Supplier<Integer> sizeSupplier = () -> 0;
	private DummyConfigValueFactory valueFactory;
	private ElementAddHandler addHandler;
	private ElementRemoveHandler<V> removeHandler;

	public ArrayConfigScreen(String ownerIdentifier, String configId, C array, Screen previous) {
		super(Component.translatable(String.format("config.%s.option.%s", configId, ownerIdentifier)), previous, configId);
		this.array = array;
		this.fixedSize = array.isFixedSize();
	}

	public void fetchSize(Supplier<Integer> integerSupplier) {
		this.sizeSupplier = integerSupplier;
	}

	public void valueFactory(DummyConfigValueFactory factory) {
		this.valueFactory = factory;
	}

	public void addElement(ElementAddHandler handler) {
		this.addHandler = handler;
	}

	public void removeElement(ElementRemoveHandler<V> handler) {
		this.removeHandler = handler;
	}

	@Override
	protected void init() {
		final int viewportMin = HEADER_HEIGHT;
		final int viewportHeight = this.height - viewportMin - FOOTER_HEIGHT;
		this.pageSize = (viewportHeight - 20) / 25;
		this.correctScrollingIndex(this.sizeSupplier.get());
		int errorOffset = (viewportHeight - 20) - (this.pageSize * 25 - 5);
		int offset = 0;

		Class<?> compType = array.get().getClass().getComponentType();
		DisplayAdapter adapter = DisplayAdapterManager.forType(compType);
		TypeAdapter.AdapterContext context = array.getSerializationContext();
		Field owner = context.getOwner();
		for (int i = this.index; i < this.index + this.pageSize; i++) {
			int j = i - this.index;
			if (i >= this.sizeSupplier.get())
				break;
			int correct = errorOffset / (this.pageSize - j);
			errorOffset -= correct;
			offset += correct;
			ConfigValue<?> dummy = valueFactory.create(array.getId(), i);
			dummy.processFieldData(owner);
			ConfigEntryWidget widget = addRenderableWidget(new ConfigEntryWidget(30, viewportMin + 10 + j * 25 + offset, this.width - 60, 20, dummy, this.configId));
			widget.setDescriptionRenderer((graphics, widget1, severity, text) -> renderEntryDescription(graphics, widget1, severity, text));
			if (adapter == null) {
				AzureLib.LOGGER.error(MARKER, "Missing display adapter for {} type, will not be displayed in GUI", compType.getSimpleName());
				continue;
			}
			try {
				adapter.placeWidgets(dummy, owner, widget);
				initializeGuiValue(dummy, widget);
			} catch (ClassCastException e) {
				AzureLib.LOGGER.error(MARKER, "Unable to create config field for {} type due to error {}", compType.getSimpleName(), e);
			}
			if (!fixedSize) {
				final int elementIndex = i;
				addRenderableWidget(Button.builder(Component.literal("x"), btn -> {
					this.removeHandler.removeElementAt(elementIndex, (index, src, dest) -> {
						System.arraycopy(src, 0, dest, 0, index);
						System.arraycopy(src, index + 1, dest, index, this.sizeSupplier.get() - 1 - index);
						return dest;
					});
					this.init(minecraft, width, height);
				}).pos(this.width - 28, widget.getY()).size(20, 20).build());
			}
		}
		addFooter();
	}

	private void renderEntryDescription(GuiGraphics graphics, AbstractWidget widget, NotificationSeverity severity, List<FormattedCharSequence> text) {
		if (!severity.isOkStatus()) {
			this.renderNotification(severity, graphics, text, widget.getX() + 5, widget.getY() + widget.getHeight() + 10);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics, mouseY, mouseY, partialTicks);
		// HEADER
		int titleWidth = this.font.width(this.title);
		graphics.drawString(this.font, this.title, (int) ((this.width - titleWidth) / 2.0F), (int) ((HEADER_HEIGHT - this.font.lineHeight) / 2.0F), 0xFFFFFF, true);
		graphics.fill(0, HEADER_HEIGHT, width, height - FOOTER_HEIGHT, 0x99 << 24);
		renderScrollbar(graphics, width - 5, HEADER_HEIGHT, 5, height - FOOTER_HEIGHT - HEADER_HEIGHT, index, sizeSupplier.get(), pageSize);
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void addFooter() {
		super.addFooter();
		if (!this.fixedSize) {
			int centerY = this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2;
			addRenderableWidget(Button.builder(ADD_ELEMENT, btn -> {
				this.addHandler.insertElement();
				this.init(minecraft, width, height);
			}).pos(width - 100, centerY).size(80, 20).build());
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount, double g) {
		int scale = (int) -amount;
		int next = this.index + scale;
		if (next >= 0 && next + this.pageSize <= this.sizeSupplier.get()) {
			this.index = next;
			this.init(minecraft, width, height);
			return true;
		}
		return false;
	}

	public static <V> TypeAdapter.AdapterContext callbackCtx(Field parent, Class<V> componentType, BiConsumer<V, Integer> callback, int index) {
		return new DummyCallbackAdapter<>(componentType, parent, callback, index);
	}

	@FunctionalInterface
	public interface ElementAddHandler {
		void insertElement();
	}

	@FunctionalInterface
	public interface DummyConfigValueFactory {
		ConfigValue<?> create(String id, int elementIndex);
	}

	@FunctionalInterface
	public interface ElementRemoveHandler<V> {
		void removeElementAt(int index, ArrayTrimmer<V> trimmer);

		@FunctionalInterface
		interface ArrayTrimmer<V> {
			V trim(int index, V src, V dest);
		}
	}

	private static class DummyCallbackAdapter<V> implements TypeAdapter.AdapterContext {

		private final TypeAdapter typeAdapter;
		private final Field parentField;
		private final BiConsumer<V, Integer> setCallback;
		private final int index;

		private DummyCallbackAdapter(Class<V> type, Field parentField, BiConsumer<V, Integer> setCallback, int index) {
			this.typeAdapter = TypeAdapters.forType(type);
			this.parentField = parentField;
			this.setCallback = setCallback;
			this.index = index;
		}

		@Override
		public TypeAdapter getAdapter() {
			return typeAdapter;
		}

		@Override
		public Field getOwner() {
			return parentField;
		}

		@Override
		public void setFieldValue(Object value) {
			this.setCallback.accept((V) value, this.index);
		}
	}
}
