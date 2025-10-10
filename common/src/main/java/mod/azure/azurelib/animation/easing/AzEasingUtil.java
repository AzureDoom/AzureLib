package mod.azure.azurelib.animation.easing;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;

import mod.azure.azurelib.animation.controller.keyframe.AzAnimationPoint;

public class AzEasingUtil {

    /**
     * Returns an easing function running linearly. Functionally equivalent to no easing
     */
    public static Double2DoubleFunction linear(Double2DoubleFunction function) {
        return function;
    }

    /**
     * Performs a Catmull-Rom interpolation, used to get smooth interpolated motion between keyframes.<br>
     * <a href="https://pub.dev/documentation/latlong2/latest/spline/CatmullRom-class.html">CatmullRom#position</a>
     *
     * @param delta The interpolation parameter (between 0 and 1)
     * @param p0    First control point (anchor)
     * @param p1    Second control point (start point)
     * @param p2    Third control point (end point)
     * @param p3    Fourth control point (anchor)
     * @return The interpolated value
     */
    public static double catmullRom(double delta, double p0, double p1, double p2, double p3) {
        return 0.5d * (2d * p1 + (p2 - p0) * delta +
            (2d * p0 - 5d * p1 + 4d * p2 - p3) * delta * delta +
            (3d * p1 - p0 - 3d * p2 + p3) * delta * delta * delta);
    }

    /**
     * Simplified Catmull-Rom interpolation for single parameter
     *
     * @param n The interpolation parameter
     * @return The interpolated value
     */
    public static double catmullRom(double n) {// Using default control points for simple interpolation
        return catmullRom(n, 0, 0, 1, 1);
    }

    /**
     * Returns an easing function running forward in time
     */
    public static Double2DoubleFunction easeIn(Double2DoubleFunction function) {
        return function;
    }

    // ---> Easing Transition Type Functions <--- //

    /**
     * Returns an easing function running backwards in time
     */
    public static Double2DoubleFunction easeOut(Double2DoubleFunction function) {
        return time -> 1 - function.apply(1 - time);
    }

    /**
     * Returns an easing function that runs equally both forwards and backwards in time based on the halfway point,
     * generating a symmetrical curve.<br>
     */
    public static Double2DoubleFunction easeInOut(Double2DoubleFunction function) {
        return time -> {
            if (time < 0.5d)
                return function.apply(time * 2d) / 2d;

            return 1 - function.apply((1 - time) * 2d) / 2d;
        };
    }

    /**
     * Returns a stepping function that returns 1 for any input value greater than 0, or otherwise returning 0
     */
    public static Double2DoubleFunction stepPositive(Double2DoubleFunction function) {
        return n -> n > 0 ? 1 : 0;
    }

    /**
     * Returns a stepping function that returns 1 for any input value greater than or equal to 0, or otherwise returning
     * 0
     */
    public static Double2DoubleFunction stepNonNegative(Double2DoubleFunction function) {
        return n -> n >= 0 ? 1 : 0;
    }

    /**
     * A linear function, equivalent to a null-operation.<br>
     * {@code f(n) = n}
     */
    public static double linear(double n) {
        return n;
    }

    // ---> Stepping Functions <--- //

    /**
     * A quadratic function, equivalent to the square (<i>n</i>^2) of elapsed time.<br>
     * {@code f(n) = n^2}<br>
     * <a href="http://easings.net/#easeInQuad">Easings.net#easeInQuad</a>
     */
    public static double quadratic(double n) {
        return n * n;
    }

    /**
     * A cubic function, equivalent to cube (<i>n</i>^3) of elapsed time.<br>
     * {@code f(n) = n^3}<br>
     * <a href="http://easings.net/#easeInCubic">Easings.net#easeInCubic</a>
     */
    public static double cubic(double n) {
        return n * n * n;
    }

    // ---> Mathematical Functions <--- //

    /**
     * A sinusoidal function, equivalent to a sine curve output.<br>
     * {@code f(n) = 1 - cos(n * π / 2)}<br>
     * <a href="http://easings.net/#easeInSine">Easings.net#easeInSine</a>
     */
    public static double sine(double n) {
        return 1 - Math.cos(n * Math.PI / 2f);
    }

    /**
     * A circular function, equivalent to a normally symmetrical curve.<br>
     * {@code f(n) = 1 - sqrt(1 - n^2)}<br>
     * <a href="http://easings.net/#easeInCirc">Easings.net#easeInCirc</a>
     */
    public static double circle(double n) {
        return 1 - Math.sqrt(1 - n * n);
    }

