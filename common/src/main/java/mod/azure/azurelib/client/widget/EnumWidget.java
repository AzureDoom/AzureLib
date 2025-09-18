package mod.azure.azurelib.client.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.config.value.EnumValue;

public class EnumWidget<E extends Enum<E>> extends AbstractWidget {

    private final EnumValue<E> value;

    public EnumWidget(int x, int y, int w, int h, EnumValue<E> value) {
        super(x, y, w, h, TextComponent.EMPTY);
        this.value = value;
        this.updateText();
    }

    @Override
    public void onClick(double p_230982_1_, double p_230982_3_) {
        this.nextValue();
        this.updateText();
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    private void nextValue() {
        E e = this.value.get();
        E[] values = e.getDeclaringClass().getEnumConstants();
        int i = e.ordinal();
        int j = (i + 1) % values.length;
        E next = values[j];
        this.value.set(next);
    }

    private void updateText() {
        E e = this.value.get();
        this.setMessage(new TextComponent(e.name()));
    }
}
