package mod.azure.azurelib.common.internal.common.core.molang;

import java.io.Serial;

public class MolangException extends Exception {
	@Serial
	private static final long serialVersionUID = 1470247726869768015L;

	public MolangException(String message) {
		super(message);
	}
}
