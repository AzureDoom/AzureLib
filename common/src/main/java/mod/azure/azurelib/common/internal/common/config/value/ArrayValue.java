package mod.azure.azurelib.common.internal.common.config.value;

public interface ArrayValue {

    boolean isFixedSize();

    default String elementToString(Object element) {
        return element.toString();
    }
}
