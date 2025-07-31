/*
 * The MIT License
 *
 * Copyright (c) 2015-2022 Richard Greenlees
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package mod.azure.azurelib.rewrite.animation.easing.bedrock_easings;

public class Vector2d {

	/**
	 * The x component of the vector.
	 */
	public double x;

	/**
	 * The y component of the vector.
	 */
	public double y;

	/**
	 * Create a new {@link Vector2d} and initialize its components to zero.
	 */
	public Vector2d() {
	}

	/**
	 * Create a new {@link Vector2d} and initialize its components to the given values.
	 *
	 * @param x the x value
	 * @param y the y value
	 */
	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x component of the vector.
	 */
	public double x() {
		return this.x;
	}

	/**
	 * @return the y component of the vector.
	 */
	public double y() {
		return this.y;
	}

	/**
	 * Set the x and y components to the supplied values.
	 *
	 * @param x the x value
	 * @param y the y value
	 * @return this
	 */
	public Vector2d set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	/**
	 * Set this vector to the values of another vector.
	 *
	 * @param other the other vector to copy values from
	 * @return this
	 */
	public Vector2d set(Vector2d other) {
		this.x = other.x;
		this.y = other.y;
		return this;
	}

	/**
	 * Linearly interpolate this vector and another vector using the given interpolation factor.
	 *
	 * @param other the other vector
	 * @param t     the interpolation factor
	 * @return this
	 */
	public Vector2d lerp(Vector2d other, double t) {
		this.x += t * (other.x - this.x);
		this.y += t * (other.y - this.y);
		return this;
	}

	/**
	 * Check if the vector equals another vector.
	 *
	 * @param obj the object to compare
	 * @return true if x and y components match, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Vector2d vector2d = (Vector2d) obj;
		return Double.compare(vector2d.x, x) == 0 && Double.compare(vector2d.y, y) == 0;
	}

	/**
	 * Generate a hash code for the vector.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		long temp = Double.doubleToLongBits(x);
		int result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Generate a string representation of the vector.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString() {
		return "Vector2d{" +
			       "x=" + x +
			       ", y=" + y +
			       '}';
	}
}