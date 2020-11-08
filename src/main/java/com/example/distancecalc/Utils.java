package com.example.distancecalc;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {

    /**
     * Prints out a square matrix of numbers into {@link System#out}
     */
    public static void dumpSquareMatrix(double[][] matrix) {
        // its ok to use since matrix is square anyway
        final int length = matrix.length;
        IntStream.range(0, length)
                .mapToObj(i -> IntStream.range(0, length)
                        .mapToObj(j -> String.format("%.2f", Math.round(matrix[i][j] * 100.0) / 100.0))
                        .collect(Collectors.joining(",")))
                .forEach(System.out::println);
    }
}
