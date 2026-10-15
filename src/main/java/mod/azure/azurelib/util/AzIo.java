package mod.azure.azurelib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Java 8 stand-ins for JDK 9+ stream helpers.
 */
public final class AzIo {

    private AzIo() {
        throw new UnsupportedOperationException();
    }

    /** Equivalent of {@code InputStream#readAllBytes()} (Java 9+). Does not close the stream. */
    public static byte[] readAllBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(256, stream.available()));
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
