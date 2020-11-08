package com.example.distancecalc;

/**
 * A coordinate pair
 */
public class Point {
    public final double x;
    public final double y;

    /**
     * Creates a point from a pair of numbers of any numeric type
     */
    public static <T extends Number> Point of(T x, T y) {
        return new Point(x.doubleValue(), y.doubleValue());
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Overflow/underflow safe distance calculation using {@link Math#hypot(double, double)}
     */
    public double distanceTo(Point other) {
        return Math.hypot(Math.abs(other.y - y), Math.abs(other.x - x));
    }
}
