package mod.azure.azurelib.core.math;

/**
 * Negative operator class This class is responsible for inverting given value
 */
public class Negative implements IValue {

    public IValue value;

    public Negative(IValue value) {
        this.value = value;
    }

    @Override
    public double get() {
        return -this.value.get();
    }

    @Override
    public IValue simplify() {
        this.value = this.value.simplify();

        return this.value instanceof Constant ? Constant.of(this.get()) : this;
    }

    @Override
    public String toString() {
        return "-" + this.value.toString();
    }
}
