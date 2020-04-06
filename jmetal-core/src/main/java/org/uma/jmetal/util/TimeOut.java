package org.uma.jmetal.util;

import java.util.concurrent.TimeUnit;

public class TimeOut {
    private long start_time;
    private long duration;
    private TimeUnit unit;

    public void start() {
        start_time = System.nanoTime();
    }

    public TimeOut(long duration, TimeUnit unit) {
        start_time = 0;
        this.duration = TimeUnit.NANOSECONDS.convert(duration, unit);
        this.unit = unit;
    }

    public TimeOut reset() {
        start_time = System.nanoTime();
        return this;
    }

    public boolean isTimeElapsed() {
        long current_time = System.nanoTime();
        return (current_time - start_time) > duration;
    }
}