package mod.azure.azurelib.client.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class ContainerWidget extends Widget implements INestedGuiEventHandler {

    private final List<IGuiEventListener> listeners = new ArrayList<>();

    private final List<Widget> widgets = new ArrayList<>();

    private IGuiEventListener focused;

    private boolean dragging;

    public ContainerWidget(int x, int y, int w, int h, ITextComponent component) {
        super(x, y, w, h, component);
    }

    public <L extends IGuiEventListener> L addGuiEventListener(L listener) {
        this.listeners.add(listener);
        return listener;
    }

    public void removeGuiEventListener(IGuiEventListener listener) {
        listeners.remove(listener);
    }

    public <W extends Widget> W addWidget(W widget) {
        widgets.add(widget);
        return addGuiEventListener(widget);
    }

    public void removeWidget(Widget widget) {
        widgets.remove(widget);
        removeGuiEventListener(widget);
    }

    public void clear() {
        listeners.clear();
        widgets.clear();
        focused = null;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        widgets.forEach(widget -> widget.render(stack, mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        return INestedGuiEventHandler.super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @Override
    public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        return INestedGuiEventHandler.super.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
    }

    @Override
    public boolean mouseDragged(
        double p_231045_1_,
        double p_231045_3_,
        int p_231045_5_,
        double p_231045_6_,
        double p_231045_8_
    ) {
        return INestedGuiEventHandler.super.mouseDragged(
            p_231045_1_,
            p_231045_3_,
            p_231045_5_,
            p_231045_6_,
            p_231045_8_
        );
    }

    @Override
    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) {
        return INestedGuiEventHandler.super.mouseScrolled(p_231043_1_, p_231043_3_, p_231043_5_);
    }

    @Override
    public void mouseMoved(double p_212927_1_, double p_212927_3_) {
        INestedGuiEventHandler.super.mouseMoved(p_212927_1_, p_212927_3_);
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        return INestedGuiEventHandler.super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

    @Override
    public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
        return INestedGuiEventHandler.super.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_);
    }

    @Override
    public List<? extends IGuiEventListener> children() {
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
    public IGuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(IGuiEventListener focused) {
        this.focused = focused;
    }
}
