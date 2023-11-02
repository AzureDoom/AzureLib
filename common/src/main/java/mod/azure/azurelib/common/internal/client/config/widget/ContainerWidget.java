package mod.azure.azurelib.common.internal.client.config.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public abstract class ContainerWidget extends AbstractWidget implements ContainerEventHandler {

	private final List<GuiEventListener> listeners = new ArrayList<>();
	private final List<AbstractWidget> widgets = new ArrayList<>();
	private GuiEventListener focused;
	private boolean dragging;

	public ContainerWidget(int x, int y, int w, int h, Component component) {
		super(x, y, w, h, component);
	}

	public <L extends GuiEventListener> L addGuiEventListener(L listener) {
		this.listeners.add(listener);
		return listener;
	}

	public void removeGuiEventListener(GuiEventListener listener) {
		listeners.remove(listener);
	}

	public <W extends AbstractWidget> W addRenderableWidget(W widget) {
		widgets.add(widget);
		return addGuiEventListener(widget);
	}

	public void removeWidget(AbstractWidget widget) {
		widgets.remove(widget);
		removeGuiEventListener(widget);
	}

	public void clear() {
		listeners.clear();
		widgets.clear();
		focused = null;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		widgets.forEach(widget -> widget.render(graphics, mouseX, mouseY, partialTicks));
	}

	@Override
	public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
		var result = ContainerEventHandler.super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
		if (!result && this.focused != null)
			this.setFocused(null);
		return result;
	}

	@Override
	public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
		return ContainerEventHandler.super.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
	}

	@Override
	public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
		return ContainerEventHandler.super.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
	}

	@Override
	public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_, double g) {
		return ContainerEventHandler.super.mouseScrolled(p_231043_1_, p_231043_3_, p_231043_5_, g);
	}

	@Override
	public void mouseMoved(double p_212927_1_, double p_212927_3_) {
		ContainerEventHandler.super.mouseMoved(p_212927_1_, p_212927_3_);
	}

	@Override
	public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
		return ContainerEventHandler.super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
	}

	@Override
	public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
		return ContainerEventHandler.super.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return listeners;
	}

	@Override
	public boolean isDragging() {
		return dragging;
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	@Override
	public GuiEventListener getFocused() {
		return focused;
	}

	@Override
	public void setFocused(GuiEventListener focused) {
		if (this.focused != null)
			this.focused.setFocused(false);
		if (focused != null)
			focused.setFocused(true);
		this.focused = focused;
	}
}
