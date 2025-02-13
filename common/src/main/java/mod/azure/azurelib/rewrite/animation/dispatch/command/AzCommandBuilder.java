package mod.azure.azurelib.rewrite.animation.dispatch.command;

import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;

import java.util.ArrayList;
import java.util.List;

public abstract class AzCommandBuilder {

    protected final List<AzAction> actions;

    protected AzCommandBuilder() {
        this.actions = new ArrayList<>();
    }

    public AzCommand build() {
        return new AzCommand(actions);
    }
}
