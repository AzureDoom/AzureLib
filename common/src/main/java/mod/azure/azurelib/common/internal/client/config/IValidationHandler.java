package mod.azure.azurelib.common.internal.client.config;

import mod.azure.azurelib.common.internal.common.config.validate.ValidationResult;

public interface IValidationHandler {

    void setValidationResult(ValidationResult result);

    default void setOkStatus() {
        this.setValidationResult(ValidationResult.ok());
    }
}
