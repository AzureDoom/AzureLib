/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

import java.util.List;
import java.util.Objects;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import mod.azure.azurelib.core.animation.EasingType;
import mod.azure.azurelib.core.math.IValue;

public class Keyframe<T> {
	private double length;
	private IValue startValue;
	private IValue endValue;
	public EasingType easingType = EasingType.LINEAR;
	public DoubleList easingArgs = new DoubleArrayList();

	public Keyframe(double length, IValue startValue, IValue endValue) {
		this.length = length;
		this.startValue = startValue;
		this.endValue = endValue;
	}

	public Keyframe(double length, IValue startValue, IValue endValue, EasingType easingType) {
		this.length = length;
		this.startValue = startValue;
		this.endValue = endValue;
		this.easingType = easingType;
	}

	public Keyframe(double length, IValue startValue, IValue endValue, EasingType easingType, List<IValue> easingArgs) {
		this.length = length;
		this.startValue = startValue;
		this.endValue = endValue;
		this.easingType = easingType;

		for (IValue easing : easingArgs) {
			this.easingArgs.add(easing.get());
		}
	}

	public double getLength() {
		return length;
	}

	public void setLength(Double length) {
		this.length = length;
	}

	public IValue getStartValue() {
		return startValue;
	}

	public void setStartValue(IValue startValue) {
		this.startValue = startValue;
	}

	public IValue getEndValue() {
		return endValue;
	}

	public void setEndValue(IValue endValue) {
		this.endValue = endValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(length, startValue, endValue);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Keyframe && hashCode() == obj.hashCode();
	}

	public DoubleList easingArgs() {
		return easingArgs;
	}

	public EasingType easingType() {
		return easingType;
	}

	public IValue startValue() {
		return startValue;
	}

	public IValue endValue() {
		return endValue;
	}

	public double length() {
		return length;
	}
}