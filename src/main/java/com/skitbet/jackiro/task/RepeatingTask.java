package com.skitbet.jackiro.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class RepeatingTask {

    private final int time;
    private final TimeUnit timeUnit;

    public RepeatingTask(int time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public void start() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(this::run, 0, time, timeUnit);
    }

    protected abstract void run();

}
