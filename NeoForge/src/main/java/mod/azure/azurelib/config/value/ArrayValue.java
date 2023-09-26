package mod.azure.azurelib.config.value;

public interface ArrayValue {

    boolean isFixedSize();

    default String elementToString(Object element) {
        return element.toString();
    }
}
