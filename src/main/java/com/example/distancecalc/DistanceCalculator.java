package com.example.distancecalc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DistanceCalculator {

    private static final int DEFAULT_TIMEOUT_SEC = 10;

    private final int timeoutSec;
    private final int threadCount;

    private volatile boolean isRunning = false;
    private ExecutorService executor;
    private long interruptTime;

    public DistanceCalculator(int threadCount) {
        this(threadCount, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * @param threadCount number of threads for parallel distance claculations
     * @param timeoutSec number of seconds to wait in thread calling {@link DistanceCalculator#calculateDistanceMatrix(Point[])}
     *                   until calculation completes
     * @throws IllegalArgumentException when threadCount is zero or negative or timeoutSec is negative
     */
    public DistanceCalculator(int threadCount, int timeoutSec) {
        if (threadCount <= 0 || timeoutSec < 0) {
            throw new IllegalArgumentException();
        }
        this.timeoutSec = timeoutSec;
        this.threadCount = threadCount;
    }

    /**
     * Method required in the assignment.
     *
     * @see DistanceCalculator#DistanceCalculator(int, int)
     * @see DistanceCalculator#calculateDistanceMatrix(Point[])
     */
    public static double[][] calcDistanceMatrix(Point[] points, int threadCount) {
        return new DistanceCalculator(threadCount).calculateDistanceMatrix(points);
    }

    /**
     * Blocking method which triggers distance matrix calculation. Awaits {@link DistanceCalculator#timeoutSec}
     * seconds until termination.
     *
     * @param points points for calculation distance between them
     * @throws IllegalStateException whenever points is empty or too large for processing
     * @throws RuntimeException if failed to calculate the results within time: {@link DistanceCalculator#timeoutSec}
     */
    public double[][] calculateDistanceMatrix(Point[] points) {
        if (isRunning) {
            throw new IllegalStateException("Currently running calculations.");
        }
        validateInput(points);

        initExecutor();
        try {
            final int length = points.length;
            // we can use simple array without locks, since writes to each array cell do not overlap
            double[][] result = new double[length][length];

            setInterruptTimer();
            for (int i = 0; i < length; i++) {
                for (int j = i; j < length; j++) {
                    // in case we are processing very large input, stop appending workers on timeout
                    if (isInterrupted()) {
                        throw new TimeoutException("Wait limit reached.");
                    }
                    if (i == j) {
                        continue;
                    }
                    submitWorker(new Worker(i, j) {
                        @Override public void doRun(int x, int y) {
                            result[x][y] = result[y][x] = points[x].distanceTo(points[y]);
                        }
                    });
                }
            }
            awaitTermination();
            return result;
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException("Failed to calculate. Cause: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void stop() {
        interruptTime = System.nanoTime() - 1;
    }

    private void cleanup() {
        isRunning = false;
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    private void awaitTermination() throws InterruptedException, TimeoutException {
        executor.shutdown();
        // block until calculations are not complete
        if (!executor.awaitTermination(timeUntilInterrupted(), TimeUnit.NANOSECONDS)) {
            throw new TimeoutException("Wait limit reached.");
        }
    }

    private void submitWorker(Worker worker) {
        executor.submit(worker);
    }

    private void initExecutor() {
        isRunning = true;
        executor = Executors.newFixedThreadPool(threadCount);
    }

    private long timeUntilInterrupted() {
        return interruptTime - System.nanoTime();
    }

    private boolean isInterrupted() {
        return System.nanoTime() > interruptTime;
    }

    private void setInterruptTimer() {
        interruptTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSec);
    }

    private void validateInput(Point[] points) {
        if (points == null || points.length == 0) {
            throw new IllegalArgumentException();
        }
        // added this check to avoid OutOfMemory exceptions when allocating array for the result matrix
        if (Integer.MAX_VALUE / points.length < (double) points.length * Double.SIZE) {
            throw new IllegalArgumentException("Number of points exceeds maximum allowed size. " +
                    "Maximum size must not exceed sqrt(Integer.MAX_SIZE) * Double.SIZE");
        }
    }

    private static abstract class Worker implements Runnable {
        private final int x;
        private final int y;

        private Worker(int x, int y) {
            this.x = x;
            this.y = y;
        }

        protected abstract void doRun(int x, int y);

        @Override
        public final void run() {
            doRun(x, y);
        }
    }
}
