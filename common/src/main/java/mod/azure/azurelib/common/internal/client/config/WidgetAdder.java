package mod.azure.azurelib.common.internal.client.config;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public interface WidgetAdder extends IValidationHandler {

    <W extends AbstractWidget> W addConfigWidget(ToWidgetFunction<W> function);

    Component getComponentName();

    @FunctionalInterface
    interface ToWidgetFunction<W extends AbstractWidget> {

        W asWidget(int x, int y, int width, int height, String configId);
    }
}
