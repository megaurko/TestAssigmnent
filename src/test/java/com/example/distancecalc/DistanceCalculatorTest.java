package com.example.distancecalc;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DistanceCalculatorTest {

    @Test
    void calcDistanceMatrix() {
        Point p0 = Point.of(3, 4);
        Point p1 = Point.of(7, 1);
        Point p2 = Point.of(2, 2);
        Point[] points = {p0, p1, p2};
        double[][] result = DistanceCalculator.calcDistanceMatrix(points, 2);
        Utils.dumpSquareMatrix(result);
        assertEquals(result.length, points.length);
        assertEquals(result[0][0], 0.0);
        assertEquals(result[0][1], p0.distanceTo(p1));
        assertEquals(result[0][2], p0.distanceTo(p2));
        assertEquals(result[1][0], p1.distanceTo(p0));
        assertEquals(result[1][1], 0.0);
        assertEquals(result[1][2], p1.distanceTo(p2));
        assertEquals(result[2][0], p2.distanceTo(p0));
        assertEquals(result[2][1], p2.distanceTo(p1));
        assertEquals(result[2][2], 0.0);

        // test what happens if there is only one point
        result = DistanceCalculator.calcDistanceMatrix(new Point[]{Point.of(1, 1)}, 1);
        assertEquals(result.length, 1);
        assertEquals(result[0][0], 0.0);
    }

    @Test
    void inputValidation() {
        assertThrows(IllegalArgumentException.class, () -> new DistanceCalculator(-1));
        assertThrows(IllegalArgumentException.class, () -> new DistanceCalculator(0));
        assertThrows(IllegalArgumentException.class, () -> new DistanceCalculator(-1, -1));
        assertThrows(IllegalArgumentException.class, () -> new DistanceCalculator(0, 0));

        assertThrows(IllegalArgumentException.class, () -> DistanceCalculator.calcDistanceMatrix(new Point[0], -1));
        assertThrows(IllegalArgumentException.class, () -> DistanceCalculator.calcDistanceMatrix(new Point[0], 1));
    }

    @Test
    void rejectInputExceedingHeap() {
        Point[] veryLargeArray = IntStream.range(0, 25000)
                .mapToObj(i -> Point.of(1, 1))
                .toArray(Point[]::new);
        assertThrows(RuntimeException.class, () -> DistanceCalculator.calcDistanceMatrix(veryLargeArray, 1));
    }

    @Test
    void interruption() {
        Point[] veryLargeArray = IntStream.range(0, 1000)
                .mapToObj(i -> Point.of(1, 1))
                .toArray(Point[]::new);
        assertThrows(RuntimeException.class, () -> {
            DistanceCalculator dc = new DistanceCalculator(1);
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                dc.stop();
            });
            dc.calculateDistanceMatrix(veryLargeArray);
        });
    }
}