package mod.azure.azurelib.config.client;

import mod.azure.azurelib.config.validate.ValidationResult;

public interface IValidationHandler {

    void setValidationResult(ValidationResult result);

    default void setOkStatus() {
        this.setValidationResult(ValidationResult.ok());
    }
}
