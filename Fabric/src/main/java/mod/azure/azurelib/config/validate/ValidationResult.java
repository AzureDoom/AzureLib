package mod.azure.azurelib.config.validate;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public final class ValidationResult {

    private static final ValidationResult OK = new ValidationResult(
        NotificationSeverity.INFO,
        (MutableComponent) TextComponent.EMPTY
    );

    private final NotificationSeverity severity;

    private final MutableComponent text;

    public ValidationResult(NotificationSeverity severity, MutableComponent text) {
        this.severity = severity;
        this.text = text;
    }

    public static ValidationResult ok() {
        return OK;
    }

    public static ValidationResult warn(MutableComponent text) {
        return new ValidationResult(NotificationSeverity.WARNING, text);
    }

    public static ValidationResult error(MutableComponent text) {
        return new ValidationResult(NotificationSeverity.ERROR, text);
    }

    public boolean isOk() {
        return this.severity.isOkStatus();
    }

    public NotificationSeverity getSeverity() {
        return severity;
    }

    public MutableComponent getText() {
        return text;
    }
}
