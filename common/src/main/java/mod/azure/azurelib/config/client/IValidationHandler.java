/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client;

import mod.azure.azurelib.config.validate.ValidationResult;

public interface IValidationHandler {

    void setValidationResult(ValidationResult result);

    default void setOkStatus() {
        this.setValidationResult(ValidationResult.ok());
    }
}
