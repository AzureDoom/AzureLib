package mod.azure.azurelib.common.internal.common.config.format;

import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import mod.azure.azurelib.common.internal.common.config.value.IDescriptionProvider;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigReadException;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Handles exporting of data to custom file format
 *
 * @author Toma
 */
public interface IConfigFormat {

    void writeBoolean(String field, boolean value);

    boolean readBoolean(String field) throws ConfigValueMissingException;

    void writeChar(String field, char value);

    char readChar(String field) throws ConfigValueMissingException;

    void writeInt(String field, int value);

    int readInt(String field) throws ConfigValueMissingException;

    void writeLong(String field, long value);

    long readLong(String field) throws ConfigValueMissingException;

    void writeFloat(String field, float value);

    float readFloat(String field) throws ConfigValueMissingException;

    void writeDouble(String field, double value);

    double readDouble(String field) throws ConfigValueMissingException;

    void writeString(String field, String value);

    String readString(String field) throws ConfigValueMissingException;

    void writeBoolArray(String field, boolean[] values);

    boolean[] readBoolArray(String field) throws ConfigValueMissingException;

    void writeIntArray(String field, int[] values);

    int[] readIntArray(String field) throws ConfigValueMissingException;

    void writeLongArray(String field, long[] values);

    long[] readLongArray(String field) throws ConfigValueMissingException;

    void writeFloatArray(String field, float[] values);

    float[] readFloatArray(String field) throws ConfigValueMissingException;

    void writeDoubleArray(String field, double[] values);

    double[] readDoubleArray(String field) throws ConfigValueMissingException;

    void writeStringArray(String field, String[] values);

    String[] readStringArray(String field) throws ConfigValueMissingException;

    <E extends Enum<E>> void writeEnum(String field, E value);

    <E extends Enum<E>> E readEnum(String field, Class<E> enumClass) throws ConfigValueMissingException;

    <E extends Enum<E>> void writeEnumArray(String field, E[] value);

    <E extends Enum<E>> E[] readEnumArray(String field, Class<E> enumClass) throws ConfigValueMissingException;

    void writeMap(String field, Map<String, ConfigValue<?>> value);

    void readMap(String field, Collection<ConfigValue<?>> values) throws ConfigValueMissingException;

    void readFile(File file) throws IOException, ConfigReadException;

    void writeFile(File file) throws IOException;

    void addComments(IDescriptionProvider provider);
}
