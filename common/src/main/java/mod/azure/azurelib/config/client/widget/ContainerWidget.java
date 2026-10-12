/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
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
        this.listeners.remove(listener);
    }

    public <W extends AbstractWidget> W addRenderableWidget(W widget) {
        this.widgets.add(widget);
        return this.addGuiEventListener(widget);
    }

    public void removeWidget(AbstractWidget widget) {
        this.widgets.remove(widget);
        this.removeGuiEventListener(widget);
    }

    public void clear() {
        this.listeners.clear();
        this.widgets.clear();
        this.focused = null;
    }

    @Override
    public void extractWidgetRenderState(
        @NonNull GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        this.widgets.forEach(widget -> widget.extractRenderState(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean result = ContainerEventHandler.super.mouseClicked(event, doubleClick);
        if (!result && this.focused != null) {
            this.setFocused(null);
        }
        return result;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        return ContainerEventHandler.super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double dragX, double dragY) {
        return ContainerEventHandler.super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        return ContainerEventHandler.super.mouseScrolled(mouseX, mouseY, amountX, amountY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        ContainerEventHandler.super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        return ContainerEventHandler.super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(@NonNull KeyEvent event) {
        return ContainerEventHandler.super.keyReleased(event);
    }

    @Override
    public @NonNull List<? extends GuiEventListener> children() {
        return this.listeners;
    }

    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(GuiEventListener focused) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }
        if (focused != null) {
            focused.setFocused(true);
        }
        this.focused = focused;
    }
}
