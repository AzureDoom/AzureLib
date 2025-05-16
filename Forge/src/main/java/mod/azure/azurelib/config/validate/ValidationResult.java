package mod.azure.azurelib.config.validate;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

public final class ValidationResult {

    private static final ValidationResult OK = new ValidationResult(
        NotificationSeverity.INFO,
        (IFormattableTextComponent) StringTextComponent.EMPTY
    );

    private final NotificationSeverity severity;

    private final IFormattableTextComponent text;

    public ValidationResult(NotificationSeverity severity, IFormattableTextComponent text) {
        this.severity = severity;
        this.text = text;
    }

    public static ValidationResult ok() {
        return OK;
    }

    public static ValidationResult warn(IFormattableTextComponent text) {
        return new ValidationResult(NotificationSeverity.WARNING, text);
    }

    public static ValidationResult error(IFormattableTextComponent text) {
        return new ValidationResult(NotificationSeverity.ERROR, text);
    }

    public boolean isOk() {
        return this.severity.isOkStatus();
    }

    public NotificationSeverity getSeverity() {
        return severity;
    }

    public IFormattableTextComponent getText() {
        return text;
    }
}