    /**
     * An exponential function, equivalent to an exponential curve.<br>
     * {@code f(n) = 2^(10 * (n - 1))}<br>
     * <a href="http://easings.net/#easeInExpo">Easings.net#easeInExpo</a>
     */
    public static double exp(double n) {
        return Math.pow(2, 10 * (n - 1));
    }

    /**
     * An elastic function, equivalent to an oscillating curve.<br>
     * <i>n</i> defines the elasticity of the output.<br>
     * {@code f(t) = 1 - (cos(t * π) / 2))^3 * cos(t * n * π)}<br>
     * <a href="http://easings.net/#easeInElastic">Easings.net#easeInElastic</a>
     */
    public static Double2DoubleFunction elastic(Double n) {
        double n2 = n == null ? 1 : n;

        return t -> 1 - Math.pow(Math.cos(t * Math.PI / 2f), 3) * Math.cos(t * n2 * Math.PI);
    }

    /**
     * A bouncing function, equivalent to a bouncing ball curve.<br>
     * <i>n</i> defines the bounciness of the output.<br>
     * Thanks to <b>Waterded#6455</b> for making the bounce adjustable, and <b>GiantLuigi4#6616</b> for additional
     * cleanup.<br>
     * <a href="http://easings.net/#easeInBounce">Easings.net#easeInBounce</a>
     */
    public static Double2DoubleFunction bounce(Double n) {
        final double n2 = n == null ? 0.5d : n;

        Double2DoubleFunction one = x -> 121f / 16f * x * x;
        Double2DoubleFunction two = x -> 121f / 4f * n2 * Math.pow(x - 6f / 11f, 2) + 1 - n2;
        Double2DoubleFunction three = x -> 121 * n2 * n2 * Math.pow(x - 9f / 11f, 2) + 1 - n2 * n2;
        Double2DoubleFunction four = x -> 484 * n2 * n2 * n2 * Math.pow(x - 10.5f / 11f, 2) + 1 - n2 * n2 * n2;

        return t -> Math.min(Math.min(one.apply(t), two.apply(t)), Math.min(three.apply(t), four.apply(t)));
    }

    /**
     * A negative elastic function, equivalent to inverting briefly before increasing.<br>
     * <code>f(t) = t^2 * ((n * 1.70158 + 1) * t - n * 1.70158)</code><br>
     * <a href="https://easings.net/#easeInBack">Easings.net#easeInBack</a>
     */
    public static Double2DoubleFunction back(Double n) {
        final double n2 = n == null ? 1.70158d : n * 1.70158d;

        return t -> t * t * ((n2 + 1) * t - n2);
    }

    // ---> Easing Curve Functions <--- //

    /**
     * An exponential function, equivalent to an exponential curve to the {@code n} root.<br>
     * <code>f(t) = t^n</code>
     *
     * @param n The exponent
     */
    public static Double2DoubleFunction pow(double n) {
        return t -> Math.pow(t, n);
    }

    /**
     * The MIT License (MIT) <br>
     * <br>
     * Copyright (c) 2015 Boris Chumichev <br>
     * <br>
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions: <br>
     * <br>
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
     * the Software. <br>
     * <br>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
     * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE. <br>
     * <br>
     * Returns a stepped value based on the nearest step to the input value.<br>
     * The size (grade) of the steps depends on the provided value of {@code n}
     **/
    public static Double2DoubleFunction step(Double n) {
        double n2 = n == null ? 2 : n;

        if (n2 < 2)
            throw new IllegalArgumentException("Steps must be >= 2, got: " + n2);

        final int steps = (int) n2;

        return t -> {
            double result = 0;

            if (t < 0)
                return result;

            double stepLength = (1 / (double) steps);

            if (t > (result = (steps - 1) * stepLength))
                return result;

            int testIndex;
            int leftBorderIndex = 0;
            int rightBorderIndex = steps - 1;

            while (rightBorderIndex - leftBorderIndex != 1) {
                testIndex = leftBorderIndex + (rightBorderIndex - leftBorderIndex) / 2;

                if (t >= testIndex * stepLength) {
                    leftBorderIndex = testIndex;
                } else {
                    rightBorderIndex = testIndex;
                }
            }

            return leftBorderIndex * stepLength;
        };
    }

    public static double lerpWithOverride(AzAnimationPoint animationPoint, AzEasingType override) {
        var easingType = override;

        if (override == null) {
            easingType = animationPoint.keyframe() == null
                ? AzEasingTypes.LINEAR
                : animationPoint.keyframe().easingType();
        }

        return easingType.apply(animationPoint);
    }
}
