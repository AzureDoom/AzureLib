/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.molang;

import java.io.Serial;

public class MolangException extends Exception {

    @Serial
    private static final long serialVersionUID = 1470247726869768015L;

    public MolangException(String message) {
        super(message);
    }
}
