package com.example.distancecalc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    @Test
    void of() {
        assertDoesNotThrow(() -> Point.of(1.0, 1.0));
        assertDoesNotThrow(() -> Point.of(1, 1));
        assertDoesNotThrow(() -> Point.of((short) 1, (short) 1));
        assertThrows(NullPointerException.class, () -> Point.of(null, 1.0));
        assertThrows(NullPointerException.class, () -> Point.of(1.0, null));
        assertThrows(NullPointerException.class, () -> Point.of(null, null));
    }

    @Test
    void distanceTo() {
        Point p0 = Point.of(3, 4);
        Point p1 = Point.of(7, 1);
        assertEquals(p0.distanceTo(p1), 5.0);
        assertEquals(p0.distanceTo(p0), 0.0);

        Point p2 = Point.of(Double.MIN_VALUE, Double.MAX_VALUE);
        Point p3 = Point.of(Double.MAX_VALUE, Double.MIN_VALUE);
        // check that there is no overflow/underflow
        assertEquals(p2.distanceTo(p3), Double.POSITIVE_INFINITY);
    }
}